package org.unibl.etf.mdp.user.properties;

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
                .getResourceAsStream("org/unibl/etf/mdp/user/properties/app.properties")) {
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

    public String getLogFile() {
        return getProperty("LOG_FILE");
    }

    public String getServerUserUrl() {
        return getProperty("USERS_URL");
    }

    public String getServerBooksUrl() {
        return getProperty("BOOKS_URL");
    }

    public String getMulticastAdd() {
        return getProperty("MULTICAST_ADD");
    }

    public int getChatUsersPort() {
        return getIntegerProperty("USERS_PORT");
    }

    public String getDiscoverAllUsersMsg() {
        return getProperty("DISCOVER_ALL_USERS");
    }

    public String getDiscoverUserMsg() {
        return getProperty("DISCOVER_USER");
    }

    public String getDefaultHost() {
        return getProperty("HOST");
    }

    public int getDiscoveryServerTCPPort() {
        return getIntegerProperty("DISCOVERY_SERVER_TCP_PORT");
    }

    public String getEndMsg() {
        return getProperty("END_MSG");
    }

    public String getChatMsg() {
        return getProperty("CHAT_MESSAGE");
    }

    public String getKeyStorePath() {
        return getProperty("KEY_STORE_PATH");
    }

    public String getKeyStorePass() {
        return getProperty("KEY_STORE_PASSWORD");
    }

    public String getOk() {
        return getProperty("OK");
    }

    public String getGroupMessage() {
        return getProperty("GROUP_MESSAGE");
    }

    public String getUnregisterMsg() {
        return getProperty("UNREGISTER");
    }

    public int getHeartbeatSeconds() {
        return getIntegerProperty("HEARTBEAT_SECONDS");
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

    public String getTakenMsg() {
        return getProperty("TAKEN");
    }
}
