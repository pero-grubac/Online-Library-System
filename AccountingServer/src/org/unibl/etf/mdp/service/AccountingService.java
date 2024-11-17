package org.unibl.etf.mdp.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.accountingserver.logger.FileLogger;
import org.unibl.etf.mdp.model.Invoice;

import com.google.gson.Gson;

public class AccountingService implements IAccountingService {
	Gson gson = new Gson();
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	private static final Logger logger = FileLogger.getLogger(AccountingService.class.getName());

	public AccountingService() throws RemoteException {
		super();
	}

	@Override
	public Invoice addInvoice(Invoice invoice, String username) throws RemoteException {
		double vat = invoice.getTotalPrice() * 0.17;
		BigDecimal vatRounded = BigDecimal.valueOf(vat).setScale(2, RoundingMode.HALF_UP);
		invoice.setVAT(vatRounded.doubleValue());
		saveInvoice(invoice, username);
		return invoice;
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
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		}
	}
}
