package org.unibl.etf.mdp.supplierserver.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.supplierserver.service.BookService;

import jdk.internal.org.jline.terminal.TerminalBuilder.SystemOutput;

public class ServerThread extends Thread {

	private Socket sock;
	private BufferedReader in;
	private ObjectOutputStream out;
	private BookService service = new BookService();
	public ServerThread(Socket sock) {
		this.sock = sock;
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new ObjectOutputStream(sock.getOutputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		start();
	}

	public void run() {
		try {
			String request;
			while (!"KRAJ".equals(request = in.readLine())) {
				String[] parts = request.split("\\|");
				if (parts.length == 2) {
					String supplierName = parts[0];
					String url = parts[1];

					Book book = service.getBookFromUrl(url);
					service.saveBookToFile(book, supplierName);
					out.writeObject(book);
					System.out.println("Sent book to supplier " + supplierName + " for URL: " + url);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
