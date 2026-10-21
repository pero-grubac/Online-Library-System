package org.unibl.etf.mdp.libraryserver.repository;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.User;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Members in users.xml. All access goes through one lock, so concurrent requests never lose an update. */
public class UserRepository {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(UserRepository.class.getName());
    private static final Path FILE = Paths.get(conf.getUsersFolder(), conf.getUsersFile());

    private static UserRepository instance = null;

    private UserRepository() {
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    /** Reads the members, lets {@code change} edit the list and saves it, as one step. */
    public synchronized <R> R modify(Function<List<User>, R> change) {
        List<User> users = findAll();
        R result = change.apply(users);
        saveAll(users);
        return result;
    }

    public synchronized void saveAll(List<User> users) {
        try {
            Files.createDirectories(FILE.toAbsolutePath().getParent());
            // Written next to the real file and then swapped in, so a crash never leaves half a file
            Path temp = Files.createTempFile(FILE.toAbsolutePath().getParent(), "users", ".tmp");
            try (OutputStream out = Files.newOutputStream(temp); XMLEncoder encoder = new XMLEncoder(out)) {
                encoder.writeObject(new ArrayList<>(users));
            }
            replace(temp);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while saving users: " + e.getMessage(), e);
            throw new IllegalStateException("Could not save the members", e);
        }
    }

    // On Windows a virus scanner or the indexer can hold users.xml for a moment; the swap is tried again
    private static void replace(Path temp) throws IOException {
        for (int attempt = 1; ; attempt++) {
            try {
                Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                return;
            } catch (AccessDeniedException e) {
                if (attempt == 10) {
                    Files.deleteIfExists(temp);
                    throw e;
                }
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    Files.deleteIfExists(temp);
                    throw e;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized List<User> findAll() {
        try {
            if (Files.notExists(FILE) || Files.size(FILE) == 0) {
                return new ArrayList<>();
            }
            try (InputStream in = new BufferedInputStream(Files.newInputStream(FILE)); XMLDecoder decoder = new XMLDecoder(in)) {
                return new ArrayList<>((List<User>) decoder.readObject());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while reading users: " + e.getMessage(), e);
            throw new IllegalStateException("Could not read the members", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        return findAll().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }
}
