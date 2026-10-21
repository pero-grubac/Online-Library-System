package org.unibl.etf.mdp.supplierserver.server;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplierserver.logger.FileLogger;
import org.unibl.etf.mdp.supplierserver.properties.AppConfig;
import org.unibl.etf.mdp.supplierserver.service.BookService;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One supplier connection:
 * LOAD_LINKS streams the offer from the supplier's linkovi.txt (GET_DTO per book, ERROR per failed link, END),
 * GET_DTO downloads one link, ADD_BOOK stores a book entered by hand, GET_MODEL returns a stored book in full.
 */
public class ServerThread extends Thread {
    public static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());
    private static final String GET_DTO_MSG = conf.getDtoMsg();
    private static final String END_MSG = conf.getEndMsg();
    private static final String GET_MODEL_MSG = conf.getModelMsg();
    private static final String LOAD_LINKS_MSG = conf.getLoadLinksMsg();
    private static final String ADD_BOOK_MSG = conf.getAddBookMsg();
    private static final String ERROR_MSG = conf.getErrorMsg();
    private static final String SERVER_NAME = "SupplierServer";
    private final JsonChannel channel;
    private final BookService service;

    public ServerThread(Socket sock) throws IOException {
        this(sock, new BookService());
    }

    public ServerThread(Socket sock, BookService service) throws IOException {
        this.channel = new JsonChannel(sock);
        this.service = service;
        start();
    }

    public void run() {
        try {
            while (true) {
                Message request = channel.receive();
                String supplier = request.getUsername();
                if (LOAD_LINKS_MSG.equals(request.getType())) {
                    loadOffer(supplier);
                } else if (GET_DTO_MSG.equals(request.getType())) {
                    String link = request.getBody(String.class);
                    Message answer = download(link, supplier);
                    if (GET_DTO_MSG.equals(answer.getType())) {
                        // A link added from the GUI belongs to the offer from now on
                        service.rememberLink(supplier, link);
                    }
                    channel.send(answer);
                } else if (ADD_BOOK_MSG.equals(request.getType())) {
                    channel.send(addBook(request.getBody(Book.class), supplier));
                } else if (GET_MODEL_MSG.equals(request.getType())) {
                    Book book = service.readBook(request.getBody(BookDto.class), supplier);
                    channel.send(book == null
                            ? new Message(ERROR_MSG, SERVER_NAME, "Book is not in the supplier's store")
                            : new Message(GET_MODEL_MSG, SERVER_NAME, book));
                } else if (END_MSG.equals(request.getType())) {
                    break;
                } else {
                    logger.warning("Unknown message type: " + request.getType());
                }
            }
        } catch (EOFException ex) {
            // Client closed the connection without END
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception.", ex);
        } finally {
            cleanup();
        }
    }

    private void loadOffer(String supplier) throws IOException {
        List<String> links;
        try {
            links = service.readLinks(supplier);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Cannot read the links of " + supplier, e);
            channel.send(new Message(ERROR_MSG, SERVER_NAME, e.getMessage()));
            channel.send(new Message(END_MSG, SERVER_NAME));
            return;
        }
        // One bad link must not cost the rest of the offer
        for (String link : links) {
            channel.send(download(link, supplier));
        }
        for (BookDto entered : service.enteredBooks(supplier)) {
            channel.send(new Message(GET_DTO_MSG, SERVER_NAME, entered));
        }
        channel.send(new Message(END_MSG, SERVER_NAME));
    }

    private Message download(String url, String supplier) {
        try {
            Book book = service.getBookFromUrl(url);
            service.saveBook(book, supplier);
            BookDto dto = service.toDto(book);
            logger.info("Book " + dto + " downloaded for " + supplier);
            return new Message(GET_DTO_MSG, SERVER_NAME, dto);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not download " + url, e);
            return new Message(ERROR_MSG, SERVER_NAME, url + ": " + e.getMessage());
        }
    }

    private Message addBook(Book book, String supplier) {
        if (book == null || isBlank(book.getTitle()) || isBlank(book.getAuthor()) || isBlank(book.getLanguage())
                || isBlank(book.getContent())) {
            return new Message(ERROR_MSG, SERVER_NAME, "Title, author, language and text are required");
        }
        try {
            service.saveEnteredBook(book, supplier);
            return new Message(GET_DTO_MSG, SERVER_NAME, service.toDto(book));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not store a book of " + supplier, e);
            return new Message(ERROR_MSG, SERVER_NAME, "Could not store the book: " + e.getMessage());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void cleanup() {
        try {
            channel.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing resources.", e);
        }
    }
}
