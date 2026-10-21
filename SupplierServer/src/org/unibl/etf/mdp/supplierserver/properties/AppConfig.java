package org.unibl.etf.mdp.supplierserver.properties;

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
                .getResourceAsStream("org/unibl/etf/mdp/supplierserver/properties/app.properties")) {
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

    public int getTCPPort() {
        return getIntegerProperty("TCP_PORT");
    }

    public String getSuppliersDir() {
        return getProperty("SUPPLIERS_DIR");
    }

    public String getBookExt() {
        return getProperty("BOOK_EXT");
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

    public int getPreviewLines() {
        return getIntegerProperty("PREVIEW_LINES");
    }

    public String getStartMarker() {
        return getProperty("START_MARKER");
    }

    public String getImageExt() {
        return getProperty("IMAGE_EXT");
    }

    public String getImageUrlEnd() {
        return getProperty("IMAGE_URL_END");
    }

    public String getLinksFile() {
        return getProperty("LINKS_FILE");
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
}
