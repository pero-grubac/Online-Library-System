package org.unibl.etf.mdp.library.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;

public class SupplierService {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(DiscoveryServerService.class.getName());
	private static final String GET_DTO_MSG = conf.getDtoMsg();
	private static final String END_MSG = conf.getEndMsg();
	private static final String OK_MSG = conf.getOkMsg();
	private static final String HOST = conf.getDefaultHost();

	public static List<BookDto> getOfferedBooks(String username, int port) {
		List<BookDto> books = new ArrayList<>();

		Message request = new Message(GET_DTO_MSG, username);
		try (Socket sock = new Socket(InetAddress.getByName(HOST), port);
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
			
			out.writeObject(request);
			out.flush();

			while (true) {
				Message response = (Message) in.readObject();

				if (GET_DTO_MSG.equals(response.getType())) {
					books.add((BookDto) response.getBody());

					Message acknowledgment = new Message(OK_MSG, username);
					out.writeObject(acknowledgment);
					out.flush();
				} else if (END_MSG.equals(response.getType())) {
					System.out.println("Received end message, closing connection.");
					break;
				} else {
					logger.warning("Unknown message type received: " + response.getType());
				}
			}

		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the client application", ex);
		}

		return books;
	}
}
