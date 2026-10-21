package org.unibl.etf.mdp.common;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Against a tiny local server that issues numbered tokens and accepts only the newest one. */
class RestClientTest {
    private HttpServer server;
    private String base;
    private final AtomicInteger issued = new AtomicInteger();
    private final List<String> seenAuthorization = new CopyOnWriteArrayList<>();
    private final List<String> seenMethods = new CopyOnWriteArrayList<>();
    private RestClient client;

    private static void reply(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            exchange.getResponseBody().write(bytes);
        }
        exchange.close();
    }

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/login", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body.contains("\"password\":\"right\"")) {
                reply(exchange, 200, "{\"token\":\"t" + issued.incrementAndGet() + "\"}");
            } else {
                reply(exchange, 401, "");
            }
        });
        server.createContext("/data", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            seenAuthorization.add(String.valueOf(auth));
            seenMethods.add(exchange.getRequestMethod());
            boolean current = ("Bearer t" + issued.get()).equals(auth);
            reply(exchange, current ? 200 : 401, current ? "ok" : "");
        });
        server.start();
        base = "http://localhost:" + server.getAddress().getPort();
        client = new RestClient(HttpClient.newHttpClient());
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void withoutLoginRequestsAreUnauthorized() throws Exception {
        assertThat(client.get(base + "/data").status()).isEqualTo(401);
        assertThat(seenAuthorization).containsExactly("null");
    }

    @Test
    void wrongPasswordDoesNotLogIn() throws Exception {
        assertThat(client.login(base + "/login", "marko", "wrong").status()).isEqualTo(401);
        assertThat(client.isLoggedIn()).isFalse();
    }

    @Test
    void tokenIsSentWithEveryMethodIncludingPatch() throws Exception {
        assertThat(client.login(base + "/login", "marko", "right").ok()).isTrue();

        assertThat(client.get(base + "/data").body()).isEqualTo("ok");
        assertThat(client.patch(base + "/data", "text/plain", "").ok()).isTrue();
        assertThat(client.put(base + "/data", "{}").ok()).isTrue();

        assertThat(seenMethods).containsExactly("GET", "PATCH", "PUT");
        assertThat(seenAuthorization).containsOnly("Bearer t1");
    }

    @Test
    void expiredSessionIsRenewedOnceAndTheRequestRepeated() throws Exception {
        client.login(base + "/login", "marko", "right");
        issued.incrementAndGet(); // the server moves on: t1 no longer works

        RestClient.Result result = client.get(base + "/data");

        assertThat(result.ok()).isTrue();
        assertThat(seenAuthorization).containsExactly("Bearer t1", "Bearer t3");
    }

    @Test
    void logoutForgetsTheSession() throws Exception {
        client.login(base + "/login", "marko", "right");
        client.logout();

        assertThat(client.get(base + "/data").status()).isEqualTo(401);
        assertThat(client.isLoggedIn()).isFalse();
    }
}
