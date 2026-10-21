package org.unibl.etf.mdp.library.services;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.Invoice;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * What suppliers deliver: approved books go into the catalogue, invoices and refusals become notices
 * for the librarian. Listeners hear about every change.
 */
public class DeliveryService {
    private static final Logger logger = FileLogger.getLogger(DeliveryService.class.getName());
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
    private final List<String> notices = new CopyOnWriteArrayList<>();
    private final List<Runnable> catalogueListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> noticeListeners = new CopyOnWriteArrayList<>();
    private final BookCatalogue catalogue;

    /** Where approved books end up; the LibraryServer in the application. */
    public interface BookCatalogue {
        void add(Book book) throws IOException;
    }

    public DeliveryService(BookCatalogue catalogue) {
        this.catalogue = catalogue;
    }

    public void onCatalogueChange(Runnable listener) {
        catalogueListeners.add(listener);
    }

    public void onNotice(Runnable listener) {
        noticeListeners.add(listener);
    }

    public List<String> getNotices() {
        return new ArrayList<>(notices);
    }

    private void notice(String text) {
        notices.add(0, LocalDateTime.now().format(TIME) + "  " + text);
        noticeListeners.forEach(Runnable::run);
    }

    public void bookArrived(String supplier, Book book) {
        try {
            catalogue.add(book);
            catalogueListeners.forEach(Runnable::run);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not add a delivered book: " + book.getTitle(), e);
            notice("\"" + book.getTitle() + "\" from " + supplier + " could not be added to the catalogue: "
                    + e.getMessage());
        }
    }

    public void invoiceArrived(String supplier, Invoice invoice) {
        notice("Invoice from " + supplier + ": total " + invoice.getTotalPrice() + ", VAT "
                + String.format(Locale.ROOT, "%.2f", invoice.getVAT()) + "\n" + invoice);
    }

    public void orderRefused(String supplier, List<String> titles) {
        notice(supplier + " rejected the order: " + String.join(", ", titles));
    }
}
