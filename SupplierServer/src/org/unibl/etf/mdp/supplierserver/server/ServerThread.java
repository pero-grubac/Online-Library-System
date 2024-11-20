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

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplierserver.logger.FileLogger;
import org.unibl.etf.mdp.supplierserver.properties.AppConfig;
import org.unibl.etf.mdp.supplierserver.service.BookService;

import jdk.internal.org.jline.terminal.TerminalBuilder.SystemOutput;

public class ServerThread extends Thread {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(ServerThread.class.getName());

	private static final String GET_DTO_MSG = conf.getDtoMsg();
	private static final String END_MSG = conf.getEndMsg();
	private static final String GET_MODEL_MSG = conf.getModelMsg();

	private static final int PREVIEW_LINES = conf.getPreviewLines();
	private static final String START_MARKER = conf.getStartMarker();

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

				if (GET_DTO_MSG.equals(request.getType())) {
					String url = (String) request.getBody();
					String supplierName = request.getUsername();

					Book book = service.getBookFromUrl(url);
					service.saveBookToFile(book, supplierName);

					BookDto bookdto = new BookDto(book);
					bookdto.setPreview(getPreview(book.getContent()));
					
					out.writeObject(bookdto);
					out.flush();
					System.out.println("BookDTO " + bookdto + " Sent to  " + supplierName);

				} else if (GET_MODEL_MSG.equals(request.getType())) {
					String username = request.getUsername();
					BookDto bookDto = (BookDto) request.getBody();

					Book book = service.readBookFromFile(bookDto, username);

					out.writeObject(book);
					out.flush();

					System.out.println("Book " + book + "Sent to  " + username);
				} else if (END_MSG.equals(request.getType())) {
					System.out.println("Ending connection for supplier ");
					break;

				} else {
					System.out.println("Unknown message type: " + request.getType());
				}
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Exception.", ex);

		} finally {
			cleanup();
		}
	}

	private String getPreview(String content) {
		String[] lines = content.split("\n");
		StringBuilder result = new StringBuilder();
		boolean startFound = false;
		int linesRead = 0;

		for (String line : lines) {
			if (!startFound) {
				if (line.contains(START_MARKER)) {
					startFound = true;
				}
				continue;
			}

			result.append(line).append("\n");
			linesRead++;
			if (linesRead >= PREVIEW_LINES) {
				break;
			}
		}
		return result.toString();

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
