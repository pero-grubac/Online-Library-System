package org.unibl.etf.mdp.accountingserver.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;

import org.unibl.etf.mdp.library.model.Invoice;

import com.google.gson.Gson;

public class AccountingService implements IAccountingService {
	Gson gson = new Gson();
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	public AccountingService() throws RemoteException {
		super();
	}

	@Override
	public double addInvoice(Invoice invoice, String username) throws RemoteException {
		invoice.setVAT(invoice.getTotalPrice() * 0.17);
		return invoice.getVAT();
	}

	private void saveInvoice(Invoice invoice, String supplierName) {
		try {
			String date = invoice.getDate().format(formatter);
			String fileName = date + "-" + invoice.hashCode() + ".json";
			Path directoryPath = Paths.get("suppliers", supplierName);
			Path filePath = directoryPath.resolve(fileName);

			if (Files.notExists(directoryPath)) {
				Files.createDirectories(directoryPath);
			}

			String jsonContent = gson.toJson(invoice);
			Files.write(filePath, jsonContent.getBytes(), StandardOpenOption.CREATE);

			System.out.println("Invoice saved as JSON at: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
