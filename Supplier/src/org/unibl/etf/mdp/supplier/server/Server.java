package org.unibl.etf.mdp.supplier.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class Server implements Runnable {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(Server.class.getName());
	private static final int DISCOVERY_SERVER_TCP = conf.getDiscoveryServerTCPPort();
	private static final String HOST = conf.getDefaultHost();

	private static Server instance; // Singleton instance

	private String serverName;
	private int serverPort;
	private List<BookDto> books;

	private ServerSocket serverSocket;
	private AtomicBoolean running = new AtomicBoolean(false);

	private Server(String serverName, List<BookDto> books) { // Private constructor
		this.serverName = serverName;
		this.books = books;
	}

	// Synchronized method to ensure only one instance is created
	public static synchronized Server getInstance(String serverName, List<BookDto> books) {
		if (instance == null) {
			instance = new Server(serverName, books);
		}
		return instance;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(0);
			this.serverPort = serverSocket.getLocalPort();
			System.out.println(serverName + " server running on port: " + serverPort);

			if (!sendDiscoveryMessage()) {
				System.out.println("Failed to register with Discovery Server. Exiting...");
				return;
			}

			running.set(true);

			while (running.get()) {
				try {
					Socket sock = serverSocket.accept();
					new ServerThread(sock, serverName, books);
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
			instance = null;
			System.out.println(serverName + " server has been stopped.");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error during server shutdown", e);
		}
	}

	private boolean sendDiscoveryMessage() {
		String discoveryMsg = conf.getDiscoveryMsg();
		String okMsg = conf.getOkMsg();
		String endMsg = conf.getEndMsg();

		Message msg = new Message(discoveryMsg, serverName, String.valueOf(serverPort));

		try (Socket discoverySocket = new Socket(InetAddress.getByName(HOST), DISCOVERY_SERVER_TCP);
				ObjectOutputStream out = new ObjectOutputStream(discoverySocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(discoverySocket.getInputStream())) {

			out.writeObject(msg);
			out.flush();

			Message response = (Message) in.readObject();
			if (okMsg.equals(response.getType())) {
				System.out.println("Received acknowledgment from Discovery Server: " + response);
				msg = new Message(endMsg, serverName);
				out.writeObject(msg);
				out.flush();
				return true;
			} else {
				System.out.println("Unexpected response from Discovery Server: " + response);
				return false;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to send discovery message", e);
			return false;
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