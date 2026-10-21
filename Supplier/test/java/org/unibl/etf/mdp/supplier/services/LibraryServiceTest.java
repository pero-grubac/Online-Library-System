package org.unibl.etf.mdp.supplier.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.InvoiceItem;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.OrderItem;
import org.unibl.etf.mdp.service.IAccountingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Approving and rejecting orders, against a fake library socket and a fake accounting service. */
class LibraryServiceTest {
    private ServerSocket fakeLibrary;
    private final List<Message> delivered = new CopyOnWriteArrayList<>();
    private final AtomicInteger invoicesBooked = new AtomicInteger();

    @AfterEach
    void stop() throws Exception {
        if (fakeLibrary != null) {
            fakeLibrary.close();
        }
    }

    private int startFakeLibrary() throws IOException {
        fakeLibrary = new ServerSocket(0);
        Thread thread = new Thread(() -> {
            try (JsonChannel channel = new JsonChannel(fakeLibrary.accept())) {
                Message message;
                do {
                    message = channel.receive();
                    delivered.add(message);
                } while (!"END".equals(message.getType()));
            } catch (Exception ignored) {
                // Test ended
            }
        });
        thread.setDaemon(true);
        thread.start();
        return fakeLibrary.getLocalPort();
    }

    private static int closedPort() throws IOException {
        try (ServerSocket free = new ServerSocket(0)) {
            return free.getLocalPort();
        }
    }

    // Adds 17% VAT like the real AccountingServer
    private final IAccountingService accounting = (invoice, supplier) -> {
        invoicesBooked.incrementAndGet();
        invoice.setVAT(Math.round(invoice.getTotalPrice() * 17) / 100.0);
        return invoice;
    };

    private static Book book(String title) {
        Book book = new Book(title, "Author", "English", null, 0);
        book.setContent("text");
        return book;
    }

    private static List<OrderItem> order() {
        return List.of(new OrderItem(new BookDto(book("Emma")), 2), new OrderItem(new BookDto(book("Dracula")), 1));
    }

    private void waitForEnd() throws InterruptedException {
        for (int i = 0; i < 100 && delivered.stream().noneMatch(m -> "END".equals(m.getType())); i++) {
            Thread.sleep(50);
        }
    }

    @Test
    void approvalDeliversPricedBooksAndTheInvoice() throws Exception {
        LibraryService service = new LibraryService("localhost", startFakeLibrary());
        // Fixed seed: prices are random per order, but repeatable here
        Pricing pricing = new Pricing(10, 10, new Random(1));

        Invoice invoice = service.approve(order(), List.of(book("Emma"), book("Dracula")), "BookSupplier_T",
                accounting, pricing);
        waitForEnd();

        assertThat(invoice.getItems()).extracting(InvoiceItem::getQuantity).containsExactly(2, 1);
        assertThat(invoice.getTotalPrice()).isEqualTo(30);
        assertThat(invoice.getVAT()).isEqualTo(5.1);
        assertThat(delivered).extracting(Message::getType).containsExactly("APPROVE", "APPROVE", "INVOICE", "END");
        assertThat(delivered.get(0).getBody(Book.class).getPrice()).isEqualTo(10);
        assertThat(delivered.get(2).getBody(Invoice.class).getVAT()).isEqualTo(5.1);
    }

    @Test
    void unreachableLibraryFailsBeforeTheInvoiceIsBooked() throws Exception {
        LibraryService service = new LibraryService("localhost", closedPort());

        assertThatThrownBy(() -> service.approve(order(), List.of(book("Emma"), book("Dracula")), "BookSupplier_T",
                accounting, new Pricing(5, 50, new Random()))).isInstanceOf(IOException.class);
        assertThat(invoicesBooked).hasValue(0);
    }

    @Test
    void failedInvoiceSendsNoBooks() throws Exception {
        LibraryService service = new LibraryService("localhost", startFakeLibrary());
        IAccountingService broken = (invoice, supplier) -> {
            throw new RemoteException("accounting is down");
        };

        assertThatThrownBy(() -> service.approve(order(), List.of(book("Emma"), book("Dracula")), "BookSupplier_T",
                broken, new Pricing(5, 50, new Random()))).isInstanceOf(RemoteException.class);
        Thread.sleep(200);
        assertThat(delivered).extracting(Message::getType).doesNotContain("APPROVE");
    }

    @Test
    void missingAccountingServiceIsAnError() throws Exception {
        LibraryService service = new LibraryService("localhost", closedPort());

        assertThatThrownBy(() -> service.approve(order(), List.of(book("Emma"), book("Dracula")), "BookSupplier_T",
                null, new Pricing(5, 50, new Random()))).isInstanceOf(IOException.class)
                .hasMessageContaining("accounting");
    }

    @Test
    void rejectionNamesEveryRefusedBook() throws Exception {
        LibraryService service = new LibraryService("localhost", startFakeLibrary());

        service.deny(order(), "BookSupplier_T");
        waitForEnd();

        assertThat(delivered).extracting(Message::getType).containsExactly("DENIAL", "DENIAL", "END");
        assertThat(delivered.get(1).getBody(BookDto.class).getTitle()).isEqualTo("Dracula");
    }

    @Test
    void pricesStayInTheConfiguredRange() {
        Pricing pricing = new Pricing(5, 50, new Random(42));
        for (int i = 0; i < 1000; i++) {
            assertThat(pricing.nextPrice()).isBetween(5, 50);
        }
        assertThatThrownBy(() -> new Pricing(10, 5, new Random())).isInstanceOf(IllegalArgumentException.class);
    }
}
