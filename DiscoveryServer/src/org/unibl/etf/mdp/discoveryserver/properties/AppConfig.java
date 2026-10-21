package org.unibl.etf.mdp.discoveryserver.properties;

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
                .getResourceAsStream("org/unibl/etf/mdp/discoveryserver/properties/app.properties")) {
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

    public int getTCPPort() {
        return getIntegerProperty("TCP_PORT");
    }

    public String getDiscoveryMsg() {
        return getProperty("DISCOVERY");
    }

    public String getOkMsg() {
        return getProperty("OK");
    }

    public String getLogDir() {
        return getProperty("LOG_DIR");
    }

    public String getLogFile() {
        return getProperty("LOG_FILE");
    }

    public String getEndMsg() {
        return getProperty("END_MSG");
    }

    public String getDiscoverAllMsg() {
        return getProperty("DISCOVER_ALL");
    }

    public String getDiscoverAllUsersMsg() {
        return getProperty("DISCOVER_ALL_USERS");
    }

    public String getDiscoverUserMsg() {
        return getProperty("DISCOVER_USER");
    }


    public String getUnregisterMsg() {
        return getProperty("UNREGISTER");
    }

    public int getRegistrationTtlSeconds() {
        return getIntegerProperty("REGISTRATION_TTL_SECONDS");
    }

    public String getTakenMsg() {
        return getProperty("TAKEN");
    }
}
