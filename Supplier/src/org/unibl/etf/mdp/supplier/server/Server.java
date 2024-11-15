package org.unibl.etf.mdp.supplier.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class Server {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(Server.class.getName());
	private static final int DISCOVERY_SERVER_TCP = conf.getDiscoveryServerTCPPort();
	private static final String HOST = conf.getDefaultHost();
	
	private String serverName;
	private int serverPort;
	private List<BookDto> books;

	public Server(String servername, List<BookDto> books) {
		this.serverName = servername;
		this.books = books;
		try {
			ServerSocket ss = new ServerSocket(0);
			this.serverPort = ss.getLocalPort();
			System.out.println(servername + " server on port: " + serverPort);

			// DISCOVERY
			sendDiscoveryMessage();

			while (true) {
				Socket sock = ss.accept();
				new ServerThread(sock, serverName, books);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
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

			// Send discovery message
			out.writeObject(msg);
			out.flush();

			// Wait for the acknowledgment message
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

}
