package org.unibl.etf.mdp.supplier.gui;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
	private Server serverInstance;
	private Thread serverThread;
	private List<List<BookDto>> requests = new CopyOnWriteArrayList<>();

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

						addRequest(bookDtos);
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
		serverInstance = Server.getInstance(username, books);
		serverThread = new Thread(serverInstance);
		serverThread.start();
	}

	public static synchronized Data getInstance(List<BookDto> books, String username) {
		if (instance == null) {
			instance = new Data(books, username);
		}
		return instance;
	}

	public void shutdownServer() {
		if (serverInstance != null) {
			serverInstance.shutdown();
			try {
				serverThread.join();
				System.out.println("Server thread terminated.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public List<BookDto> getBooks() {
		return books;
	}

	public void setBooks(List<BookDto> books) {
		shutdownServer();
		this.books = books;
		initServer();

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

	public List<List<BookDto>> getRequests() {
		return requests;
	}

	public void addRequest(List<BookDto> bookDtos) {
		requests.add(bookDtos);
	}

	public void removeRequest(List<BookDto> bookDtos) {
		requests.remove(bookDtos);
	}

}
