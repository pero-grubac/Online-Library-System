package org.unibl.etf.mdp.libraryserver.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/** Who may call what: no token gives 401, a member on a staff endpoint gets 403. */
class AuthorizationTest extends ApiTest {

    private Response call(String token, String method, String path) {
        Entity<String> body = "GET".equals(method) || "DELETE".equals(method) ? null
                : Entity.entity("{}", "PATCH".equals(method) ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_JSON);
        return as(token, path).method(method, body);
    }

    @ParameterizedTest(name = "{0} /{1} without a token is 401")
    @CsvSource({
            "GET, users", "PUT, users", "DELETE, users/john", "PATCH, users/john/status",
            "GET, books", "GET, books/some-key", "POST, books", "PUT, books/some-key", "DELETE, books/some-key",
            "PUT, books/email/john", "POST, reservations", "GET, reservations", "GET, reservations/mine",
            "PATCH, reservations/1"})
    void protectedEndpointsNeedAToken(String method, String path) {
        Response response = call(null, method, path);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");
    }

    @ParameterizedTest(name = "member: {0} /{1} is 403")
    @CsvSource({"GET, users", "PUT, users", "DELETE, users/john", "PATCH, users/john/status", "POST, books",
            "PUT, books/some-key", "DELETE, books/some-key", "GET, reservations", "PATCH, reservations/1"})
    void membersCannotUseStaffEndpoints(String method, String path) {
        assertThat(call(approvedMember("marko"), method, path).getStatus()).isEqualTo(403);
    }

    @Test
    void forgedTokenIsRejected() {
        assertThat(call("eyJhbGciOiJub25lIn0.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJhZG1pbiJ9.", "GET", "users").getStatus())
                .isEqualTo(401);
        assertThat(call("garbage", "GET", "users").getStatus()).isEqualTo(401);
    }

    @Test
    void approvedMemberCanBrowseTheCatalogueEndpoint() {
        // 200 with Redis, 500 without it; either way the request got past authentication
        assertThat(call(approvedMember("marko"), "GET", "books").getStatus()).isNotIn(401, 403);
    }

    @Test
    void blockedMemberLosesAccessImmediately() {
        String marko = approvedMember("marko");
        assertThat(as(marko, "books/email/marko").put(Entity.json("[]")).getStatus()).isEqualTo(400);

        changeStatusAsAdmin("marko", "BLOCKED");

        assertThat(as(marko, "books/email/marko").put(Entity.json("[]")).getStatus()).isEqualTo(401);
    }

    @Test
    void registrationAndLoginArePublic() {
        assertThat(post("users", member("ana")).getStatus()).isEqualTo(201);
        assertThat(post("users/login", credentials("ana", "secret")).getStatus()).isEqualTo(403);
    }
}
