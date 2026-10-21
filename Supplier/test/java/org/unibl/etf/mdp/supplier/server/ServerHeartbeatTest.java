package org.unibl.etf.mdp.supplier.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The supplier registers, renews its registration and leaves the registry when it stops. */
class ServerHeartbeatTest {
    private ServerSocket fakeDiscovery;
    private final List<Message> received = new CopyOnWriteArrayList<>();

    @AfterEach
    void stop() throws Exception {
        if (fakeDiscovery != null) {
            fakeDiscovery.close();
        }
    }

    /** A DiscoveryServer that answers every request with the given type. */
    private int startFakeDiscovery(String answer) throws IOException {
        fakeDiscovery = new ServerSocket(0);
        Thread acceptor = new Thread(() -> {
            while (!fakeDiscovery.isClosed()) {
                try (JsonChannel channel = new JsonChannel(fakeDiscovery.accept())) {
                    received.add(channel.receive());
                    channel.send(new Message(answer));
                    channel.receive(); // END
                } catch (Exception ignored) {
                    // Next connection
                }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        return fakeDiscovery.getLocalPort();
    }

    private long count(String type) {
        return received.stream().filter(m -> type.equals(m.getType())).count();
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(50);
        }
    }

    @Test
    void registersRenewsAndUnregisters() throws Exception {
        Server server = new Server("BookSupplier_T", List.of(), "localhost", startFakeDiscovery("OK"), 1);
        server.start();

        waitUntil(() -> count("DISCOVERY") >= 3);
        assertThat(count("DISCOVERY")).isGreaterThanOrEqualTo(3);
        Message registration = received.get(0);
        assertThat(registration.getUsername()).isEqualTo("BookSupplier_T");
        assertThat(registration.getBody(Integer.class)).isEqualTo(server.getServerPort());

        server.shutdown();

        assertThat(received).last().satisfies(m -> {
            assertThat(m.getType()).isEqualTo("UNREGISTER");
            assertThat(m.getUsername()).isEqualTo("BookSupplier_T");
            assertThat(m.getEntityType()).isEqualTo("server");
        });
    }

    @Test
    void secondAppWithTheSameNameIsRefused() throws Exception {
        Server server = new Server("BookSupplier_T", List.of(), "localhost", startFakeDiscovery("TAKEN"), 1);

        assertThatThrownBy(server::start).isInstanceOf(Server.NameTakenException.class)
                .hasMessageContaining("BookSupplier_T");
    }

    @Test
    void missingDiscoveryServerIsReported() throws Exception {
        int port;
        try (ServerSocket free = new ServerSocket(0)) {
            port = free.getLocalPort();
        }
        Server server = new Server("BookSupplier_T", List.of(), "localhost", port, 1);

        assertThatThrownBy(server::start).isInstanceOf(IOException.class)
                .isNotInstanceOf(Server.NameTakenException.class);
    }
}
