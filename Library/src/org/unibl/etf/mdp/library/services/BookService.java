package org.unibl.etf.mdp.library.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unibl.etf.mdp.library.event.BookArrivalEvent;
import org.unibl.etf.mdp.library.event.Event;
import org.unibl.etf.mdp.library.interfaces.Observer;
import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.observer.BookObserver;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BookService {
	private static AppConfig conf = new AppConfig();
	private static final String BOOKS_URL = conf.getServerBooksUrl();
	public static final Logger logger = FileLogger.getLogger(BookService.class.getName());
	private static final int PREVIEW_LINES = conf.getPreviewLines();
	private static final String START_MARKER = conf.getStartMarker();

	private static BookService instance;
	private final List<BookObserver> observers = new ArrayList<>();

	private BookService() {
	}

	public static synchronized BookService getInstance() {
		if (instance == null) {
			instance = new BookService();
		}
		return instance;
	}

	public void addObserver(BookObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(BookObserver observer) {
		observers.remove(observer);
	}

	public void notifyBookArrival(List<Book> books) {
		BookArrivalEvent event = new BookArrivalEvent(books);
		notifyObservers(event);
	}

	private void notifyObservers(Event event) {
		for (BookObserver observer : observers) {
			observer.onEvent(event);
		}
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

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();

	}

	private String getPreview(String content) {
		String[] lines = content.split("\n");
		StringBuilder result = new StringBuilder();
		boolean startFound = false;
		int linesRead = 0;

		for (String line : lines) {
			if (!startFound) {
				if (line.contains(START_MARKER)) {
					startFound = true;
				}
				continue;
			}

			result.append(line).append("\n");
			linesRead++;
			if (linesRead >= PREVIEW_LINES) {
				break;
			}
		}
		return result.toString();

	}

	public boolean delete(BookDto book) {
		try {
			String encodedKey = URLEncoder.encode(book.getKey(), "UTF-8");

			URL url = new URL(BOOKS_URL + encodedKey);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");

			int responseCode = conn.getResponseCode();
			System.out.println(responseCode);
			return responseCode == HttpURLConnection.HTTP_NO_CONTENT; // 204
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error deleting book: " + book.getTitle(), e);
			return false;
		}
	}

}
