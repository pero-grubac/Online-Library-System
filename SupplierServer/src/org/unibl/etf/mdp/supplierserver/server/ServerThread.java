package org.unibl.etf.mdp.supplierserver.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.supplierserver.logger.FileLogger;
import org.unibl.etf.mdp.supplierserver.properties.AppConfig;
import org.unibl.etf.mdp.supplierserver.service.BookService;

import jdk.internal.org.jline.terminal.TerminalBuilder.SystemOutput;

public class ServerThread extends Thread {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());
	private static final String getDTOMsg = conf.getDtoMsg();
	private static final String endMsg = conf.getEndMsg();

	private Socket sock;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private BookService service = new BookService();

	public ServerThread(Socket sock) {
		this.sock = sock;
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
				request = (Message) in.readObject();

				// Provera tipa poruke
				if (getDTOMsg.equals(request.getType())) {
					String url = (String) request.getBody(); // Preuzimanje URL-a knjige iz tela poruke
					String supplierName = request.getUsername(); // Preuzimanje korisničkog imena dobavljača

					// Preuzimanje knjige na osnovu URL-a i čuvanje
					Book book = service.getBookFromUrl(url);
					service.saveBookToFile(book, supplierName);

					// Slanje odgovora klijentu
					BookDto bookdto = new BookDto(book);
					out.writeObject(bookdto);
					out.flush();
					System.out.println("Sent book to supplier " + supplierName);

				} else if (endMsg.equals(request.getType())) {
					System.out.println("Ending connection for supplier ");
					break; // Izlazak iz petlje i završetak konekcije

				} else {
					System.out.println("Unknown message type: " + request.getType());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
