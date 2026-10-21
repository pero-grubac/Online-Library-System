package org.unibl.etf.mdp.supplier.properties;

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
                .getResourceAsStream("org/unibl/etf/mdp/supplier/properties/app.properties")) {
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

    public int getSupplierServerTCPPort() {
        return getIntegerProperty("SUPPLIER_SERVER_TCP_PORT");
    }

    public int getDiscoveryServerTCPPort() {
        return getIntegerProperty("DISCOVERY_SERVER_TCP_PORT");
    }

    public int getLibraryTCPPort() {
        return getIntegerProperty("LIBRARY_TCP_PORT");
    }

    public String getLogDir() {
        return getProperty("LOG_DIR");
    }

    public String getLogFile() {
        return getProperty("LOG_FILE");
    }

    public String getRegistryName() {
        return getProperty("REGISTRY_NAME");
    }

    public int getRegistryPort() {
        return getIntegerProperty("REGISTRY_PORT");
    }

    public String getDtoMsg() {
        return getProperty("GET_DTO");
    }

    public String getModelMsg() {
        return getProperty("GET_MODEL");
    }

    public String getEndMsg() {
        return getProperty("END_MSG");
    }

    public String getDiscoveryMsg() {
        return getProperty("DISCOVERY");
    }

    public String getOkMsg() {
        return getProperty("OK");
    }

    public String getApproveMsg() {
        return getProperty("APPROVE");
    }

    public String getDenialMsg() {
        return getProperty("DENIAL");
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

    public String getInvoiceMsg() {
        return getProperty("INVOICE");
    }

    public String getUnregisterMsg() {
        return getProperty("UNREGISTER");
    }

    public int getHeartbeatSeconds() {
        return getIntegerProperty("HEARTBEAT_SECONDS");
    }

    public String getQueuePrefix() {
        return getProperty("QUEUE_PREFIX");
    }

    public int getRabbitMQPort() {
        return getIntegerProperty("RABBITMQ_PORT");
    }

    /** The suppliers this application can log in as (SUPPLIER_NAMES, comma separated). */
    public java.util.List<String> getSupplierNames() {
        return java.util.Arrays.stream(getProperty("SUPPLIER_NAMES").split(",")).map(String::trim)
                .filter(n -> !n.isEmpty()).collect(java.util.stream.Collectors.toList());
    }

    public String getRmiHost() {
        return getProperty("RMI_HOST");
    }

    public int getPriceMin() {
        return getIntegerProperty("PRICE_MIN");
    }

    public int getPriceMax() {
        return getIntegerProperty("PRICE_MAX");
    }

    public String getLoadLinksMsg() {
        return getProperty("LOAD_LINKS");
    }

    public String getAddBookMsg() {
        return getProperty("ADD_BOOK");
    }

    public String getErrorMsg() {
        return getProperty("ERROR");
    }

    public String getTakenMsg() {
        return getProperty("TAKEN");
    }
}
