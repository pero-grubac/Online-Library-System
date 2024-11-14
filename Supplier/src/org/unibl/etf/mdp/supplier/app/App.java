package org.unibl.etf.mdp.supplier.app;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.mock.MockSupppliers;
import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.templates.Tuple;

public class App {
	public static final AppConfig conf = new AppConfig();
	public static final int DOBAVLJAC_SERVER_TCP_PORT = conf.getDobavljacServerTCPPort();
	private static final Logger logger = FileLogger.getLogger(App.class.getName());

	public static void main(String[] args) {
		try {
			System.out.println("Supplier client");
			MockSupppliers mock = new MockSupppliers();
			Map<String, Tuple<List<String>, List<Book>>> supplierData = mock.getSupplierData();
			String getDTOMsg = conf.getDtoMsg();
			String endMsg = conf.getEndMsg();
			Message msg;

			InetAddress addr = InetAddress.getByName("localhost");
			Socket sock = new Socket(addr, DOBAVLJAC_SERVER_TCP_PORT);

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());

			for (Map.Entry<String, Tuple<List<String>, List<Book>>> entry : supplierData.entrySet()) {
				String supplierName = entry.getKey();
				List<String> bookLinks = entry.getValue().getFirst();
				List<Book> books = entry.getValue().getSecond();

				for (String url : bookLinks) {
					msg = new Message(getDTOMsg, supplierName, url);
					out.writeObject(msg);
					out.flush();
					// out.println(supplierName + "|" + url);

					Book book = (Book) in.readObject();
					synchronized (books) {
						books.add(book);
					}
					System.out.println("Supplier " + supplierName + " received Book from server: " + book);
				}

			}
			msg = new Message(endMsg);
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
