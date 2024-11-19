package org.unibl.etf.mdp.library.gui;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.unibl.etf.mdp.library.mq.DirectSender;
import org.unibl.etf.mdp.library.observer.BookObserver;
import org.unibl.etf.mdp.library.observer.InvoiceObserver;
import org.unibl.etf.mdp.library.server.Server;
import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.library.services.InvoiceService;
import org.unibl.etf.mdp.library.services.UserService;

public class Data {
	private static BookObserver bookObserver = new BookObserver();
	private static InvoiceObserver invoiceObserver = new InvoiceObserver();
	private static BookService bookService = BookService.getInstance();
	private static InvoiceService invoiceService = InvoiceService.getInstance();
	private static UserService userService = UserService.getInstance();
	private static DirectSender sender;
	private Server server;
	private Thread serverThread;
	private static Data instance = null;

	private Data() {
		bookService.addObserver(bookObserver);
		invoiceService.addObserver(invoiceObserver);
		initServer();
		initMQ();
	}

	private void initServer() {
		server = Server.getInstance();
		serverThread = new Thread(server);
		serverThread.start();

	}

	private void initMQ() {
		initSender();
	}

	public static void initSender() {
		if (sender == null) {
			try {
				sender = DirectSender.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void shutdownMQ() {
		if (sender != null) {
			try {
				sender.shutdown();
				sender = null;

			} catch (IOException | TimeoutException e) {
				e.printStackTrace();
			}
		}
	}

	public static DirectSender getSender() {
		return sender;
	}

	public static synchronized Data getInstance() {
		if (instance == null) {
			instance = new Data();
		}
		return instance;
	}

	public static BookObserver getBookObserver() {
		return bookObserver;
	}

	public static InvoiceObserver getInvoiceObserver() {
		return invoiceObserver;
	}

	public static BookService getBookService() {
		return bookService;
	}

	public static InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public static UserService getUserService() {
		return userService;
	}

}
