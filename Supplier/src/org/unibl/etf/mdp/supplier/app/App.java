package org.unibl.etf.mdp.supplier.app;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.mock.MockSupppliers;
import org.unibl.etf.mdp.supplier.mq.DirectReceiver;
import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.server.Server;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;
import org.unibl.etf.mdp.supplier.templates.Tuple;

public class App {
	public static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(App.class.getName());

	public static void main(String[] args) {

		System.out.println("Supplier client");
		MockSupppliers mock = new MockSupppliers();
		Map<String, List<String>> supplierData = mock.getSupplierData();
		Map<String, List<BookDto>> supplierBooks = new HashMap<>();

		SupplierServerService serverService = new SupplierServerService();

		System.out.println("Available suppliers:");
		for (String supplierName : supplierData.keySet()) {
			System.out.println("- " + supplierName);
		}

		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter supplier name: ");
		String supplierName = scanner.nextLine();

		if (supplierData.containsKey(supplierName)) {
			List<String> bookLinks = supplierData.get(supplierName);
			List<BookDto> books = serverService.getBooks(supplierName, bookLinks);
			supplierBooks.put(supplierName, books);
		} else {
			System.out.println("Invalid supplier name. Please try again.");
		}

		scanner.close();

		Server server = Server.getInstance(supplierName, supplierBooks.get(supplierName));
		Thread serverThread = new Thread(server);
		serverThread.start();
		DirectReceiver receiver;
		try {
			receiver = DirectReceiver.getInstance();
			receiver.startListening(supplierName, msg -> {
				System.out.println("Received message: " + msg);
				
				// ocekuj List<BookDto>
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
