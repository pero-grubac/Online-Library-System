package org.unibl.etf.mdp.library.properties;

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
                .getResourceAsStream("org/unibl/etf/mdp/library/properties/app.properties")) {
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

    private String getProperty(String key) {
        return properties.getProperty(key);
    }

    private Integer getIntegerProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public String getLogDir() {
        return getProperty("LOG_DIR");
    }

    public String getDtoMsg() {
        return getProperty("GET_DTO");
    }

    public String getLogFile() {
        return getProperty("LOG_FILE");
    }

    public String getEndMsg() {
        return getProperty("END_MSG");
    }

    public int getDiscoveryServerTCPPort() {
        return getIntegerProperty("DISCOVERY_SERVER_TCP_PORT");
    }

    public String getDiscoverAllMsg() {
        return getProperty("DISCOVER_ALL");
    }

    public String getRequestMsg() {
        return getProperty("REQUEST");
    }

    public String getApproveMsg() {
        return getProperty("APPROVE");
    }

    public String getDenialMsg() {
        return getProperty("DENIAL");
    }

    public String getOkMsg() {
        return getProperty("OK");
    }

    public String getDefaultHost() {
        return getProperty("HOST");
    }

    public String getRabbitMQUser() {
        return getProperty("RABBITMQ_USER");
    }

    public String getRabbitMQPass() {
        return getProperty("RABBITMQ_PASS");
    }

    public String getExchangeName() {
        return getProperty("EXCHANGE_NAME");
    }

    public String getDirect() {
        return getProperty("DIRECT");
    }

    public int getTCPPort() {
        return getIntegerProperty("TCP_PORT");
    }

    public String getInvoiceMsg() {
        return getProperty("INVOICE");
    }

    public String getServerUsersUrl() {
        return getProperty("USERS_URL");
    }

    public String getServerBooksUrl() {
        return getProperty("BOOKS_URL");
    }

    public String getStatus() {
        return getProperty("BOOKS_STATUS");
    }

    public String getGroupMessage() {
        return getProperty("GROUP_MESSAGE");
    }

    public String getMulticastAdd() {
        return getProperty("MULTICAST_ADD");
    }

    public int getChatUsersPort() {
        return getIntegerProperty("USERS_PORT");
    }

    public String getQueuePrefix() {
        return getProperty("QUEUE_PREFIX");
    }

    public int getRabbitMQPort() {
        return getIntegerProperty("RABBITMQ_PORT");
    }

    // Staff account on the LibraryServer; LIBRARY_ADMIN_USERNAME / LIBRARY_ADMIN_PASSWORD override the file
    public String getAdminUsername() {
        return fromEnv("LIBRARY_ADMIN_USERNAME", getProperty("ADMIN_USERNAME"));
    }

    public String getAdminPassword() {
        return fromEnv("LIBRARY_ADMIN_PASSWORD", getProperty("ADMIN_PASSWORD"));
    }

    private static String fromEnv(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    public String getLibraryName() {
        return getProperty("LIBRARY_NAME");
    }

    public String getProposalMsg() {
        return getProperty("PROPOSAL");
    }

    public String getProposalAnswerMsg() {
        return getProperty("PROPOSAL_ANSWER");
    }

    public String getReservationsUrl() {
        return getProperty("RESERVATIONS_URL");
    }
}
