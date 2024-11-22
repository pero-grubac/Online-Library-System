package org.unibl.etf.mdp.user.chat;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import java.time.LocalDateTime;

import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

public class ChatServerThread extends Thread {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ChatServerThread.class.getName());
	private static final String CHAT_MESSAGE = conf.getChatMsg();
	private static final String OK_MSG = conf.getOk();

	private SSLSocket sock;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ChatHistory chatHistory;

	public ChatServerThread(SSLSocket socket) {
		this.sock = socket;
		chatHistory = ChatHistory.getInstance();
		try {
			in = new ObjectInputStream(sock.getInputStream());
			out = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Failed to initialize input/output streams.", ex);
		}
		start();
	}

	public void run() {
		Message request;
		try {
			while (true) {
				request = (Message) in.readObject();
				try {
					if (CHAT_MESSAGE.equals(request.getType())) {
						String user = request.getUsername();
						String text = (String) request.getBody();
						ChatMessage cm = new ChatMessage(user, text, LocalDateTime.now());
						chatHistory.addMessage(user, cm);

						out.writeObject(new Message(OK_MSG));
						out.flush();
						break;
					} else {
						logger.log(Level.WARNING, "Unknown message type received: " + request.getType());
					}
				} catch (EOFException e) {
					logger.log(Level.INFO, "Client disconnected (EOF).");
					break; 
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "Client ended the connection.", ex);
					break;
				}
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Exception occurred during run.", ex);
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
