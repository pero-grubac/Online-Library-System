package org.unibl.etf.mdp.dobavljacserver.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unibl.etf.mdp.dobavljacserver.model.Book;

import redis.clients.jedis.Jedis;

public class BookService {
	private static final Jedis jedis = new Jedis("localhost", 6379);

	public static Book getBookFromUrl(String url) {
		URL bookURL;
		BufferedReader contentReader = null;
		StringBuilder content = new StringBuilder();
		Book book = new Book();

		try {
			bookURL = new URL(url);
			contentReader = new BufferedReader(new InputStreamReader(bookURL.openStream()));
			String line;
			while ((line = contentReader.readLine()) != null) {
				content.append(line).append("\n");
			}
			book.setContent(content.toString());

			book.setTitle(parsePattern("Title:\\s*(.*)", content.toString()));
			book.setAuthor(parsePattern("Author:\\s*(.*)", content.toString()));
			book.setEditor(parsePattern("Editor:\\s*(.*)", content.toString()));
			book.setLanguage(parsePattern("Language:\\s*(.*)", content.toString()));

			String releaseDateStr = parsePattern("Release date:\\s*([A-Za-z]+\\s+\\d{1,2},\\s+\\d{4})",
					content.toString());
			if (releaseDateStr != null) {
				try {
					book.setReleaseDate(new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(releaseDateStr));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (contentReader != null) {
				try {
					contentReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return book;
	}

	private static String parsePattern(String pattern, String text) {
		Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		if (m.find()) {
			return m.group(1).trim();
		}
		return null;
	}

	public static void saveBookToFile(Book book, String username) {
		Path directoryPath = Paths.get("suppliers", "users", username);
		try {
			Files.createDirectories(directoryPath);

			String fileName = book.toString() + ".txt";
			Path filePath = directoryPath.resolve(fileName);

			Files.write(filePath, book.getContent().getBytes());
			System.out.println("Book content saved successfully at: " + filePath);
		} catch (IOException e) {
			System.out.println("An error occurred while saving the book content.");
			e.printStackTrace();
		}
	}

	public static void saveBookToRedis(Book book, String username) {
		String bookId = "user:" + username + ":book:" + book.hashCode();
		Map<String, String> bookMap = book.toHashMap();
		jedis.hmset(bookId, bookMap);
		System.out.println("Book saved in Redis for user " + username + " with ID: " + bookId);
	}

	public static Book getBookFromRedis(String username, int bookHash) {
		String bookId = "user:" + username + ":book:" + bookHash;
		Map<String, String> bookMap = jedis.hgetAll(bookId);
		Book book = Book.fromMap(bookMap);
		return book;
	}
}
