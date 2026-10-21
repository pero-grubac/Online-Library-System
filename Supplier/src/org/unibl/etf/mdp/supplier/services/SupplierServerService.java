package org.unibl.etf.mdp.supplier.services;

import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** The supplier's side of the SupplierServer protocol. */
public class SupplierServerService {
    private static final AppConfig conf = new AppConfig();
    private static final String GET_DTO_MSG = conf.getDtoMsg();
    private static final String END_MSG = conf.getEndMsg();
    private static final String GET_MODEL_MSG = conf.getModelMsg();
    private static final String LOAD_LINKS_MSG = conf.getLoadLinksMsg();
    private static final String ADD_BOOK_MSG = conf.getAddBookMsg();
    private static final String ERROR_MSG = conf.getErrorMsg();
    private final String host;
    private final int port;

    public SupplierServerService() {
        this(conf.getDefaultHost(), conf.getSupplierServerTCPPort());
    }

    public SupplierServerService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** What the SupplierServer made of the supplier's linkovi.txt. */
    public static class Offer {
        public final List<BookDto> books = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
    }

    /**
     * Has the SupplierServer download every book in the supplier's linkovi.txt.
     *
     * @param onBook called for each book as it arrives, so the GUI can show progress
     */
    public Offer loadOffer(String supplier, Consumer<BookDto> onBook) throws IOException {
        Offer offer = new Offer();
        try (JsonChannel channel = JsonChannel.connect(host, port)) {
            channel.send(new Message(LOAD_LINKS_MSG, supplier));
            Message message;
            while (!END_MSG.equals((message = channel.receive()).getType())) {
                if (GET_DTO_MSG.equals(message.getType())) {
                    BookDto book = message.getBody(BookDto.class);
                    offer.books.add(book);
                    onBook.accept(book);
                } else if (ERROR_MSG.equals(message.getType())) {
                    offer.errors.add(message.getBody(String.class));
                }
            }
            channel.send(new Message(END_MSG));
        }
        return offer;
    }

    /** Downloads one more Gutenberg book into the offer. */
    public BookDto addFromLink(String supplier, String link) throws IOException {
        return expectBook(new Message(GET_DTO_MSG, supplier, link));
    }

    /** Stores a book the supplier entered by hand. */
    public BookDto addBook(String supplier, Book book) throws IOException {
        return expectBook(new Message(ADD_BOOK_MSG, supplier, book));
    }

    private BookDto expectBook(Message request) throws IOException {
        try (JsonChannel channel = JsonChannel.connect(host, port)) {
            Message answer = channel.request(request);
            channel.send(new Message(END_MSG));
            if (ERROR_MSG.equals(answer.getType())) {
                throw new IOException(answer.getBody(String.class));
            }
            return answer.getBody(BookDto.class);
        }
    }

    /**
     * The full books for an order, cover included.
     *
     * @throws IOException when any of them is missing, so no partial order is ever sent
     */
    public List<Book> getBooks(String supplier, List<BookDto> bookDtos) throws IOException {
        List<Book> books = new ArrayList<>();
        try (JsonChannel channel = JsonChannel.connect(host, port)) {
            for (BookDto bookDto : bookDtos) {
                Message answer = channel.request(new Message(GET_MODEL_MSG, supplier, bookDto));
                if (!GET_MODEL_MSG.equals(answer.getType())) {
                    throw new IOException("\"" + bookDto.getTitle() + "\" is not in the supplier's store");
                }
                books.add(answer.getBody(Book.class));
            }
            channel.send(new Message(END_MSG));
        }
        return books;
    }
}
