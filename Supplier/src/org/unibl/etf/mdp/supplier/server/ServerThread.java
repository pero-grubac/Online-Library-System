package org.unibl.etf.mdp.supplier.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.server.ServerThread;

public class ServerThread extends Thread {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());

	private static final String END_MSG = conf.getEndMsg();
	private static final String GET_DTO_MSG = conf.getDtoMsg();
	private static final String OK_MSG = conf.getOkMsg();

	private Socket sock;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String serverName;
	private List<BookDto> books;

	public ServerThread(Socket sock, String serverName, List<BookDto> books) {
		this.sock = sock;
		this.serverName = serverName;
		this.books = books;
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

					if (GET_DTO_MSG.equals(request.getType())) {
						for (BookDto book : books) {
							Message response = new Message(GET_DTO_MSG, serverName, book);
							out.writeObject(response);
							out.flush();

							Message acknowledgment = (Message) in.readObject();
							if (!OK_MSG.equals(acknowledgment.getType())) {
								logger.warning("Did not receive OK acknowledgment for book: " + book);
								break;
							}
						}
						Message endMessage = new Message(END_MSG, serverName);
						out.writeObject(endMessage);
						out.flush();
						break;
					} else if (END_MSG.equals(request.getType())) {
						System.out.println("Ending connection for supplier " + serverName);
						break;
					} else {
						logger.log(Level.WARNING, "Unknown message type received: " + request.getType());
					}
				} catch (EOFException ex) {
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
