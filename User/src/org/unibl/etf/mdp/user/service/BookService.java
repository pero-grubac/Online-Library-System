package org.unibl.etf.mdp.user.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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

	public void sendBooksToEmail(List<BookDto> selectedBooks, String username) {
	    HttpURLConnection conn = null;

	    try {
	        // Konverzija knjiga u JSON
	    	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
	        String booksJson = gson.toJson(selectedBooks);

	        // Podešavanje konekcije
	        URL url = new URL(BOOKS_URL + "email/" + username);
	        conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("PUT"); // Endpoint koristi PUT metod
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setDoOutput(true);

	        // Slanje JSON podataka
	        try (OutputStream os = conn.getOutputStream()) {
	            os.write(booksJson.getBytes(StandardCharsets.UTF_8));
	        }

	        // Provera odgovora
	        int responseCode = conn.getResponseCode();
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            logger.info("Books sent to email successfully.");
	        } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
	            logger.warning("Invalid request. Check book data or username.");
	        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
	            logger.warning("User not found: " + username);
	        } else {
	            logger.warning("Unexpected response code: " + responseCode);
	        }

	    } catch (IOException e) {
	        logger.log(Level.SEVERE, "Error while sending books to email.", e);
	    } finally {
	        if (conn != null) {
	            conn.disconnect(); // Zatvaranje konekcije
	        }
	    }
	}


}
