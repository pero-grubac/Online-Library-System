package org.unibl.etf.mdp.supplier.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.supplier.app.App;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class SupplierServerService {
	public AppConfig conf;
	public int DOBAVLJAC_SERVER_TCP_PORT;
	private Logger logger;

	public SupplierServerService() {
		conf = new AppConfig();
		DOBAVLJAC_SERVER_TCP_PORT = conf.getSupplierServerTCPPort();
		logger = FileLogger.getLogger(SupplierServerService.class.getName());
	}

	public List<BookDto> getBooks(String username, List<String> bookLinks) {
		List<BookDto> books = new ArrayList<>();
		String getDTOMsg = conf.getDtoMsg();
		String endMsg = conf.getEndMsg();
		Message msg;

		try {

			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, DOBAVLJAC_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			for (String url : bookLinks) {
				msg = new Message(getDTOMsg, username, url);
				out.writeObject(msg);
				out.flush();

				BookDto book = (BookDto) in.readObject();

				books.add(book);

				System.out.println("Supplier " + username + " received Book from server: " + book);
			}

			msg = new Message(endMsg);
			out.writeObject(msg);
			out.flush();

			in.close();
			out.close();
			sock.close();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the client application", ex);
		}
		return books;
	}
}
