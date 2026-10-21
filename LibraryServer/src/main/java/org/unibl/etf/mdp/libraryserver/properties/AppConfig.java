package org.unibl.etf.mdp.libraryserver.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppConfig {
    private Properties properties;

    public AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("org/unibl/etf/mdp/libraryserver/properties/app.properties")) {
            if (input == null) {
                throw new IllegalStateException("app.properties is missing from the classpath");
            }
            properties.load(input);
        } catch (IOException ex) {
            // Without its configuration the application cannot work at all
            Logger.getLogger(AppConfig.class.getName()).log(Level.SEVERE, "Cannot read app.properties", ex);
            throw new IllegalStateException("Cannot read app.properties", ex);
        }
    }

    // An environment variable with the same name wins, so secrets (MAIL_PASSWORD...) stay out of the file
    private String getProperty(String key) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return properties.getProperty(key);
    }

    private Integer getIntegerProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public String getBookExt() {
        return getProperty("BOOK_EXT");
    }

    public String getZipFolder() {
        return getProperty("ZIP_FOLDER");
    }

    public String getLogDir() {
        return getProperty("LOG_DIR");
    }

    public String getLogFile() {
        return getProperty("LOG_FILE");
    }

    public String getUsersFolder() {
        return getProperty("USERS_FOLDER");
    }

    public String getUsersFile() {
        return getProperty("USERS_FILE");
    }

    public int getRedisPort() {
        return getIntegerProperty("REDIS_PORT");
    }

    public String getRedisHost() {
        return getProperty("REDIS_HOST");
    }

    public String getBookKeyPrefix() {
        return getProperty("BOOK_KEY_PREFIX");
    }

    public int getPreviewLines() {
        return getIntegerProperty("PREVIEW_LINES");
    }

    public String getStartMarker() {
        return getProperty("START_MARKER");
    }

    public int getMailPort() {
        return getIntegerProperty("MAIL_PORT");
    }

    public String getMailHost() {
        return getProperty("MAIL_HOST");
    }

    public String getMailUser() {
        return getProperty("MAIL_USERNAME");
    }

    public String getMailPass() {
        return getProperty("MAIL_PASSWORD");
    }

    public String getAdminUsername() {
        return getProperty("ADMIN_USERNAME");
    }

    public String getAdminPassword() {
        return getProperty("ADMIN_PASSWORD");
    }

    public String getJwtSecret() {
        return getProperty("JWT_SECRET");
    }

    public int getTokenTtlMinutes() {
        return getIntegerProperty("TOKEN_TTL_MINUTES");
    }

    public int getPasswordHashIterations() {
        return getIntegerProperty("PASSWORD_HASH_ITERATIONS");
    }

    public String getReservationKeyPrefix() {
        return getProperty("RESERVATION_KEY_PREFIX");
    }
}
