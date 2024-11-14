package org.unibl.etf.mdp.supplier.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class Server {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(Server.class.getName());
	private static final int DISCOVERY_SERVER_TCP = conf.getDiscoveryServerTCPPort();
	
	private String serverName;
	private int serverPort;

	public Server(String servername) {
		this.serverName = servername;
			try {
				ServerSocket ss = new ServerSocket(0);
				this.serverPort = ss.getLocalPort();
				System.out.println(servername + " server on port: " + serverPort);

				while (true) {
					Socket sock = ss.accept();
	                new ServerThread(sock);
				}
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "An error occurred in the server application", ex);
			}
		
	}
}
