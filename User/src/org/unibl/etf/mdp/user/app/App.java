package org.unibl.etf.mdp.user.app;

import org.unibl.etf.mdp.user.gui.LoginFrame;
import org.unibl.etf.mdp.user.properties.AppConfig;

public class App {
	private static final AppConfig conf = new AppConfig();
	private static final String KEY_STORE_PATH = conf.getKeyStorePath();
	private static final String KEY_STORE_PASSWORD = conf.getKeyStorePass();

	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.trustStore", KEY_STORE_PATH);
		System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);
		System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
		System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);
		new LoginFrame().setVisible(true);

	}

}
