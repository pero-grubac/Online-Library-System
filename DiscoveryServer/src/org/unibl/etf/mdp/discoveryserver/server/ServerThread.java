package org.unibl.etf.mdp.discoveryserver.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.discoveryserver.logger.FileLogger;
import org.unibl.etf.mdp.discoveryserver.properties.AppConfig;
import org.unibl.etf.mdp.library.model.Message;

public class ServerThread extends Thread {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());
	private static final String discoverMsg = conf.getDiscoveryMsg();
	private static final String endMsg = conf.getEndMsg();
	private static final String okMsg = conf.getOkMsg();
	private static final String discoverAllMsg = conf.getDiscoverAllMsg();

	private Socket sock;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	private static Map<String, String> servers = new HashMap<>();

	public ServerThread(Socket sock) {
		this.sock = sock;
		try {
			in = new ObjectInputStream(sock.getInputStream());
			out = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Failed to initialize input/output streams.", ex);
		}
		start();
	}

	public void run() {
		try {
			Message request;
			while (true) {
				try {
					request = (Message) in.readObject();
					if (discoverMsg.equals(request.getType())) {
						int port;
						try {
							port = (Integer) request.getBody();
						} catch (Exception e) {
							logger.log(Level.SEVERE, "Error parsing port.", e);
							break;
						}
						servers.put((String) request.getUsername(), (String) request.getBody());
						System.out.println("Registered server: " + request.getUsername() + " on port: " + port);

						Message response = new Message(okMsg);
						out.writeObject(response);
						out.flush();
					} else if (discoverAllMsg.equals(request.getType())) {
						Message response = new Message(discoverAllMsg, "DiscoveryServer");
						response.setBody(servers.toString());
						out.writeObject(response);
						out.flush();
					} else if (endMsg.equals(request.getType())) {
						System.out.println("Ending connection for: " + request.getUsername());
						break;
					} else {
						logger.log(Level.SEVERE, "Unknown message type: " + request.getType());
					}
				} catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, "Error reading object. Class not found.", e);
					break;
				} catch (IOException e) {
					logger.log(Level.WARNING, "Client disconnected.", e);
					break;
				}
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Unexpected error during run.", ex);
		} finally {
			cleanup();
		}
	}

	private void cleanup() {
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (sock != null && !sock.isClosed())
				sock.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error closing resources.", e);
		}
	}

}
