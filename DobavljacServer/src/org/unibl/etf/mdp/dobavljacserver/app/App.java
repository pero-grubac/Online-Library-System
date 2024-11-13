package org.unibl.etf.mdp.dobavljacserver.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.unibl.etf.mdp.biblioteka.model.Book;
import org.unibl.etf.mdp.dobavljacserver.logger.FileLogger;
import org.unibl.etf.mdp.dobavljacserver.server.ServerThread;
import org.unibl.etf.mdp.dobavljacserver.service.BookService;

public class App {
	public static final int TCP_PORT = 9000;
    private static final Logger logger = FileLogger.getLogger(App.class.getName());

	public static void main(String[] args) {
		try {
			System.out.println("DOBAVLJAC SERVER");
			ServerSocket ss = new ServerSocket(TCP_PORT);
			while (true) {
				Socket sock = ss.accept();
				new ServerThread(sock);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		}
	}

}
