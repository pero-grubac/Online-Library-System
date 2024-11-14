package org.unibl.etf.mdp.supplier.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.Book;
import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Invoice;
import org.unibl.etf.mdp.library.service.IAccountingService;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.mock.MockSupppliers;
import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.templates.Tuple;

import sun.tools.serialver.resources.serialver;

public class App {
	public static final AppConfig conf = new AppConfig();
	public static final int DOBAVLJAC_SERVER_TCP_PORT = conf.getDobavljacServerTCPPort();
	private static final Logger logger = FileLogger.getLogger(App.class.getName());

	public static void main(String[] args) {
		try {
			System.out.println("Supplier client");
			MockSupppliers mock = new MockSupppliers();
			Map<String, Tuple<List<String>, List<Book>>> supplierData = mock.getSupplierData();
			/*
			 * List<String> urlList =
			 * Arrays.asList("https://www.gutenberg.org/cache/epub/1342/pg1342.txt",
			 * "https://www.gutenberg.org/cache/epub/1661/pg1661.txt",
			 * "https://www.gutenberg.org/cache/epub/2701/pg2701.txt"); List<Book> books =
			 * new ArrayList<>(); Invoice invoice = new Invoice();
			 * System.out.println("DOBAVLJAC KLIJENT"); InetAddress addr =
			 * InetAddress.getByName("localhost"); Socket sock = new Socket(addr,
			 * DOBAVLJAC_SERVER_TCP_PORT);
			 * 
			 * ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			 * PrintWriter out = new PrintWriter(new BufferedWriter(new
			 * OutputStreamWriter(sock.getOutputStream())), true);
			 * 
			 * for (String url : urlList) { out.println(url);
			 * System.out.println("\nSent URL to server: " + url); Book book = (Book)
			 * in.readObject(); books.add(book); BookDto bookdto = new BookDto(book);
			 * invoice.addBook(bookdto); System.out.println("Received Book from server: " +
			 * " " + book); } out.println("KRAJ");
			 * 
			 * in.close(); out.close(); sock.close();
			 * 
			 * String securityDir = conf.getSecurityDir(); String securityFile =
			 * conf.getSecurityFile(); String securityPolicy = conf.getSecurityPolicy();
			 * System.setProperty(securityPolicy, securityDir + File.separator +
			 * securityFile); if (System.getSecurityManager() == null) {
			 * System.setSecurityManager(new SecurityManager()); } String registryName =
			 * conf.getRegistryName(); int registryPort = conf.getRegistryPort(); Registry
			 * registry = LocateRegistry.getRegistry(registryPort); IAccountingService
			 * service = (IAccountingService) registry.lookup(registryName);
			 * System.out.println(service.addInvoice(invoice, "test"));
			 */

			/*
			 * MockSupppliers mock = new MockSupppliers(); Map<String, Tuple<List<String>,
			 * List<Book>>> supplierData = mock.getSupplierData();
			 * 
			 * InetAddress addr = InetAddress.getByName("localhost"); Socket sock = new
			 * Socket(addr, DOBAVLJAC_SERVER_TCP_PORT);
			 * 
			 * ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			 * PrintWriter out = new PrintWriter(new BufferedWriter(new
			 * OutputStreamWriter(sock.getOutputStream())), true);
			 * 
			 * for (Map.Entry<String, Tuple<List<String>, List<Book>>> entry :
			 * supplierData.entrySet()) { String supplierName = entry.getKey(); List<String>
			 * bookLinks = entry.getValue().getFirst(); List<Book> books =
			 * entry.getValue().getSecond();
			 * 
			 * for (String url : bookLinks) { out.println(supplierName + "|" + url);
			 * 
			 * Book book = (Book) in.readObject(); books.add(book);
			 * System.out.println("Supplier " + supplierName + "recieved Book from server: "
			 * + " " + book); } } out.println("KRAJ"); in.close(); out.close();
			 * sock.close();
			 */

			for (Map.Entry<String, Tuple<List<String>, List<Book>>> entry : supplierData.entrySet()) {
				String supplierName = entry.getKey();
				List<String> bookLinks = entry.getValue().getFirst();
				List<Book> books = entry.getValue().getSecond();

				new Thread(() -> {
					try {
						InetAddress addr = InetAddress.getByName("localhost");
						Socket sock = new Socket(addr, DOBAVLJAC_SERVER_TCP_PORT);

						ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
						PrintWriter out = new PrintWriter(
								new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

						for (String url : bookLinks) {
							out.println(supplierName + "|" + url);

							Book book = (Book) in.readObject();
							synchronized (books) {
								books.add(book);
							}
							System.out.println("Supplier " + supplierName + " received Book from server: " + book);
						}
						out.println("KRAJ");
						in.close();
						out.close();
						sock.close();
					} catch (Exception ex) {
						logger.log(Level.SEVERE, "An error occurred for supplier " + supplierName, ex);
					}
				}).start();
			}

		} catch (

		Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		}

	}

}
