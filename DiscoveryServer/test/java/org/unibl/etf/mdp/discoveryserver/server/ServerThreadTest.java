package org.unibl.etf.mdp.discoveryserver.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.common.JsonChannel;
import org.unibl.etf.mdp.model.Message;

import java.net.ServerSocket;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The discovery protocol over a real TCP socket, the way suppliers and members use it. */
class ServerThreadTest {
    static ServerSocket serverSocket;

    @BeforeAll
    static void startServer() throws Exception {
        serverSocket = new ServerSocket(0);
        Thread acceptor = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    new ServerThread(serverSocket.accept());
                } catch (Exception closed) {
                    return;
                }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        serverSocket.close();
    }

    private static JsonChannel connect() throws Exception {
        JsonChannel channel = JsonChannel.connect("localhost", serverSocket.getLocalPort());
        channel.getSocket().setSoTimeout(5000);
        return channel;
    }

    private static Map<String, String> list(JsonChannel channel, String type) throws Exception {
        return channel.request(new Message(type)).getBody(Json.STRING_MAP);
    }

    private static String unique(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void registeredSupplierIsListedForTheLibrary() throws Exception {
        String supplier = unique("Supplier");
        try (JsonChannel client = connect()) {
            assertThat(client.request(new Message("DISCOVERY", supplier, "50123")).getType()).isEqualTo("OK");
            client.send(new Message("END"));
        }

        try (JsonChannel library = connect()) {
            assertThat(list(library, "DISCOVER_ALL")).containsEntry(supplier, "50123");
        }
    }

    @Test
    void supplierPortMayBeSentAsAnInteger() throws Exception {
        String supplier = unique("Supplier");
        try (JsonChannel client = connect()) {
            assertThat(client.request(new Message("DISCOVERY", supplier, 50124)).getType()).isEqualTo("OK");
            assertThat(list(client, "DISCOVER_ALL")).containsEntry(supplier, "50124");
        }
    }

    @Test
    void registeredMemberIsListedForOtherMembers() throws Exception {
        String member = unique("member");
        try (JsonChannel client = connect()) {
            assertThat(client.request(new Message("DISCOVER_USER", member, 40001)).getType()).isEqualTo("OK");

            Message response = client.request(new Message("GET_ALL_USERS", member));
            assertThat(response.getType()).isEqualTo("GET_ALL_USERS");
            Map<String, String> members = response.getBody(Json.STRING_MAP);
            assertThat(members).containsEntry(member, "40001");
        }
    }

    @Test
    void suppliersAndMembersAreKeptApart() throws Exception {
        String member = unique("member");
        try (JsonChannel client = connect()) {
            client.request(new Message("DISCOVER_USER", member, 40002));

            assertThat(list(client, "DISCOVER_ALL")).doesNotContainKey(member);
        }
    }

    @Test
    void secondSupplierWithTheSameNameIsRefused() throws Exception {
        String supplier = unique("Supplier");
        try (JsonChannel client = connect()) {
            assertThat(client.request(new Message("DISCOVERY", supplier, "50125")).getType()).isEqualTo("OK");
            // Heartbeat from the same supplier
            assertThat(client.request(new Message("DISCOVERY", supplier, "50125")).getType()).isEqualTo("OK");
            // Another app started with the same name
            assertThat(client.request(new Message("DISCOVERY", supplier, "50126")).getType()).isEqualTo("TAKEN");

            assertThat(list(client, "DISCOVER_ALL")).containsEntry(supplier, "50125");
        }
    }

    @Test
    void unregisteredSupplierDisappearsImmediately() throws Exception {
        String supplier = unique("Supplier");
        try (JsonChannel client = connect()) {
            client.request(new Message("DISCOVERY", supplier, 50127));
            assertThat(client.request(new Message("UNREGISTER", supplier, null, "server")).getType()).isEqualTo("OK");

            assertThat(list(client, "DISCOVER_ALL")).doesNotContainKey(supplier);
        }
    }

    @Test
    void invalidPortIsRefused() throws Exception {
        String supplier = unique("Supplier");
        try (JsonChannel client = connect()) {
            client.send(new Message("DISCOVERY", supplier, "not a port"));
        }

        try (JsonChannel library = connect()) {
            assertThat(list(library, "DISCOVER_ALL")).doesNotContainKey(supplier);
        }
    }
}
