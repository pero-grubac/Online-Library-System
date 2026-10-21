package org.unibl.etf.mdp.library.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.library.services.DeliveryService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Suppliers deliver to the library's own server: books into the catalogue, invoices and refusals as notices. */
class DeliveryTest {
    private final List<String> catalogue = new CopyOnWriteArrayList<>();
    private final AtomicInteger catalogueChanges = new AtomicInteger();
    private DeliveryService deliveries;
    private Server server;

    @BeforeEach
    void start() throws Exception {
        deliveries = new DeliveryService(book -> {
            if (book.getTitle().equals("Broken")) {
                throw new IOException("HTTP 500");
            }
            catalogue.add(book.getTitle() + " @" + book.getPrice());
        });
        deliveries.onCatalogueChange(catalogueChanges::incrementAndGet);
        server = new Server(0, deliveries);
        server.start();
    }

    @AfterEach
    void stop() {
        server.shutdown();
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 100 && !condition.getAsBoolean(); i++) {
            Thread.sleep(50);
        }
    }

    private static Book book(String title, int price) {
        Book book = new Book(title, "Author", "English", null, price);
        book.setContent("text");
        return book;
    }

    @Test
    void approvedBooksGoIntoTheCatalogueAndTheInvoiceBecomesANotice() throws Exception {
        Invoice invoice = new Invoice();
        invoice.addItem(new BookDto(book("Emma", 0)), 2, 10);
        invoice.setVAT(3.4);

        try (JsonChannel supplier = JsonChannel.connect("localhost", server.getPort())) {
            supplier.send(new Message("APPROVE", "BookSupplier_A", book("Emma", 10)));
            supplier.send(new Message("INVOICE", "BookSupplier_A", invoice));
            supplier.send(new Message("END", "BookSupplier_A"));
        }
        waitUntil(() -> !deliveries.getNotices().isEmpty());

        assertThat(catalogue).containsExactly("Emma @10");
        assertThat(catalogueChanges).hasValue(1);
        assertThat(deliveries.getNotices()).singleElement().asString()
                .contains("Invoice from BookSupplier_A").contains("VAT 3.40");
    }

    @Test
    void refusedOrderIsReportedOnceWithEveryTitle() throws Exception {
        try (JsonChannel supplier = JsonChannel.connect("localhost", server.getPort())) {
            supplier.send(new Message("DENIAL", "BookSupplier_B", new BookDto(book("Emma", 0))));
            supplier.send(new Message("DENIAL", "BookSupplier_B", new BookDto(book("Dracula", 0))));
            supplier.send(new Message("END", "BookSupplier_B"));
        }
        waitUntil(() -> !deliveries.getNotices().isEmpty());

        assertThat(deliveries.getNotices()).singleElement().asString()
                .contains("BookSupplier_B rejected the order: Emma, Dracula");
        assertThat(catalogue).isEmpty();
    }

    @Test
    void bookTheServerRefusesBecomesANoticeInsteadOfDisappearing() throws Exception {
        try (JsonChannel supplier = JsonChannel.connect("localhost", server.getPort())) {
            supplier.send(new Message("APPROVE", "BookSupplier_A", book("Broken", 5)));
            supplier.send(new Message("END", "BookSupplier_A"));
        }
        waitUntil(() -> !deliveries.getNotices().isEmpty());

        assertThat(deliveries.getNotices()).singleElement().asString()
                .contains("\"Broken\" from BookSupplier_A could not be added").contains("HTTP 500");
    }

    @Test
    void secondLibraryOnTheSamePortCannotStart() {
        Server second = new Server(server.getPort(), deliveries);

        assertThatThrownBy(second::start).isInstanceOf(IOException.class);
    }
}
