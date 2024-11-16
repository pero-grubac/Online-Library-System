package org.unibl.etf.mdp.supplierserver.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
				System.out.println("Sorry, unable to find app.properties");
				return;
			}
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String getProperty(String key) {
		return properties.getProperty(key);
	}

	private double getDoubleProperty(String key) {
		return Double.parseDouble(properties.getProperty(key));
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

	public int getRedisPort() {
		return getIntegerProperty("REDIS_PORT");
	}

	public String getBookStartUrl() {
		return getProperty("BOOK_URL_START");
	}

	public String getRedisHost() {
		return getProperty("REDIS_HOST");
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

	public String getDefaultHost() {
		return getProperty("HOST");
	}

	public int getPreviewLines() {
		return getIntegerProperty("PREVIEW_LINES");
	}

	public String getStartMarker() {
		return getProperty("START_MARKER");
	}
}
