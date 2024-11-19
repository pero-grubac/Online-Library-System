package org.unibl.etf.mdp.supplier.app;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.service.IAccountingService;
import org.unibl.etf.mdp.supplier.gui.LoginFrame;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.mock.MockSupppliers;
import org.unibl.etf.mdp.supplier.mq.DirectReceiver;
import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.server.Server;
import org.unibl.etf.mdp.supplier.services.LibraryService;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;
import org.unibl.etf.mdp.supplier.templates.Tuple;

public class App {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(App.class.getName());
	private static IAccountingService accountingService;

	public static void main(String[] args) {

		System.out.println("Supplier client");
		initializeRMI();
		/*
		 * 
		 * Server server = Server.getInstance(supplierName,
		 * supplierBooks.get(supplierName)); Thread serverThread = new Thread(server);
		 * serverThread.start(); DirectReceiver receiver; try { receiver =
		 * DirectReceiver.getInstance(); receiver.startListening(supplierName, msg -> {
		 * System.out.println("Received message: " + msg);
		 * 
		 * List<BookDto> bookDtos = (List<BookDto>) msg.getBody();
		 * 
		 * List<Book> books = serverService.getBooks(supplierName, bookDtos);
		 * 
		 * Invoice invoice = libraryService.approveBook(books, supplierName);
		 * 
		 * System.out.println(invoice); }); } catch (Exception e) { e.printStackTrace();
		 * }
		 */
		new LoginFrame().setVisible(true);
		
	}

	public static void initializeRMI() {
		try {
			String securityDir = conf.getSecurityDir();
			String securityFile = conf.getSecurityFile();
			String securityPolicy = conf.getSecurityPolicy();
			System.setProperty(securityPolicy, securityDir + File.separator + securityFile);
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			String registryName = conf.getRegistryName();
			int registryPort = conf.getRegistryPort();
			Registry registry = LocateRegistry.getRegistry(registryPort);

			accountingService = (IAccountingService) registry.lookup(registryName);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to initialize RMI", e);
		}
	}

	public static IAccountingService getAccountingService() {
		return accountingService;
	}
}
