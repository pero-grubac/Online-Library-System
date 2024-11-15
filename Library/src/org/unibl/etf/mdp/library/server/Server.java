package org.unibl.etf.mdp.library.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.server.ServerThread;

public class Server implements Runnable {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(Server.class.getName());
	private static final int DISCOVERY_SERVER_TCP = conf.getDiscoveryServerTCPPort();
	private static final String HOST = conf.getDefaultHost();

	private static Server instance; // Singleton instance

	private static final int TCP_PORT = conf.getTCPPort();

	private ServerSocket serverSocket;
	private AtomicBoolean running = new AtomicBoolean(false);

	private Server() {
	}

	// Synchronized method to ensure only one instance is created
	public static synchronized Server getInstance(String serverName, List<BookDto> books) {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	@Override
	public void run() {
		try {

			System.out.println("Library is running on :" + TCP_PORT);

			running.set(true);

			while (running.get()) {
				try {
					Socket sock = serverSocket.accept();
					new ServerThread(sock);
				} catch (Exception e) {
					if (!running.get()) {
						System.out.println("Server is shutting down...");
					} else {
						logger.log(Level.SEVERE, "Error accepting connection", e);
					}
				}
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		} finally {
			cleanup();
		}
	}

	public void shutdown() {
		try {
			running.set(false);
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error during server shutdown", e);
		}
	}

	private void cleanup() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error during cleanup", e);
		}
	}

}
