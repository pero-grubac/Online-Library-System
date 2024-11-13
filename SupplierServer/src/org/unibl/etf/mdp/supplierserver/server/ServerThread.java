package org.unibl.etf.mdp.supplierserver.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
				BookService service = new BookService();
				if (request.startsWith("https://www.gutenberg.org/cache/epub/")) {
					Book book = service.getBookFromUrl(request);
					out.writeObject(book);
					out.flush();
				}
			}
			in.close();
			out.close();
			sock.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
