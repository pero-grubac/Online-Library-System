package org.unibl.etf.mdp.libraryserver.service;

import org.unibl.etf.mdp.libraryserver.mock.MockUsers;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.libraryserver.repository.UserRepository;
import org.unibl.etf.mdp.libraryserver.security.PasswordHasher;
import org.unibl.etf.mdp.libraryserver.security.Roles;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService {
    private static final AppConfig conf = new AppConfig();
    private final UserRepository repository = UserRepository.getInstance();
    private final PasswordHasher hasher = new PasswordHasher(conf.getPasswordHashIterations());

    public UserService() {
        initMockData();
    }

    public void initMockData() {
        repository.modify(users -> {
            if (users.isEmpty()) {
                for (User user : MockUsers.mockData()) {
                    insert(users, user);
                }
            }
            return null;
        });
    }

    /** @return the new member, or null when the username is taken (the staff account's name included) */
    public UserDto add(User user) {
        return repository.modify(users -> insert(users, user));
    }

    private UserDto insert(List<User> users, User user) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))
                || user.getUsername().equalsIgnoreCase(conf.getAdminUsername())) {
            return null;
        }
        user.setId(users.isEmpty() ? 1 : users.stream().mapToInt(User::getId).max().orElse(0) + 1);
        user.setStatus(StatusEnum.PENDING);
        user.setPassword(hasher.hash(user.getPassword()));
        users.add(user);
        return new UserDto(user);
    }

    public List<UserDto> getAll() {
        return repository.findAll().stream().map(UserDto::new).collect(Collectors.toList());
    }

    public boolean approver(String username) {
        return changeUserStatus(username, StatusEnum.APPROVED);
    }

    public boolean reject(String username) {
        return changeUserStatus(username, StatusEnum.REJECTED);
    }

    public boolean block(String username) {
        return changeUserStatus(username, StatusEnum.BLOCKED);
    }

    private boolean changeUserStatus(String username, StatusEnum status) {
        return repository.modify(users -> {
            User user = find(users, username);
            if (user == null) {
                return false;
            }
            user.setStatus(status);
            return true;
        });
    }

    public boolean delete(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return repository.modify(users -> users.removeIf(u -> u.getUsername().equals(username)));
    }

    public boolean update(UserDto userDto) {
        if (userDto == null || isBlank(userDto.getUsername()) || isBlank(userDto.getFirstName())
                || isBlank(userDto.getLastName()) || isBlank(userDto.getAddress()) || isBlank(userDto.getEmail())
                || userDto.getStatus() == null) {
            return false;
        }
        return repository.modify(users -> {
            User user = find(users, userDto.getUsername());
            if (user == null) {
                return false;
            }
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setAddress(userDto.getAddress());
            user.setEmail(userDto.getEmail());
            user.setStatus(userDto.getStatus());
            return true;
        });
    }

    private static User find(List<User> users, String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** @return the role to put in the session token, or empty when the login is refused */
    public Optional<String> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        if (username.equalsIgnoreCase(conf.getAdminUsername())) {
            String adminPassword = conf.getAdminPassword();
            boolean ok = adminPassword != null && !adminPassword.isEmpty() && MessageDigest.isEqual(
                    adminPassword.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8));
            return ok ? Optional.of(Roles.ADMIN) : Optional.empty();
        }
        return accountStatus(username, password).filter(s -> s == StatusEnum.APPROVED).map(s -> Roles.MEMBER);
    }

    public boolean login(String username, String password) {
        return accountStatus(username, password).filter(s -> s == StatusEnum.APPROVED).isPresent();
    }

    /**
     * The status of the account, but only for the right password, so it tells a member whose account is still
     * pending or blocked why they cannot log in without revealing anything to someone who guesses.
     */
    public Optional<StatusEnum> accountStatus(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        User user = repository.findByUsername(username).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        if (PasswordHasher.isHash(user.getPassword())) {
            return hasher.verify(password, user.getPassword()) ? Optional.of(user.getStatus()) : Optional.empty();
        }
        if (!MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                String.valueOf(user.getPassword()).getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        // Saved before hashing existed: accepted once and stored hashed from now on
        String hash = hasher.hash(password);
        repository.modify(users -> {
            User stored = find(users, username);
            if (stored != null && !PasswordHasher.isHash(stored.getPassword())) {
                stored.setPassword(hash);
            }
            return null;
        });
        return Optional.of(user.getStatus());
    }

    public UserDto getByUsername(String username) {
        return repository.findByUsername(username).map(UserDto::new).orElse(null);
    }
}
