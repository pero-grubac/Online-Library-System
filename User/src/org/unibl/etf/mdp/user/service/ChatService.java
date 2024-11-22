package org.unibl.etf.mdp.user.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

public class ChatService {
	private static final AppConfig conf = new AppConfig();
	private static final int DISCOVERY_SERVER_TCP_PORT = conf.getDiscoveryServerTCPPort();
	private static final Logger logger = FileLogger.getLogger(ChatService.class.getName());
	private static final String HOST = conf.getDefaultHost();

	private static final String DISCOVER_USER_MSG = conf.getDiscoverUserMsg();
	private static final String DISCOVER_ALL_USERS_MSG = conf.getDiscoverAllUsersMsg();
	private static final String CHAT_MSG = conf.getChatMsg();
	private static final String END_MSG = conf.getEndMsg();

	private static final String KEY_STORE_PATH = conf.getKeyStorePath();
	private static final String KEY_STORE_PASSWORD = conf.getKeyStorePass();

	private String username;
	private int port;
	private static ChatService instance;

	private ChatService(String username, int port) {
		this.username = username;
		this.port = port;
	}

	public static synchronized ChatService getInstance(String username, int port) {
		if (instance == null)
			instance = new ChatService(username, port);

		return instance;
	}

	public Map<String, String> getUsers() {
		Map<String, String> users = new HashMap<>();
		Message msg = new Message(DISCOVER_ALL_USERS_MSG);
		try {
			InetAddress addr = InetAddress.getByName(HOST);
			Socket sock = new Socket(addr, DISCOVERY_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			out.writeObject(msg);
			out.flush();

			Message response = (Message) in.readObject();
			String serverList = (String) response.getBody();
			String[] entries = serverList.replace("{", "").replace("}", "").split(", ");
			for (String entry : entries) {
				String[] keyValue = entry.split("=");
				String user = keyValue[0].trim();
				String port = keyValue[1].trim();
				users.put(user, port);
			}

			out.writeObject(new Message(END_MSG));
			out.flush();

			in.close();
			out.close();
			sock.close();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the client application", ex);
		}

		return users;
	}

	public void register() {
		try (Socket socket = new Socket(HOST, DISCOVERY_SERVER_TCP_PORT);
				ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
			output.writeObject(new Message(DISCOVER_USER_MSG, username, port));
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred during registration", ex);
		}

	}

	public void sendMessage(String recipientIp, int recipientPort, String messageBody) {
	    try {
	        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
	        SSLSocket socket = (SSLSocket) sf.createSocket(HOST, recipientPort);

	        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

	        Message message = new Message(CHAT_MSG, username, messageBody);
	        out.writeObject(message);
	        out.flush();

	        out.close();
	        socket.close();
	    } catch (Exception ex) {
	        logger.log(Level.SEVERE, "An error occurred while sending a message", ex);
	    }
	}

}
