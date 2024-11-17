package org.unibl.etf.mdp.library.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.library.mq.DirectSender;
import org.unibl.etf.mdp.library.observer.BookObserver;
import org.unibl.etf.mdp.library.observer.InvoiceObserver;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.library.server.Server;
import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.library.services.DiscoveryServerService;
import org.unibl.etf.mdp.library.services.InvoiceService;
import org.unibl.etf.mdp.library.services.SupplierService;

public class App {

	public static void main(String[] args) {
		BookObserver bookObserver = new BookObserver();
		InvoiceObserver invoiceObserver = new InvoiceObserver();
		BookService bookService = BookService.getInstance();
		bookService.addObserver(bookObserver);
		InvoiceService invoiceService = InvoiceService.getInstance();
		invoiceService.addObserver(invoiceObserver);
		
		Map<String, String> suppliers = new HashMap<>();
		suppliers = DiscoveryServerService.getSuppliers();
		Map<String, List<BookDto>> suppliersBooks = new HashMap<>();

		suppliers.entrySet().stream()
				.forEach(entry -> System.out.println("Server: " + entry.getKey() + ", Port: " + entry.getValue()));

		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter supplier name: ");
		String supplierName = scanner.nextLine();

		if (suppliers.containsKey(supplierName)) {
			List<BookDto> books = SupplierService.getOfferedBooks(supplierName,
					Integer.parseInt(suppliers.get(supplierName)));
			suppliersBooks.put(supplierName, books);
		} else {
			System.out.println("Invalid supplier name. Please try again.");
		}

		scanner.close();
		AppConfig conf = new AppConfig();
		String req = conf.getRequestMsg();

		List<BookDto> reqb = new ArrayList<>();
		reqb.add(suppliersBooks.get(supplierName).get(0));
		Message msg = new Message(req, "biblioteka:port", reqb);
		
		Server server = Server.getInstance();
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		DirectSender sender;
		try {
			sender = DirectSender.getInstance();
			sender.sendMessage(supplierName, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		// sender.shutdown();

	}

}
