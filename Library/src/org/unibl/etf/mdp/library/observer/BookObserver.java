package org.unibl.etf.mdp.library.observer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.library.event.BookArrivalEvent;
import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.interfaces.Observer;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BookObserver implements Observer {
	private static AppConfig conf = new AppConfig();
	private static final String BOOKS_URL = conf.getServerBooksUrl();
	public static final Logger logger = FileLogger.getLogger(BookObserver.class.getName());

	@Override
	public void onEvent(Event event) {
		if (event instanceof BookArrivalEvent) {
			List<Book> books = ((BookArrivalEvent) event).getBooks();
			handleBooksArrival(books);
		} else {
			System.err.println("Unexpected event type: " + event.getClass().getName());
		}
	}

	private void handleBooksArrival(List<Book> books) {
		System.out.println("Books arrived: " + books);

		for (Book book : books) {
			try {
				// Kreiranje URL-a za server
				URL url = new URL(BOOKS_URL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();

				// Podešavanje HTTP metode i zaglavlja
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setDoOutput(true);

				// Pretvaranje knjige u JSON
				String bookJson = convertBookToJson(book);
				try (OutputStream os = conn.getOutputStream()) {
					os.write(bookJson.getBytes());
					os.flush();
				}

				// Provera odgovora servera
				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_CREATED) {
					System.out.println("Book added successfully: " + book.getTitle());
				} else {
					System.err.println("Failed to add book: " + book.getTitle() + ". Response code: " + responseCode);
					try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
						StringBuilder errorResponse = new StringBuilder();
						String line;
						while ((line = br.readLine()) != null) {
							errorResponse.append(line);
						}
						System.err.println("Error response: " + errorResponse);
					}
				}

			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error adding book: " + book.getTitle(), e);
			}
		}
	}

	private String convertBookToJson(Book book) {
	    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create(); 
	    return gson.toJson(book);
	}

}