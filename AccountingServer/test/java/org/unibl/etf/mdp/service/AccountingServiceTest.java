package org.unibl.etf.mdp.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.unibl.etf.mdp.common.SerialFilter;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.InvoiceItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class AccountingServiceTest {
    // Not the production 1099, so the test can run next to a live AccountingServer
    private static final int RMI_PORT = 21099;
    private static final String SUPPLIER = "BookSupplier_T";

    @TempDir
    Path invoicesDir;

    private AccountingService service() throws RemoteException {
        return new AccountingService(invoicesDir, new BigDecimal("0.17"));
    }

    private static BookDto book(String title) {
        BookDto book = new BookDto();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setCoverImageBase64("aGVsbG8=");
        return book;
    }

    /** One copy of each book at the given unit prices. */
    private static Invoice invoice(int... prices) {
        Invoice invoice = new Invoice();
        for (int i = 0; i < prices.length; i++) {
            invoice.addItem(book("Book " + i), 1, prices[i]);
        }
        return invoice;
    }

    private List<Path> savedInvoices() throws IOException {
        try (Stream<Path> files = Files.list(invoicesDir.resolve(SUPPLIER))) {
            return files.collect(Collectors.toList());
        }
    }

    @Test
    void vatIsSeventeenPercentRoundedToCents() throws Exception {
        // 42 * 0.17 = 7.14
        assertThat(service().addInvoice(invoice(10, 25, 7), SUPPLIER).getVAT()).isCloseTo(7.14, within(1e-9));
        assertThat(service().addInvoice(invoice(33), SUPPLIER).getVAT()).isCloseTo(5.61, within(1e-9));
    }

    @Test
    void vatCoversQuantities() throws Exception {
        Invoice invoice = new Invoice();
        invoice.addItem(book("Emma"), 4, 25);

        assertThat(service().addInvoice(invoice, SUPPLIER).getVAT()).isCloseTo(17.0, within(1e-9));
    }

    @Test
    void invoiceIsSavedAsJsonWithItemsAndNoCovers() throws Exception {
        Invoice invoice = invoice(10, 32);
        invoice.setDate(LocalDate.of(2024, 3, 15));

        service().addInvoice(invoice, SUPPLIER);

        List<Path> saved = savedInvoices();
        assertThat(saved).singleElement()
                .satisfies(p -> assertThat(p.getFileName().toString()).startsWith("15-03-2024-").endsWith(".json"));
        String json = Files.readString(saved.get(0));
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        assertThat(root.get("totalPrice").getAsInt()).isEqualTo(42);
        assertThat(root.get("VAT").getAsDouble()).isCloseTo(7.14, within(1e-9));
        assertThat(root.get("date").getAsString()).isEqualTo("2024-03-15");
        assertThat(root.getAsJsonArray("items")).hasSize(2);
        assertThat(json).doesNotContain("coverImageBase64");
    }

    @Test
    void identicalInvoicesOfOneDayAreKeptApart() throws Exception {
        service().addInvoice(invoice(10), SUPPLIER);
        service().addInvoice(invoice(10), SUPPLIER);

        assertThat(savedInvoices()).hasSize(2);
    }

    @Test
    void supplierIsToldWhenTheInvoiceCannotBeSaved() throws Exception {
        // A file where the supplier's folder should be
        Files.writeString(invoicesDir.resolve(SUPPLIER), "blocked");

        assertThatThrownBy(() -> service().addInvoice(invoice(10), SUPPLIER)).isInstanceOf(RemoteException.class);
    }

    @Test
    void worksAsARemoteObjectOverRmi() throws Exception {
        AccountingService service = service();
        IAccountingService stub = (IAccountingService) UnicastRemoteObject.exportObject(service, 0);
        SerialFilter.install();
        Registry registry = LocateRegistry.createRegistry(RMI_PORT);
        try {
            registry.rebind("AccountingServer", stub);

            IAccountingService remote = (IAccountingService) LocateRegistry.getRegistry(RMI_PORT).lookup("AccountingServer");
            Invoice result = remote.addInvoice(invoice(100), SUPPLIER);

            assertThat(result.getVAT()).isCloseTo(17.0, within(1e-9));
            assertThat(result.getItems()).extracting(InvoiceItem::getTitle).containsExactly("Book 0");
            assertThat(savedInvoices()).hasSize(1);
        } finally {
            registry.unbind("AccountingServer");
            UnicastRemoteObject.unexportObject(service, true);
            UnicastRemoteObject.unexportObject(registry, true);
        }
    }
}
