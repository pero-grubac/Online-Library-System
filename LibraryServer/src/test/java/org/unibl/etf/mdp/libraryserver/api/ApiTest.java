package org.unibl.etf.mdp.libraryserver.api;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.unibl.etf.mdp.libraryserver.security.AuthFilter;

import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** The whole REST API with its security filter, served in memory by Jersey, on a fresh member store. */
abstract class ApiTest extends JerseyTest {
    // ADMIN_USERNAME / ADMIN_PASSWORD in src/test/resources
    static final String ADMIN = "admin";
    static final String ADMIN_PASSWORD = "admin-secret";

    @Override
    protected Application configure() {
        return new ResourceConfig(UserController.class, BookController.class, ReservationController.class, AuthFilter.class);
    }

    @BeforeEach
    void start() throws Exception {
        Files.deleteIfExists(Path.of("target", "test-users", "users.xml"));
        super.setUp();
    }

    @AfterEach
    void stop() throws Exception {
        super.tearDown();
    }

    static String credentials(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    static String member(String username) {
        return "{\"firstName\":\"Test\",\"lastName\":\"User\",\"address\":\"Street 1\",\"email\":\"" + username
                + "@example.com\",\"username\":\"" + username + "\",\"password\":\"secret\"}";
    }

    Response post(String path, String json) {
        return target(path).request().post(Entity.entity(json, MediaType.APPLICATION_JSON));
    }

    Invocation.Builder as(String token, String path) {
        Invocation.Builder request = target(path).request();
        return token == null ? request : request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> loginBody(String username, String password) {
        Response response = post("users/login", credentials(username, password));
        if (response.getStatus() != 200) {
            throw new AssertionError("Login failed with " + response.getStatus());
        }
        return JsonbBuilder.create().fromJson(response.readEntity(String.class), Map.class);
    }

    String login(String username, String password) {
        return (String) loginBody(username, password).get("token");
    }

    String adminToken() {
        return login(ADMIN, ADMIN_PASSWORD);
    }

    Response changeStatus(String token, String username, String status) {
        Invocation.Builder request = target("users/" + username + "/status").queryParam("status", status).request();
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return request.method("PATCH", Entity.text(""));
    }

    Response changeStatusAsAdmin(String username, String status) {
        return changeStatus(adminToken(), username, status);
    }

    /** Registers a member, has the library approve them and logs them in. */
    String approvedMember(String username) {
        post("users", member(username));
        changeStatusAsAdmin(username, "APPROVED");
        return login(username, "secret");
    }
}
