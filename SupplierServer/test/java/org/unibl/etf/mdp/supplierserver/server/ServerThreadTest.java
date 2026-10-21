package org.unibl.etf.mdp.supplierserver.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplierserver.GutenbergFixture;
import org.unibl.etf.mdp.supplierserver.service.BookService;

import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The supplier-server protocol over TCP. */
class ServerThreadTest {
    private static final String SUPPLIER = "BookSupplier_T";
    private ServerSocket serverSocket;

    @TempDir
    Path store;
    @TempDir
    Path gutenberg;

    @BeforeEach
    void startServer() throws Exception {
        serverSocket = new ServerSocket(0);
        BookService service = new BookService(store);
        Thread acceptor = new Thread(() -> {
            try {
                new ServerThread(serverSocket.accept(), service);
            } catch (Exception ignored) {
                // Test ended
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        serverSocket.close();
    }

    private JsonChannel connect() throws Exception {
        JsonChannel channel = JsonChannel.connect("localhost", serverSocket.getLocalPort());
        channel.getSocket().setSoTimeout(10_000);
        return channel;
    }

    @Test
    void offerComesFromTheSuppliersLinksAndSurvivesABrokenOne() throws Exception {
        String pride = GutenbergFixture.create(gutenberg, GutenbergFixture.TITLE);
        String frankenstein = GutenbergFixture.create(gutenberg, 84, "Frankenstein", "Release date: October 31, 1993", false);
        String broken = gutenberg.resolve("pg404.txt").toUri().toString();
        Files.createDirectories(store.resolve(SUPPLIER));
        Files.writeString(store.resolve(SUPPLIER).resolve("linkovi.txt"), pride + "\n" + broken + "\n" + frankenstein + "\n");

        List<String> offered = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (JsonChannel channel = connect()) {
            channel.send(new Message("LOAD_LINKS", SUPPLIER));
            Message message;
            while (!"END".equals((message = channel.receive()).getType())) {
                if ("GET_DTO".equals(message.getType())) {
                    offered.add(message.getBody(BookDto.class).getTitle());
                } else {
                    errors.add(message.getBody(String.class));
                }
            }
            channel.send(new Message("END"));
        }

        assertThat(offered).containsExactly(GutenbergFixture.TITLE, "Frankenstein");
        assertThat(errors).singleElement().asString().contains("pg404.txt");
    }

    @Test
    void missingLinksFileIsReportedNotHung() throws Exception {
        try (JsonChannel channel = connect()) {
            channel.send(new Message("LOAD_LINKS", "Nobody"));

            assertThat(channel.receive().getType()).isEqualTo("ERROR");
            assertThat(channel.receive().getType()).isEqualTo("END");
        }
    }

    @Test
    void singleLinkGivesAPreviewAndThenTheWholeBookWithCover() throws Exception {
        String url = GutenbergFixture.create(gutenberg, GutenbergFixture.TITLE);

        try (JsonChannel channel = connect()) {
            BookDto dto = channel.request(new Message("GET_DTO", SUPPLIER, url)).getBody(BookDto.class);

            assertThat(dto.getTitle()).isEqualTo(GutenbergFixture.TITLE);
            assertThat(dto.getCoverImageBase64()).isNotBlank();
            // PREVIEW_LINES = 100, counted from the line after the start marker
            assertThat(dto.getPreview()).startsWith("line 1\n").contains("line 100\n").doesNotContain("line 101");

            dto.setCoverImageBase64(null); // the server has its own copy
            Book book = channel.request(new Message("GET_MODEL", SUPPLIER, dto)).getBody(Book.class);

            assertThat(book.getKey()).isEqualTo(dto.getKey());
            assertThat(book.getContent()).contains("line 150");
            assertThat(book.getCoverImageBytes()).isNotEmpty();

            channel.send(new Message("END"));
        }
    }

    @Test
    void bookEnteredByHandIsStoredWithItsCover() throws Exception {
        Book book = new Book("My Notes", "A. Writer", "Serbian", null, 0);
        book.setContent("Prvi red\nDrugi red\n");
        book.setCoverImageBase64(Base64.getEncoder().encodeToString(new byte[]{9, 8, 7}));

        try (JsonChannel channel = connect()) {
            Message added = channel.request(new Message("ADD_BOOK", SUPPLIER, book));
            assertThat(added.getType()).isEqualTo("GET_DTO");
            BookDto dto = added.getBody(BookDto.class);
            assertThat(dto.getPreview()).isEqualTo("Prvi red\nDrugi red\n");

            Book stored = channel.request(new Message("GET_MODEL", SUPPLIER, dto)).getBody(Book.class);
            assertThat(stored.getCoverImageBytes()).containsExactly(9, 8, 7);
        }
    }

    @Test
    void addedLinksAndHandEnteredBooksStayInTheOffer() throws Exception {
        String pride = GutenbergFixture.create(gutenberg, GutenbergFixture.TITLE);
        Book entered = new Book("My Notes", "A. Writer", "Serbian", null, 0);
        entered.setContent("Prvi red\n");
        Files.createDirectories(store.resolve(SUPPLIER));
        Files.writeString(store.resolve(SUPPLIER).resolve("linkovi.txt"), "");

        try (JsonChannel channel = connect()) {
            assertThat(channel.request(new Message("GET_DTO", SUPPLIER, pride)).getType()).isEqualTo("GET_DTO");
            assertThat(channel.request(new Message("ADD_BOOK", SUPPLIER, entered)).getType()).isEqualTo("GET_DTO");
        }
        assertThat(Files.readString(store.resolve(SUPPLIER).resolve("linkovi.txt"))).contains(pride);

        // The server accepts one connection per test, so the reload goes through a second one
        startServer();
        List<String> offered = new ArrayList<>();
        try (JsonChannel channel = connect()) {
            channel.send(new Message("LOAD_LINKS", SUPPLIER));
            Message message;
            while (!"END".equals((message = channel.receive()).getType())) {
                offered.add(message.getBody(BookDto.class).getTitle());
            }
        }
        assertThat(offered).containsExactly(GutenbergFixture.TITLE, "My Notes");
    }

    @Test
    void incompleteBookOrUnknownModelIsAnError() throws Exception {
        try (JsonChannel channel = connect()) {
            assertThat(channel.request(new Message("ADD_BOOK", SUPPLIER, new Book())).getType()).isEqualTo("ERROR");

            BookDto unknown = new BookDto();
            unknown.setTitle("Nope");
            assertThat(channel.request(new Message("GET_MODEL", SUPPLIER, unknown)).getType()).isEqualTo("ERROR");
        }
    }
}
