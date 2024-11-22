package org.unibl.etf.mdp.user.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;
import org.unibl.etf.mdp.user.service.ChatService;

public class ChatServer implements Runnable {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ChatServer.class.getName());
	private static final String HOST = conf.getDefaultHost();

	private static ChatServer instance;
	private String username;
	private SSLServerSocket serverSocket;
	private AtomicBoolean running = new AtomicBoolean(false);

	private static ChatService service;

	private ChatServer(String username) {
		this.username = username;
	}

	public static synchronized ChatServer getInstance(String name) {
		if (instance == null) {
			instance = new ChatServer(name);
		}
		return instance;
	}

	@Override
	public void run() {
		try {
			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			serverSocket = (SSLServerSocket) ssf.createServerSocket(0); 
			int assignedPort = serverSocket.getLocalPort(); 

			service = ChatService.getInstance(username, assignedPort);
			service.register();
			running.set(true);
			while (running.get()) {
				try {
                    SSLSocket socket = (SSLSocket) serverSocket.accept();
					new ChatServerThread(socket);
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

	private void cleanup() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				instance = null;
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error during cleanup", e);
		}

	}
}