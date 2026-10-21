package org.unibl.etf.mdp.libraryserver.api;

import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.model.UserDto;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The /users API over HTTP. */
class UserControllerTest extends ApiTest {

    @Test
    void listsUsersWithoutPasswords() {
        Response response = as(adminToken(), "users").accept(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).contains("\"username\":\"john\"").doesNotContain("password");
    }

    @Test
    void registrationReturns201AndThen409ForTheSameUsername() {
        Response created = post("users", member("marko"));
        assertThat(created.getStatus()).isEqualTo(201);
        assertThat(created.readEntity(UserDto.class).getUsername()).isEqualTo("marko");

        assertThat(post("users", member("marko")).getStatus()).isEqualTo(409);
    }

    @Test
    void nobodyCanRegisterAsTheStaffAccount() {
        assertThat(post("users", member("admin")).getStatus()).isEqualTo(409);
        assertThat(post("users", member("ADMIN")).getStatus()).isEqualTo(409);
    }

    @Test
    void registrationNeedsEveryField() {
        assertThat(post("users", "{\"username\":\"x\",\"email\":\"x@example.com\"}").getStatus()).isEqualTo(400);
        assertThat(post("users", member("ana").replace("\"address\":\"Street 1\"", "\"address\":\" \""))
                .getStatus()).isEqualTo(400);
    }

    @Test
    void blockedMemberIsToldWhyTheyCannotLogIn() {
        approvedMember("marko");
        changeStatusAsAdmin("marko", "BLOCKED");

        Response response = post("users/login", credentials("marko", "secret"));
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.readEntity(String.class)).contains("BLOCKED");
    }

    @Test
    void memberCanLogInOnlyAfterTheLibraryApprovesThem() {
        post("users", member("marko"));
        Response pending = post("users/login", credentials("marko", "secret"));
        assertThat(pending.getStatus()).isEqualTo(403);
        assertThat(pending.readEntity(String.class)).contains("PENDING");
        // A wrong password reveals nothing about the account
        assertThat(post("users/login", credentials("marko", "wrong")).getStatus()).isEqualTo(401);

        assertThat(changeStatusAsAdmin("marko", "APPROVED").getStatus()).isEqualTo(200);

        Map<String, Object> session = loginBody("marko", "secret");
        assertThat(session).containsEntry("role", "member").containsEntry("username", "marko")
                .containsEntry("tokenType", "Bearer");
        assertThat((String) session.get("token")).isNotBlank();
    }

    @Test
    void staffLogsInWithTheConfiguredPassword() {
        assertThat(loginBody(ADMIN, ADMIN_PASSWORD)).containsEntry("role", "admin");
        assertThat(post("users/login", credentials(ADMIN, "wrong")).getStatus()).isEqualTo(401);
    }

    @Test
    void unknownStatusIsRejected() {
        assertThat(changeStatusAsAdmin("john", "ADMIN").getStatus()).isEqualTo(400);
    }

    @Test
    void deleteReturns404ForUnknownUser() {
        String admin = adminToken();
        assertThat(as(admin, "users/john").delete().getStatus()).isEqualTo(200);
        assertThat(as(admin, "users/john").delete().getStatus()).isEqualTo(404);

        List<UserDto> users = as(admin, "users").get(new GenericType<List<UserDto>>() {
        });
        assertThat(users).extracting(UserDto::getUsername).doesNotContain("john");
    }
}
