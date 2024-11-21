package org.unibl.etf.mdp.user.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BookService {
	private static AppConfig conf = new AppConfig();
	private static final String BOOKS_URL = conf.getServerBooksUrl();
	private static final Logger logger = FileLogger.getLogger(BookService.class.getName());
	private static BookService instance;

	private BookService() {
	}

	public static synchronized BookService getInstance() {
		if (instance == null) {
			instance = new BookService();
		}
		return instance;
	}

	public List<BookDto> getAll() {
		List<BookDto> books = new ArrayList<>();
		try (InputStream is = new URL(BOOKS_URL).openStream();
				InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			BookDto[] bookArray = gson.fromJson(reader, BookDto[].class);
			books = Arrays.asList(bookArray);

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to fetch books.", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected error occurred while fetching books.", e);
		}
		return books;
	}
}
