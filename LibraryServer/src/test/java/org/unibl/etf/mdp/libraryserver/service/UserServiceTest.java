package org.unibl.etf.mdp.libraryserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unibl.etf.mdp.libraryserver.repository.UserRepository;
import org.unibl.etf.mdp.libraryserver.security.PasswordHasher;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {
    // Matches USERS_FOLDER / USERS_FILE in src/test/resources
    static final Path USERS_FILE = Path.of("target", "test-users", "users.xml");

    private UserService service;

    @BeforeEach
    void freshUserStore() throws Exception {
        Files.deleteIfExists(USERS_FILE);
        service = new UserService();
    }

    private static User newUser(String username) {
        return new User("Test", "User", "Street 1", username + "@example.com", username, "secret");
    }

    @Test
    void emptyStoreIsSeededWithDemoUsers() {
        assertThat(service.getAll()).hasSize(10).extracting(UserDto::getUsername).contains("john", "jane");
    }

    @Test
    void newUserGetsNextIdAndWaitsForApproval() {
        UserDto created = service.add(newUser("marko"));

        assertThat(created.getId()).isEqualTo(11);
        assertThat(created.getStatus()).isEqualTo(StatusEnum.PENDING);
        assertThat(service.getByUsername("marko")).isEqualTo(created);
    }

    @Test
    void usernameMustBeUnique() {
        service.add(newUser("marko"));

        assertThat(service.add(newUser("marko"))).isNull();
        assertThat(service.getAll()).hasSize(11);
    }

    @Test
    void dtoNeverExposesThePassword() {
        UserDto dto = service.add(newUser("marko"));

        assertThat(dto.toString()).doesNotContain("secret");
    }

    @Test
    void onlyApprovedUsersCanLogIn() {
        service.add(newUser("marko"));
        assertThat(service.login("marko", "secret")).isFalse();

        assertThat(service.approver("marko")).isTrue();
        assertThat(service.login("marko", "secret")).isTrue();
        assertThat(service.login("marko", "wrong")).isFalse();

        assertThat(service.block("marko")).isTrue();
        assertThat(service.login("marko", "secret")).isFalse();
    }

    @Test
    void statusChangeOfUnknownUserFails() {
        assertThat(service.reject("nobody")).isFalse();
    }

    @Test
    void updateChangesProfileButRejectsIncompleteData() {
        UserDto dto = service.getByUsername("john");
        dto.setAddress("New Address 5");
        dto.setStatus(StatusEnum.APPROVED);

        assertThat(service.update(dto)).isTrue();
        assertThat(service.getByUsername("john").getAddress()).isEqualTo("New Address 5");

        dto.setEmail(" ");
        assertThat(service.update(dto)).isFalse();
        assertThat(service.update(null)).isFalse();
    }

    @Test
    void deleteRemovesTheUser() {
        assertThat(service.delete("john")).isTrue();
        assertThat(service.getByUsername("john")).isNull();
        assertThat(service.delete("john")).isFalse();
        assertThat(service.delete(" ")).isFalse();
    }

    @Test
    void usersSurviveARestart() {
        service.add(newUser("marko"));

        assertThat(new UserService().getByUsername("marko")).isNotNull();
    }

    @Test
    void passwordsAreStoredOnlyAsHashes() throws Exception {
        service.add(newUser("marko"));

        String stored = UserRepository.getInstance().findByUsername("marko").orElseThrow().getPassword();
        assertThat(PasswordHasher.isHash(stored)).isTrue();
        assertThat(Files.readString(USERS_FILE)).doesNotContain(">secret<");
    }

    @Test
    void plainTextPasswordFromAnOldStoreIsUpgradedOnLogin() {
        UserRepository repository = UserRepository.getInstance();
        List<User> users = repository.findAll();
        User old = newUser("legacy");
        old.setId(99);
        old.setStatus(StatusEnum.APPROVED);
        users.add(old); // saved as-is, like before hashing existed
        repository.saveAll(users);

        assertThat(service.login("legacy", "wrong")).isFalse();
        assertThat(service.login("legacy", "secret")).isTrue();

        assertThat(PasswordHasher.isHash(repository.findByUsername("legacy").orElseThrow().getPassword())).isTrue();
        assertThat(service.login("legacy", "secret")).isTrue();
    }

    @Test
    void authenticationTellsStaffFromMembers() {
        service.add(newUser("marko"));
        service.approver("marko");

        assertThat(service.authenticate("admin", "admin-secret")).contains("admin");
        assertThat(service.authenticate("marko", "secret")).contains("member");
        assertThat(service.authenticate("admin", "secret")).isEmpty();
        assertThat(service.authenticate(null, null)).isEmpty();
    }
}
