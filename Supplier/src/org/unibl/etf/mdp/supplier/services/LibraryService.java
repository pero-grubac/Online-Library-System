package org.unibl.etf.mdp.supplier.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Invoice;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.library.service.IAccountingService;
import org.unibl.etf.mdp.supplier.app.App;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

public class LibraryService {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(LibraryService.class.getName());

	private static final int LIBRARY_TCP_PORT = conf.getLibraryTCPPort();
	private static final String END_MSG = conf.getEndMsg();
	private static final String APPROVE_MSG = conf.getApproveMsg();
	private static final String DENIAL_MSG = conf.getDenialMsg();
	private static final String INVOICE_MSG = conf.getInvoiceMsg();

	public Invoice approveBook(List<Book> books, String username) {
		Message msg;
		Invoice invoice = new Invoice();
		try {

			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, LIBRARY_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			for (Book book : books) {
				msg = new Message(APPROVE_MSG, username, book);
				out.writeObject(msg);
				out.flush();
				BookDto bookDto = new BookDto(book);
				invoice.addBook(bookDto);
			}

			IAccountingService service = App.getAccountingService();
			invoice = service.addInvoice(invoice, username);

			msg = new Message(INVOICE_MSG, username, invoice);
			out.writeObject(msg);
			out.flush();
			
			msg = new Message(END_MSG);
			out.writeObject(msg);
			out.flush();

			in.close();
			out.close();
			sock.close();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the client application", ex);
		}
		return invoice;
	}

	public void denial(List<BookDto> books, String username) {
		Message msg;
		try {
			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, LIBRARY_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			for (BookDto book : books) {
				msg = new Message(DENIAL_MSG, username, book);
				out.writeObject(msg);
				out.flush();

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
	}
}
