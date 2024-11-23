package org.unibl.etf.mdp.libraryserver.properties;

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
				.getResourceAsStream("org/unibl/etf/mdp/libraryserver/properties/app.properties")) {
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

	public String getImageExt() {
		return getProperty("IMAGE_EXT");
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

	public String getImageKeySufix() {
		return getProperty("IMAGE_KEY_SUFIX");
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

	public String getKeyStorePath() {
		return getProperty("KEY_STORE_PATH");
	}

	public String getKeyStorePass() {
		return getProperty("KEY_STORE_PASSWORD");
	}

	public String getZipFile() {
		return getProperty("ZIP_FILE");
	}
}
