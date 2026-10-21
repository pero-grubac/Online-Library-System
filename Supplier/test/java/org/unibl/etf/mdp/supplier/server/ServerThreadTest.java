package org.unibl.etf.mdp.supplier.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A supplier streams its offer to the library one book at a time, waiting for OK after each. */
class ServerThreadTest {
    private ServerSocket serverSocket;

    @BeforeEach
    void startSupplier() throws Exception {
        List<BookDto> offer = List.of(book("Emma", 30), book("Dracula", 12));
        serverSocket = new ServerSocket(0);
        Thread acceptor = new Thread(() -> {
            try {
                new ServerThread(serverSocket.accept(), "BookSupplier_A", offer);
            } catch (Exception ignored) {
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
    }

    @AfterEach
    void stopSupplier() throws Exception {
        serverSocket.close();
    }

    private static BookDto book(String title, int price) {
        BookDto book = new BookDto();
        book.setTitle(title);
        book.setPrice(price);
        return book;
    }

    private JsonChannel connect() throws Exception {
        JsonChannel channel = JsonChannel.connect("localhost", serverSocket.getLocalPort());
        channel.getSocket().setSoTimeout(5000);
        return channel;
    }

    @Test
    void sendsEveryOfferedBookThenEnd() throws Exception {
        List<String> received = new ArrayList<>();
        try (JsonChannel channel = connect()) {
            Message message = channel.request(new Message("GET_DTO", "library"));
            while ("GET_DTO".equals(message.getType())) {
                assertThat(message.getUsername()).isEqualTo("BookSupplier_A");
                received.add(message.getBody(BookDto.class).getTitle());
                message = channel.request(new Message("OK", "library"));
            }

            assertThat(message.getType()).isEqualTo("END");
        }
        assertThat(received).containsExactly("Emma", "Dracula");
    }

    @Test
    void stopsWhenTheLibraryDoesNotAcknowledge() throws Exception {
        try (JsonChannel channel = connect()) {
            assertThat(channel.request(new Message("GET_DTO", "library")).getType()).isEqualTo("GET_DTO");

            assertThat(channel.request(new Message("NOPE", "library")).getType()).isEqualTo("END");
        }
    }
}
