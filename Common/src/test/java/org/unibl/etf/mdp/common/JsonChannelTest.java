package org.unibl.etf.mdp.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.Message;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonChannelTest {
    private ServerSocket server;
    private JsonChannel client;
    private JsonChannel peer;

    @BeforeEach
    void connect() throws Exception {
        server = new ServerSocket(0);
        client = JsonChannel.connect("localhost", server.getLocalPort());
        peer = new JsonChannel(server.accept());
        peer.getSocket().setSoTimeout(5000);
    }

    @AfterEach
    void close() throws Exception {
        client.close();
        peer.close();
        server.close();
    }

    private static Book book() {
        Book book = new Book("Emma", "Jane Austen", "English", new Date(-4796841600000L), 30);
        book.setContent("Line one\nLine \"two\" with ünïcödé\n");
        book.setCoverImageBase64("aGVsbG8=");
        return book;
    }

    @Test
    void bookSurvivesTheTripIncludingDateAndNewlines() throws Exception {
        Book sent = book();

        client.send(new Message("APPROVE", "BookSupplier_A", sent));
        Message received = peer.receive();

        assertThat(received.getType()).isEqualTo("APPROVE");
        assertThat(received.getUsername()).isEqualTo("BookSupplier_A");
        Book book = received.getBody(Book.class);
        assertThat(book).isEqualTo(sent);
        assertThat(book.getContent()).isEqualTo(sent.getContent());
        assertThat(book.getKey()).isEqualTo(sent.getKey());
    }

    @Test
    void listsAndInvoicesKeepTheirElementTypes() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setDate(LocalDate.of(2024, 3, 15));
        invoice.addItem(new BookDto(book()), 1, 30);

        client.send(new Message("REQUEST", "library", List.of(new BookDto(book()), new BookDto(book()))));
        client.send(new Message("INVOICE", "BookSupplier_A", invoice));

        assertThat(peer.receive().getBodyList(BookDto.class)).hasSize(2).allMatch(b -> b.getTitle().equals("Emma"));
        Invoice received = peer.receive().getBody(Invoice.class);
        assertThat(received).isEqualTo(invoice);
        assertThat(received.getTotalPrice()).isEqualTo(30);
    }

    @Test
    void messagesWithoutBodyStayEmpty() throws Exception {
        client.send(new Message("END"));

        Message received = peer.receive();
        assertThat(received.hasBody()).isFalse();
        assertThat(received.getBody(String.class)).isNull();
    }

    @Test
    void closedConnectionIsReportedAsEof() throws Exception {
        client.close();

        assertThatThrownBy(peer::receive).isInstanceOf(EOFException.class);
    }

    @Test
    void garbageIsRejectedInsteadOfInstantiated() throws Exception {
        OutputStream raw = client.getSocket().getOutputStream();
        raw.write("{not json\n".getBytes(StandardCharsets.UTF_8));
        raw.write("{\"username\":\"x\"}\n".getBytes(StandardCharsets.UTF_8));
        raw.flush();

        assertThatThrownBy(peer::receive).isInstanceOf(IOException.class).hasMessageContaining("Malformed");
        assertThatThrownBy(peer::receive).isInstanceOf(IOException.class).hasMessageContaining("without a type");
    }

    @Test
    void oversizedMessageIsRefused() throws Exception {
        Thread writer = new Thread(() -> {
            try (Socket socket = client.getSocket()) {
                OutputStream raw = socket.getOutputStream();
                byte[] chunk = new byte[1024 * 1024];
                java.util.Arrays.fill(chunk, (byte) 'a');
                for (int i = 0; i <= JsonChannel.MAX_MESSAGE_BYTES / chunk.length; i++) {
                    raw.write(chunk);
                }
            } catch (IOException ignored) {
                // The reader hangs up
            }
        });
        writer.start();

        assertThatThrownBy(peer::receive).isInstanceOf(IOException.class).hasMessageContaining("larger than");
        peer.close();
        writer.join(10_000);
    }
}
