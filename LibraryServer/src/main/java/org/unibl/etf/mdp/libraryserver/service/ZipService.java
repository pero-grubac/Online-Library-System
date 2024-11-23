package org.unibl.etf.mdp.libraryserver.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

public class ZipService {
	private static final Logger logger = FileLogger.getLogger(ZipService.class.getName());
	private static final AppConfig conf = new AppConfig();
	private static final String BOOK_EXT = conf.getBookExt();
	private static final String ZIP_FOLDER = conf.getZipFolder();
	private static final String ZIP_FILE = conf.getZipFile();
	private final BookService bookService;

	public ZipService() {
		this.bookService = new BookService();
	}

	public String zipBooks(List<BookDto> bookDtos) {
		if (bookDtos == null || bookDtos.isEmpty()) {
			logger.log(Level.WARNING, "BookDto list is empty or null.");
			return null;
		}

		try {
			Path zipFolderPath = Paths.get(ZIP_FOLDER);
			if (!Files.exists(zipFolderPath)) {
				Files.createDirectories(zipFolderPath);
				logger.info("Created ZIP folder: " + zipFolderPath.toAbsolutePath());
			}

			Path zipFilePath = zipFolderPath.resolve(ZIP_FILE);

			try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
				for (BookDto bookDto : bookDtos) {
					Book book = bookService.getByKey(bookDto.getKey());

					if (book == null) {
						logger.log(Level.WARNING, "Book not found for key: " + bookDto.getKey());
						continue;
					}

					String fileName = book.getTitle().replaceAll("[:\\\\/*?|<>]", "-") + BOOK_EXT;
					ZipEntry entry = new ZipEntry(fileName);
					zipOut.putNextEntry(entry);

					String content = book.getContent() != null ? book.getContent() : "No content available.";
					zipOut.write(content.getBytes());

					zipOut.closeEntry();
				}
				logger.info("ZIP file created successfully: " + zipFilePath.toAbsolutePath());
				return zipFilePath.toAbsolutePath().toString();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error while creating ZIP file: " + e.getMessage(), e);
				return null;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while creating ZIP folder: " + e.getMessage(), e);
			return null;
		}
	}

	public void clearZipFolder() {
		try {
			Path zipFolderPath = Paths.get(ZIP_FOLDER);
			if (Files.exists(zipFolderPath)) {
				Files.walk(zipFolderPath).filter(Files::isRegularFile).forEach(file -> {
					try {
						Files.delete(file);
					} catch (IOException e) {
						logger.log(Level.WARNING, "Failed to delete file: " + file.toAbsolutePath(), e);
					}
				});
				logger.info("Cleared ZIP folder: " + zipFolderPath.toAbsolutePath());
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while clearing ZIP folder: " + e.getMessage(), e);
		}
	}

}
