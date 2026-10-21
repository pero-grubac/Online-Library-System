package org.unibl.etf.mdp.supplier.gui;

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.OrderItem;
import org.unibl.etf.mdp.supplier.app.App;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.mq.DirectReceiver;
import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.server.Server;
import org.unibl.etf.mdp.supplier.services.LibraryService;
import org.unibl.etf.mdp.supplier.services.Pricing;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** State of the logged-in supplier: its offer, its open orders and the connections behind them. */
public class Data {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(Data.class.getName());
    private static Data instance = null;
    private final SupplierServerService serverService = new SupplierServerService();
    private final LibraryService libraryService = new LibraryService();
    private final Pricing pricing = new Pricing(conf.getPriceMin(), conf.getPriceMax(), new Random());
    private final String username;
    // Shared with the supplier's TCP server, so new books are offered at once
    private final List<BookDto> books = new CopyOnWriteArrayList<>();
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private final Server server;
    private DirectReceiver receiver;
    private String queueProblem;

    /** An order from the library waiting for the supplier's decision. */
    public static class Request {
        private final List<OrderItem> items;
        private final long deliveryTag;
        private final LocalDateTime received = LocalDateTime.now();

        Request(List<OrderItem> items, long deliveryTag) {
            this.items = items;
            this.deliveryTag = deliveryTag;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public LocalDateTime getReceived() {
            return received;
        }
    }

    private Data(String username) throws IOException {
        this.username = username;
        server = new Server(username, books);
        server.start();
        try {
            receiver = new DirectReceiver();
            receiver.startListening(username, this::onOrder);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot receive orders", e);
            queueProblem = "Orders cannot be received: RabbitMQ is not reachable (" + e.getMessage() + ").";
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Starts the supplier: registers its server with the DiscoveryServer and starts receiving orders.
     *
     * @throws Server.NameTakenException when this supplier is already running elsewhere
     */
    public static synchronized Data start(String username) throws IOException {
        if (instance == null) {
            instance = new Data(username);
        }
        return instance;
    }

    public static synchronized Data getInstance() {
        return instance;
    }

    public SupplierServerService getServerService() {
        return serverService;
    }

    /** Null when everything works; otherwise why orders cannot arrive. */
    public String getQueueProblem() {
        return queueProblem;
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void changed() {
        listeners.forEach(Runnable::run);
    }

    // Orders come in oldest first; anything that is not an order is refused
    private void onOrder(Message message, long deliveryTag) {
        List<OrderItem> items = message.getBodyList(OrderItem.class).stream()
                .filter(i -> i != null && i.getBook() != null && i.getQuantity() > 0).collect(Collectors.toList());
        if (items.isEmpty()) {
            logger.warning("Ignoring an order without books: " + message);
            ack(deliveryTag);
            return;
        }
        requests.add(new Request(items, deliveryTag));
        changed();
    }

    private void ack(long deliveryTag) {
        try {
            receiver.ack(deliveryTag);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not acknowledge an order", e);
        }
    }

    public String getUsername() {
        return username;
    }

    public List<BookDto> getBooks() {
        return books;
    }

    /** Adds a book to the offer, or replaces the one with the same key. */
    public void addBook(BookDto book) {
        books.removeIf(b -> b.getKey().equals(book.getKey()));
        books.add(book);
        changed();
    }

    public void replaceOffer(List<BookDto> offer) {
        books.clear();
        books.addAll(offer);
        changed();
    }

    public List<Request> getRequests() {
        return requests;
    }

    /**
     * Approves an order: loads the full books, books the invoice over RMI and sends books and invoice to the
     * library. Only then is the order removed from the queue; on any failure it stays and can be retried.
     */
    public Invoice approve(Request request) throws IOException {
        List<BookDto> ordered = request.getItems().stream().map(OrderItem::getBook).collect(Collectors.toList());
        List<Book> fullBooks = serverService.getBooks(username, ordered);
        Invoice invoice;
        try {
            invoice = libraryService.approve(request.getItems(), fullBooks, username, App.getAccountingService(), pricing);
        } catch (RemoteException e) {
            // The AccountingServer may have been restarted; look it up again next time
            App.resetAccountingService();
            throw new IOException("The accounting service did not accept the invoice: " + e.getMessage(), e);
        }
        finish(request);
        return invoice;
    }

    public void reject(Request request) throws IOException {
        libraryService.deny(request.getItems(), username);
        finish(request);
    }

    private void finish(Request request) {
        ack(request.deliveryTag);
        requests.remove(request);
        changed();
    }

    public void shutdown() {
        server.shutdown();
        if (receiver != null) {
            try {
                receiver.shutdown();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error while closing the order queue", e);
            }
        }
    }
}
