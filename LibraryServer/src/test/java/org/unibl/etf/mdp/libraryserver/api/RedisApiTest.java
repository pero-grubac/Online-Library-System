package org.unibl.etf.mdp.libraryserver.api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.libraryserver.repository.RedisConnection;
import org.unibl.etf.mdp.libraryserver.support.TestBooks;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.model.ReservationStatus;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Books and reservations over HTTP against a real Redis in Docker; skipped when Docker is not available. */
@Testcontainers(disabledWithoutDocker = true)
class RedisApiTest extends ApiTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static JedisPool pool;

    @BeforeAll
    static void useContainer() {
        pool = new JedisPool(REDIS.getHost(), REDIS.getMappedPort(6379));
        RedisConnection.use(pool);
    }

    @AfterAll
    static void release() {
        RedisConnection.use(null);
        pool.close();
    }

    @BeforeEach
    void emptyRedis() {
        try (Jedis jedis = pool.getResource()) {
            jedis.flushAll();
        }
    }

    private String addBook(String admin, Book book) {
        String json = "{\"title\":\"" + book.getTitle() + "\",\"author\":\"" + book.getAuthor()
                + "\",\"language\":\"English\",\"releaseDate\":\"1897-05-26\",\"price\":10,\"content\":\"*** START OF THE PROJECT GUTENBERG EBOOK X ***\\nline 1\\n\"}";
        assertThat(as(admin, "books").post(Entity.entity(json, MediaType.APPLICATION_JSON)).getStatus()).isEqualTo(201);
        return new Book(book.getTitle(), book.getAuthor(), "English", TestBooks.date("1897-05-26"), 10).getKey();
    }

    private static String path(String key) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    private Response reserve(String member, String key) {
        return as(member, "reservations").post(Entity.entity("{\"bookKey\":\"" + key + "\"}", MediaType.APPLICATION_JSON));
    }

    private Response decide(String admin, long id, String status) {
        return target("reservations/" + id).queryParam("status", status).request()
                .header("Authorization", "Bearer " + admin).method("PATCH", Entity.text(""));
    }

    @Test
    void singleBookUsesTheSameDateFormatAsTheList() {
        String admin = adminToken();
        String key = addBook(admin, TestBooks.book("Dracula", "Bram Stoker"));

        String body = as(admin, "books/" + path(key)).get(String.class);

        assertThat(body).contains("\"releaseDate\":\"1897-05-26\"");
    }

    @Test
    void librarianEditsABook() {
        String admin = adminToken();
        String key = addBook(admin, TestBooks.book("Dracula", "Bram Stoker"));

        Response response = as(admin, "books/" + path(key)).put(Entity.entity(
                "{\"title\":\"Dracula\",\"author\":\"Bram Stoker\",\"language\":\"English\",\"releaseDate\":\"1897-05-26\",\"price\":25}",
                MediaType.APPLICATION_JSON));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(as(admin, "books/" + path(key)).get(String.class)).contains("\"price\":25").contains("line 1");
        assertThat(as(admin, "books/" + path("missing")).put(Entity.entity(
                "{\"title\":\"X\",\"author\":\"Y\",\"language\":\"Z\"}", MediaType.APPLICATION_JSON)).getStatus())
                .isEqualTo(404);
    }

    @Test
    void memberReservesAndTheLibraryDecides() {
        String admin = adminToken();
        String key = addBook(admin, TestBooks.book("Dracula", "Bram Stoker"));
        String marko = approvedMember("marko");

        Response created = reserve(marko, key);
        assertThat(created.getStatus()).isEqualTo(201);
        Reservation reservation = Json.fromJson(created.readEntity(String.class), Reservation.class);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getBookTitle()).isEqualTo("Dracula");
        assertThat(reservation.getUsername()).isEqualTo("marko");

        List<Reservation> all = Json.fromJson(as(admin, "reservations").get(String.class), Json.listOf(Reservation.class));
        assertThat(all).extracting(Reservation::getId).containsExactly(reservation.getId());

        assertThat(decide(admin, reservation.getId(), "APPROVED").getStatus()).isEqualTo(200);
        // Decided once
        assertThat(decide(admin, reservation.getId(), "REJECTED").getStatus()).isEqualTo(409);

        List<Reservation> mine = Json.fromJson(as(marko, "reservations/mine").get(String.class),
                Json.listOf(Reservation.class));
        assertThat(mine).singleElement().extracting(Reservation::getStatus).isEqualTo(ReservationStatus.APPROVED);
    }

    @Test
    void reservationRules() {
        String admin = adminToken();
        String key = addBook(admin, TestBooks.book("Emma", "Jane Austen"));
        String marko = approvedMember("marko");
        String ana = approvedMember("ana");

        assertThat(reserve(marko, key).getStatus()).isEqualTo(201);
        assertThat(reserve(marko, key).getStatus()).isEqualTo(409);
        assertThat(reserve(ana, key).getStatus()).isEqualTo(201);
        assertThat(reserve(marko, "no-such-book").getStatus()).isEqualTo(404);
        assertThat(as(marko, "reservations").post(Entity.entity("{not json", MediaType.APPLICATION_JSON)).getStatus())
                .isEqualTo(400);

        // Each member sees only their own
        assertThat(Json.<List<Reservation>>fromJson(as(ana, "reservations/mine").get(String.class),
                Json.listOf(Reservation.class))).extracting(Reservation::getUsername).containsOnly("ana");
        assertThat(decide(admin, 999, "APPROVED").getStatus()).isEqualTo(404);
        assertThat(decide(admin, 1, "MAYBE").getStatus()).isEqualTo(400);
        assertThat(decide(admin, 1, "PENDING").getStatus()).isEqualTo(400);
    }
}
