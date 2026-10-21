package org.unibl.etf.mdp.supplier.services;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.OrderItem;
import org.unibl.etf.mdp.service.IAccountingService;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import java.io.IOException;
import java.util.List;

/** Answers the library's orders over its TCP server. */
public class LibraryService {
    private static final AppConfig conf = new AppConfig();
    private static final String END_MSG = conf.getEndMsg();
    private static final String APPROVE_MSG = conf.getApproveMsg();
    private static final String DENIAL_MSG = conf.getDenialMsg();
    private static final String INVOICE_MSG = conf.getInvoiceMsg();
    private final String host;
    private final int port;

    public LibraryService() {
        this(conf.getDefaultHost(), conf.getLibraryTCPPort());
    }

    public LibraryService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Prices the order, books the invoice with the accounting service and delivers books and invoice.
     * The library is contacted first and the invoice is booked before any book is sent, so a failure at
     * either step leaves the order untouched and it can be approved again.
     *
     * @param books the full books, in the order of {@code items}
     * @return the invoice with VAT
     */
    public Invoice approve(List<OrderItem> items, List<Book> books, String supplier, IAccountingService accounting,
                           Pricing pricing) throws IOException {
        if (items.size() != books.size()) {
            throw new IllegalArgumentException("Every ordered book must be loaded");
        }
        if (accounting == null) {
            throw new IOException("The accounting service is not available");
        }
        try (JsonChannel channel = JsonChannel.connect(host, port)) {
            Invoice invoice = new Invoice();
            int[] prices = new int[items.size()];
            for (int i = 0; i < items.size(); i++) {
                prices[i] = pricing.nextPrice();
                invoice.addItem(items.get(i).getBook(), items.get(i).getQuantity(), prices[i]);
            }
            invoice = accounting.addInvoice(invoice, supplier);

            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                // The library records the price it paid
                book.setPrice(prices[i]);
                channel.send(new Message(APPROVE_MSG, supplier, book));
            }
            channel.send(new Message(INVOICE_MSG, supplier, invoice));
            channel.send(new Message(END_MSG, supplier));
            return invoice;
        }
    }

    /** Tells the library which books of its order were refused. */
    public void deny(List<OrderItem> items, String supplier) throws IOException {
        try (JsonChannel channel = JsonChannel.connect(host, port)) {
            for (OrderItem item : items) {
                channel.send(new Message(DENIAL_MSG, supplier, item.getBook()));
            }
            channel.send(new Message(END_MSG, supplier));
        }
    }
}
