package org.unibl.etf.mdp.service;

import com.google.gson.Gson;
import org.unibl.etf.mdp.accountingserver.logger.FileLogger;
import org.unibl.etf.mdp.accountingserver.properties.AppConfig;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.model.Invoice;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountingService implements IAccountingService {
    private static final AppConfig conf = new AppConfig();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(conf.getInvoiceDatePattern());
    private static final Logger logger = FileLogger.getLogger(AccountingService.class.getName());
    // Knows java.time, which plain Gson cannot reflect into on Java 17+
    private final Gson gson = Json.gson();
    private final Path invoicesDir;
    private final BigDecimal vatRate;

    public AccountingService() throws RemoteException {
        this(Paths.get(conf.getInvoicesDir()), new BigDecimal(conf.getVatRate()));
    }

    public AccountingService(Path invoicesDir, BigDecimal vatRate) throws RemoteException {
        super();
        this.invoicesDir = invoicesDir;
        this.vatRate = vatRate;
    }

    @Override
    public Invoice addInvoice(Invoice invoice, String username) throws RemoteException {
        BigDecimal vat = BigDecimal.valueOf(invoice.getTotalPrice()).multiply(vatRate).setScale(2, RoundingMode.HALF_UP);
        invoice.setVAT(vat.doubleValue());
        try {
            saveInvoice(invoice, username);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save the invoice of " + username, ex);
            // The supplier must know the invoice was not booked
            throw new RemoteException("The invoice could not be saved", ex);
        }
        return invoice;
    }

    private void saveInvoice(Invoice invoice, String supplierName) throws IOException {
        Path directoryPath = invoicesDir.resolve(supplierName);
        Files.createDirectories(directoryPath);
        // A random part keeps two invoices of the same day apart
        String fileName = invoice.getDate().format(formatter) + "-" + UUID.randomUUID() + ".json";
        Path filePath = directoryPath.resolve(fileName);
        Files.writeString(filePath, gson.toJson(invoice), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        logger.info("Invoice saved as JSON at: " + filePath);
    }
}
