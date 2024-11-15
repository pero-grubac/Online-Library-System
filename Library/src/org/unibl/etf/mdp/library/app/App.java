package org.unibl.etf.mdp.library.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.unibl.etf.mdp.library.model.BookDto;
import org.unibl.etf.mdp.library.services.DiscoveryServerService;
import org.unibl.etf.mdp.library.services.SupplierService;

public class App {

	public static void main(String[] args) {
		Map<String, String> suppliers = new HashMap<>();
		suppliers = DiscoveryServerService.getSuppliers();
		Map<String,List<BookDto>> suppliersBooks = new HashMap<>();
		
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

	}

}
