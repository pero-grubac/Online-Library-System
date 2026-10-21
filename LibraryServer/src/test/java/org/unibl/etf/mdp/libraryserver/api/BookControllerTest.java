package org.unibl.etf.mdp.libraryserver.api;

import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

/** Request validation in /books; every case here is answered before Redis or e-mail is used. */
class BookControllerTest extends ApiTest {

    private int putJson(String token, String path, String json) {
        return as(token, path).put(Entity.entity(json, MediaType.APPLICATION_JSON)).getStatus();
    }

    @Test
    void addingABookWithoutAuthorIsABadRequest() {
        int status = as(adminToken(), "books")
                .post(Entity.entity("{\"title\":\"Emma\",\"language\":\"English\"}", MediaType.APPLICATION_JSON))
                .getStatus();

        assertThat(status).isEqualTo(400);
    }

    @Test
    void emailingAnEmptyListIsABadRequest() {
        assertThat(putJson(adminToken(), "books/email/john", "[]")).isEqualTo(400);
    }

    @Test
    void emailingMalformedJsonIsABadRequest() {
        assertThat(putJson(adminToken(), "books/email/john", "{not json")).isEqualTo(400);
    }

    @Test
    void emailingAnUnknownUserIsNotFound() {
        assertThat(putJson(adminToken(), "books/email/nobody", "[{\"title\":\"Emma\"}]")).isEqualTo(404);
    }

    @Test
    void emailWithoutAConfiguredMailboxIsUnavailableNotSuccess() {
        // MAIL_USERNAME / MAIL_PASSWORD are empty in the test configuration
        assertThat(putJson(adminToken(), "books/email/john", "[{\"title\":\"Emma\"}]")).isEqualTo(503);
    }

    @Test
    void editWithoutTitleIsABadRequest() {
        assertThat(putJson(adminToken(), "books/some-key", "{\"author\":\"A\",\"language\":\"English\"}"))
                .isEqualTo(400);
    }

    @Test
    void memberMaySendBooksOnlyToThemselves() {
        String marko = approvedMember("marko");

        assertThat(putJson(marko, "books/email/john", "[{\"title\":\"Emma\"}]")).isEqualTo(403);
        // Passes the ownership check and fails on validation instead
        assertThat(putJson(marko, "books/email/marko", "[]")).isEqualTo(400);
    }
}
