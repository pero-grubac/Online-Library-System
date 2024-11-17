package org.unibl.etf.mdp.supplier.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplier.app.App;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class SupplierServerService {
	private static final AppConfig conf = new AppConfig();
	private static final int SUPPLIER_SERVER_TCP_PORT = conf.getSupplierServerTCPPort();
	private static final Logger logger = FileLogger.getLogger(SupplierServerService.class.getName());

	private static final String GET_DTO_MSG = conf.getDtoMsg();
	private static final String END_MSG = conf.getEndMsg();
	private static final String GET_MODEL_MSG = conf.getModelMsg();

	public SupplierServerService() {

	}

	public List<BookDto> getBookDtos(String username, List<String> bookLinks) {
		List<BookDto> books = new ArrayList<>();

		Message msg;

		try {

			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, SUPPLIER_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			for (String url : bookLinks) {
				msg = new Message(GET_DTO_MSG, username, url);
				out.writeObject(msg);
				out.flush();

				BookDto book = (BookDto) in.readObject();

				books.add(book);
			}

			msg = new Message(END_MSG);
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

	public List<Book> getBooks(String username, List<BookDto> bookDtos) {
		List<Book> books = new ArrayList<>();
		Message msg;
		try {
			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, SUPPLIER_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			for (BookDto bookDto : bookDtos) {
				msg = new Message(GET_MODEL_MSG, username, bookDto);
				out.writeObject(msg);
				out.flush();

				Book book = (Book) in.readObject();
				books.add(book);
				System.out.println(book);
			}

			msg = new Message(END_MSG);
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
