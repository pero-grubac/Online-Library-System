package org.unibl.etf.mdp.library.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;

import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/** The library side of the offer protocol, against a fake supplier socket. */
class SupplierServiceTest {
    private ServerSocket fakeSupplier;
    private final List<String> acknowledgements = new CopyOnWriteArrayList<>();

    @AfterEach
    void stop() throws Exception {
        fakeSupplier.close();
    }

    private int startFakeSupplier(String... titles) throws Exception {
        fakeSupplier = new ServerSocket(0);
        Thread thread = new Thread(() -> {
            try (JsonChannel channel = new JsonChannel(fakeSupplier.accept())) {
                if (!"GET_DTO".equals(channel.receive().getType())) {
                    return;
                }
                for (String title : titles) {
                    BookDto book = new BookDto();
                    book.setTitle(title);
                    acknowledgements.add(channel.request(new Message("GET_DTO", "BookSupplier_A", book)).getType());
                }
                channel.send(new Message("END", "BookSupplier_A"));
            } catch (Exception ignored) {
            }
        });
        thread.setDaemon(true);
        thread.start();
        return fakeSupplier.getLocalPort();
    }

    @Test
    void collectsTheWholeOfferAndAcknowledgesEachBook() throws Exception {
        int port = startFakeSupplier("Emma", "Dracula", "Ulysses");

        List<BookDto> offer = SupplierService.getOfferedBooks("library", port);

        assertThat(offer).extracting(BookDto::getTitle).containsExactly("Emma", "Dracula", "Ulysses");
        assertThat(acknowledgements).containsExactly("OK", "OK", "OK");
    }

    @Test
    void emptyOfferGivesAnEmptyList() throws Exception {
        int port = startFakeSupplier();

        assertThat(SupplierService.getOfferedBooks("library", port)).isEmpty();
    }

    @Test
    void unreachableSupplierGivesAnEmptyListInsteadOfFailing() throws Exception {
        int port = startFakeSupplier();
        fakeSupplier.close();

        assertThat(SupplierService.getOfferedBooks("library", port)).isEmpty();
    }
}
