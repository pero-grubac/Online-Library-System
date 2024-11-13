package org.unibl.etf.mdp.accountingserver.properties;

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
				.getResourceAsStream("org/unibl/etf/mdp/accountingserver/properties/app.properties")) {
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

	public String getSecurityDir() {
		return getProperty("SECURITY_DIR");
	}

	public String getRegistryName() {
		return getProperty("REGISTRY_NAME");
	}

	public int getRegistryPort() {
		return getIntegerProperty("REGISTRY_PORT");
	}

	public String getSecurityFile() {
		return getProperty("SECURITY_FILE");
	}

	public String getSecurityPolicy() {
		return getProperty("SECURITY_POLICY");
	}

	public String getLogDir() {
		return getProperty("LOG_DIR");
	}

	public String getLogFile() {
		return getProperty("LOG_FILE");
	}
}
