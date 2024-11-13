package org.unibl.etf.mdp.dobavljacserver.app;

import java.net.ServerSocket;
import java.net.Socket;

import org.unibl.etf.mdp.biblioteka.model.Book;
import org.unibl.etf.mdp.dobavljacserver.server.ServerThread;
import org.unibl.etf.mdp.dobavljacserver.service.BookService;

public class App {
	public static final int TCP_PORT = 9000;

	public static void main(String[] args) {
		try {
			System.out.println("DOBAVLJAC SERVER");
			ServerSocket ss = new ServerSocket(TCP_PORT);
			while (true) {
				Socket sock = ss.accept();
				new ServerThread(sock);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
