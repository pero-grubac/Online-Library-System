package org.unibl.etf.mdp.library.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.event.BookArrivalEvent;
import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.event.InvoiceEvent;
import org.unibl.etf.mdp.library.interfaces.Observer;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.observer.BookObserver;
import org.unibl.etf.mdp.library.observer.InvoiceObserver;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.library.services.InvoiceService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.Message;

public class ServerThread extends Thread {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());

	private static final String APPROVE_MSG = conf.getApproveMsg();
	private static final String DENIAL_MSG = conf.getDenialMsg();
	private static final String OK_MSG = conf.getOkMsg();
	private static final String END_MSG = conf.getEndMsg();
	private static final String INVOICE_MSG = conf.getInvoiceMsg();

	private Socket sock;

	private ObjectInputStream in;
	private ObjectOutputStream out;

	private String serverName;
	private List<Book> books;

	public ServerThread(Socket socket) {
		this.sock = socket;
		this.books = new ArrayList<>();
		try {
			in = new ObjectInputStream(sock.getInputStream());
			out = new ObjectOutputStream(sock.getOutputStream());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Failed to initialize input/output streams.", ex);
		}
	}

	public void run() {
		try {
			Message request;
			while (true) {
				try {

					request = (Message) in.readObject();

					if (APPROVE_MSG.equals(request.getType())) {
						Object body = request.getBody();
						List<Book> receivedBooks = new ArrayList<>();

						if (body instanceof List) {
							receivedBooks = (List<Book>) body;
							books.addAll(receivedBooks);
						} else if (body instanceof Book) {
							Book book = (Book) body;
							books.add(book);
							receivedBooks.add(book);
						} else {
							logger.log(Level.WARNING, "Unexpected body type: " + body.getClass().getName());
						}

						BookService.getInstance().notifyBookArrival(receivedBooks);

						Message endMessage = new Message(END_MSG, serverName);
						out.writeObject(endMessage);
						out.flush();
						//break;
					} else if (INVOICE_MSG.equals(request.getType())) {
						Invoice invoice = (Invoice) request.getBody();
						InvoiceService.getInstance().notifyInvoiceReceived(invoice);
						break;

					} else if (DENIAL_MSG.equals(request.getType())) {
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
