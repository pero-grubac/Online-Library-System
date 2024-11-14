package org.unibl.etf.mdp.discoveryserver.app;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.discoveryserver.logger.FileLogger;
import org.unibl.etf.mdp.discoveryserver.properties.AppConfig;
import org.unibl.etf.mdp.discoveryserver.server.ServerThread;

public class App {
	public static final AppConfig conf = new AppConfig();
	public static final int TCP_PORT = conf.getTCPPort();
	private static final Logger logger = FileLogger.getLogger(App.class.getName());
	
	public static void main(String[] args) {
		try {
			System.out.println("Discovery server");
			ServerSocket ss = new ServerSocket(TCP_PORT);
			while (true) {
				Socket sock = ss.accept();
				new ServerThread(sock);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		}
	}
}
