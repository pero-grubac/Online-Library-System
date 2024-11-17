package org.unibl.etf.mdp.supplier.properties;

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
				.getResourceAsStream("org/unibl/etf/mdp/supplier/properties/app.properties")) {
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

	public String getSecurityDir() {
		return getProperty("SECURITY_DIR");
	}

	public String getSecurityFile() {
		return getProperty("SECURITY_FILE");
	}

	public String getRegistryName() {
		return getProperty("REGISTRY_NAME");
	}

	public int getRegistryPort() {
		return getIntegerProperty("REGISTRY_PORT");
	}

	public String getSecurityPolicy() {
		return getProperty("SECURITY_POLICY");
	}

	public String getSuppliersDir() {
		return getProperty("SUPPLIERS_DIR");
	}

	public String getLinksFile() {
		return getProperty("LINKS_FILE");
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
}
