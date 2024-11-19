package org.unibl.etf.mdp.supplier.gui;

import java.io.IOException;
import java.util.List;

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.supplier.mq.DirectReceiver;
import org.unibl.etf.mdp.supplier.server.Server;
import org.unibl.etf.mdp.supplier.services.LibraryService;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;

public class Data {
	private static Data instance = null;
	private List<BookDto> books;
	private String username;
	private static final SupplierServerService serverService = new SupplierServerService();
	private static final LibraryService libraryService = new LibraryService();

	private Data(List<BookDto> books, String username) {
		this.books = books;
		this.username = username;
		initServer();
		initMQ();
	}

	private void initMQ() {
		DirectReceiver receiver;

		try {
		    receiver = DirectReceiver.getInstance();
		    Thread receiverThread = new Thread(() -> {
		        try {
		            receiver.startListening(username, msg -> {
		                System.out.println("Received message: " + msg);

		                List<BookDto> bookDtos = (List<BookDto>) msg.getBody();

		                List<Book> books = serverService.getBooks(username, bookDtos);

		                Invoice invoice = libraryService.approveBook(books, username);

		                System.out.println(invoice);
		            });
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    });

		    receiverThread.start();
		} catch (Exception e) {
		    e.printStackTrace();
		}

	}

	private void initServer() {
		Server server = Server.getInstance(username, books);
		Thread serverThread = new Thread(server);
		serverThread.start();
	}

	public static synchronized Data getInstance(List<BookDto> books, String username) {
		if (instance == null) {
			instance = new Data(books, username);
		}
		return instance;
	}

	public List<BookDto> getBooks() {
		return books;
	}

	public void setBooks(List<BookDto> books) {
		this.books = books;
	}

	public String getUsername() {
		return username;
	}

	public static SupplierServerService getServerservice() {
		return serverService;
	}

	public static LibraryService getLibraryservice() {
		return libraryService;
	}

}
