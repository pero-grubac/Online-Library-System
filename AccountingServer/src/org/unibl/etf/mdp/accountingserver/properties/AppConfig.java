package org.unibl.etf.mdp.accountingserver.properties;

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
                .getResourceAsStream("org/unibl/etf/mdp/accountingserver/properties/app.properties")) {
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

    public String getRegistryName() {
        return getProperty("REGISTRY_NAME");
    }

    public int getRegistryPort() {
        return getIntegerProperty("REGISTRY_PORT");
    }

    public String getLogDir() {
        return getProperty("LOG_DIR");
    }

    public String getLogFile() {
        return getProperty("LOG_FILE");
    }

    public String getInvoicesDir() {
        return getProperty("INVOICES_DIR");
    }

    public String getInvoiceDatePattern() {
        return getProperty("INVOICE_DATE_PATTERN");
    }

    public String getVatRate() {
        return getProperty("VAT_RATE");
    }
}
