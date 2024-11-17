package org.unibl.etf.mdp.supplier.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.supplier.properties.AppConfig;
import org.unibl.etf.mdp.supplier.templates.Tuple;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.BookSupplier;
import org.unibl.etf.mdp.supplier.logger.FileLogger;

public class MockSupppliers {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(MockSupppliers.class.getName());
	private static final List<String> bookUrls = Arrays.asList("https://www.gutenberg.org/cache/epub/1342/pg1342.txt",
			"https://www.gutenberg.org/cache/epub/2701/pg2701.txt",
			"https://www.gutenberg.org/cache/epub/1400/pg1400.txt",
			"https://www.gutenberg.org/cache/epub/1399/pg1399.txt",
			"https://www.gutenberg.org/cache/epub/2600/pg2600.txt",
			"https://www.gutenberg.org/cache/epub/4300/pg4300.txt",
			"https://www.gutenberg.org/cache/epub/996/pg996.txt",
			"https://www.gutenberg.org/cache/epub/1184/pg1184.txt", "https://www.gutenberg.org/cache/epub/84/pg84.txt",
			"https://www.gutenberg.org/cache/epub/345/pg345.txt", "https://www.gutenberg.org/cache/epub/120/pg120.txt",
			"https://www.gutenberg.org/cache/epub/1260/pg1260.txt",
			"https://www.gutenberg.org/cache/epub/768/pg768.txt",
			"https://www.gutenberg.org/cache/epub/19417/pg19417.txt",
			"https://www.gutenberg.org/cache/epub/11/pg11.txt", "https://www.gutenberg.org/cache/epub/2383/pg2383.txt",
			"https://www.gutenberg.org/cache/epub/8800/pg8800.txt",
			"https://www.gutenberg.org/cache/epub/14591/pg14591.txt",
			"https://www.gutenberg.org/cache/epub/2554/pg2554.txt",
			"https://www.gutenberg.org/cache/epub/28054/pg28054.txt",
			"https://www.gutenberg.org/cache/epub/829/pg829.txt",
			"https://www.gutenberg.org/cache/epub/1260/pg1260.txt",
			"https://www.gutenberg.org/cache/epub/1342/pg1342.txt",
			"https://www.gutenberg.org/cache/epub/27761/pg27761.txt",
			"https://www.gutenberg.org/cache/epub/64317/pg64317.txt",
			"https://www.gutenberg.org/cache/epub/36/pg36.txt", "https://www.gutenberg.org/cache/epub/37/pg37.txt",
			"https://www.gutenberg.org/cache/epub/38/pg38.txt", "https://www.gutenberg.org/cache/epub/39/pg39.txt",
			"https://www.gutenberg.org/cache/epub/514/pg514.txt", "https://www.gutenberg.org/cache/epub/98/pg98.txt",
			"https://www.gutenberg.org/cache/epub/161/pg161.txt", "https://www.gutenberg.org/cache/epub/141/pg141.txt",
			"https://www.gutenberg.org/cache/epub/1661/pg1661.txt",
			"https://www.gutenberg.org/cache/epub/174/pg174.txt", "https://www.gutenberg.org/cache/epub/135/pg135.txt",
			"https://www.gutenberg.org/cache/epub/25344/pg25344.txt",
			"https://www.gutenberg.org/cache/epub/1257/pg1257.txt",
			"https://www.gutenberg.org/cache/epub/145/pg145.txt",
			"https://www.gutenberg.org/cache/epub/2413/pg2413.txt",
			"https://www.gutenberg.org/cache/epub/766/pg766.txt",
			"https://www.gutenberg.org/cache/epub/1727/pg1727.txt",
			"https://www.gutenberg.org/cache/epub/6130/pg6130.txt", "https://www.gutenberg.org/cache/epub/21/pg21.txt",
			"https://www.gutenberg.org/cache/epub/74/pg74.txt", "https://www.gutenberg.org/cache/epub/76/pg76.txt",
			"https://www.gutenberg.org/cache/epub/1232/pg1232.txt",
			"https://www.gutenberg.org/cache/epub/132/pg132.txt",
			"https://www.gutenberg.org/cache/epub/1497/pg1497.txt",
			"https://www.gutenberg.org/cache/epub/5200/pg5200.txt");

	public MockSupppliers() {
		init();
	}

	private void init() {
		for (BookSupplier supplier : BookSupplier.values()) {
			createDirectory(supplier.getName());
			createLinks(supplier.getName());
		}
	}

	private void createDirectory(String name) {
		String dir = conf.getSuppliersDir();
		String linkFile = conf.getLinksFile();
		Path path = Paths.get(dir, name, linkFile);

		try {
			Files.createDirectories(path.getParent());
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred while creating directory for supplier: " + name, ex);
		}
	}

	private void createLinks(String supplierName) {
		String dir = conf.getSuppliersDir();
		String linkFile = conf.getLinksFile();
		Path path = Paths.get(dir, supplierName, linkFile);

		Collections.shuffle(bookUrls);
		List<String> selectedUrls = bookUrls.subList(0, 20);

		try {
			Files.write(path, selectedUrls);
			System.out.println("Links saved at: " + path);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred while saving links for supplier: " + supplierName, e);
		}
	}

	public Map<String, List<String>> getSupplierData() {
		Map<String, List<String>> supplierData = new HashMap<>();
		String dir = conf.getSuppliersDir();
		String linkFile = conf.getLinksFile();

		for (BookSupplier supplier : BookSupplier.values()) {
			String supplierName = supplier.getName();
			Path linkFilePath = Paths.get(dir, supplierName, linkFile);

			try {
				List<String> bookLinks = Files.readAllLines(linkFilePath);

				supplierData.put(supplierName, bookLinks);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "An error occurred while reading links for supplier: " + supplierName, e);
			}
		}

		return supplierData;
	}
}
