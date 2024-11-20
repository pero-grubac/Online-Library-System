package org.unibl.etf.mdp.supplierserver.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.supplierserver.logger.FileLogger;
import org.unibl.etf.mdp.supplierserver.properties.AppConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class BookService {
	// private final JedisPool pool;
	private static final AppConfig conf = new AppConfig();
	private static final Random rand = new Random();
	private static final Logger logger = FileLogger.getLogger(BookService.class.getName());
	private static final String DIRECTORY = conf.getSuppliersDir();
	private static final String EXT = conf.getBookExt();
	private static final String IMAGE_URL_END = conf.getImageUrlEnd();

	public BookService() {
		/*
		 * String redisHost = conf.getRedisHost(); int redisPort = conf.getRedisPort();
		 * pool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
		 */
	}

	public Book getBookFromUrl(String url) {
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
			book = parseString(content);
			int lastSlashIndex = url.lastIndexOf('/');
			String baseUrl = url.substring(0, lastSlashIndex + 1);
			String fileName = url.substring(lastSlashIndex + 1, url.lastIndexOf('.'));

			String photoUrl = baseUrl + fileName + IMAGE_URL_END;

			BufferedImage image = ImageService.downloadImage(photoUrl);
			// convert bi to bytes
			byte[] bytes;
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(image, conf.getImageExt(), baos);
				bytes = baos.toByteArray();
			} catch (Exception e) {
				System.err.println("Error while converting cover image to bytes: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
			// convert bytes to base64
			String base64Image = Base64.getEncoder().encodeToString(bytes);
			book.setCoverImageBase64(base64Image);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (contentReader != null) {
				try {
					contentReader.close();
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "An error occurred in the server application", ex);
				} catch (Exception ex) {
					logger.log(Level.SEVERE, "An error occurred in the server application", ex);
				}
			}
		}
		return book;
	}

	private Book parseString(StringBuilder content) {
		Book book = new Book();
		book.setContent(content.toString());

		book.setTitle(parsePattern("Title:\\s*(.*)", content.toString()));
		book.setAuthor(parsePattern("Author:\\s*(.*)", content.toString()));
		book.setLanguage(parsePattern("Language:\\s*(.*)", content.toString()));

		String releaseDateStr = parsePattern("Release date:\\s*([A-Za-z]+\\s+\\d{1,2},\\s+\\d{4})", content.toString());
		if (releaseDateStr != null) {
			try {
				book.setReleaseDate(new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(releaseDateStr));
			} catch (ParseException ex) {
				logger.log(Level.SEVERE, "An error occurred in the server application", ex);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "An error occurred in the server application", ex);
			}
		}
		book.setPrice(rand.nextInt(100) + 1);
		return book;
	}

	private String parsePattern(String pattern, String text) {
		Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		if (m.find()) {
			return m.group(1).trim();
		}
		return null;
	}

	public void saveBookToFile(Book book, String username) {
		Path filePath = Paths.get(DIRECTORY, username, book.toString() + EXT);

		try {
			Files.createDirectories(filePath.getParent());

			Files.write(filePath, book.getContent().getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);

			System.out.println("Book content saved successfully at: " + filePath);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "An error occurred while saving the book content", ex);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "An error occurred in the server application", ex);
		}
	}

	public Book readBookFromFile(BookDto bookDto, String username) {
		Book book = new Book();
		Path filePath = Paths.get(DIRECTORY, username, bookDto.toString() + EXT);
		StringBuilder content = new StringBuilder();

		try {
			Files.lines(filePath).forEach(line -> content.append(line).append("\n"));
			book = parseString(content);
		} catch (IOException ex) {
			System.err.println("An error occurred while reading the book content: " + ex.getMessage());
		}
		return book;
	}
	/*
	 * public void saveBookToRedis(Book book, String username) { String bookId =
	 * "supplier:" + username + ":book:" + book.hashCode(); Map<String, String>
	 * bookMap = book.toHashMap();
	 * 
	 * try (Jedis jedis = pool.getResource()) { jedis.hmset(bookId, bookMap);
	 * System.out.println("Book saved in Redis for user " + username + " with ID: "
	 * + bookId); } catch (JedisConnectionException ex) { logger.log(Level.SEVERE,
	 * "An error occurred in the server application", ex); } catch (Exception ex) {
	 * logger.log(Level.SEVERE, "An error occurred in the server application", ex);
	 * } }
	 * 
	 * public Book getBookFromRedis(String username, int bookHash) { String bookId =
	 * "user:" + username + ":book:" + bookHash;
	 * 
	 * try (Jedis jedis = pool.getResource()) { if (!jedis.exists(bookId)) {
	 * System.out.println("Book not found in Redis for user " + username +
	 * " with ID: " + bookId); return null; } Map<String, String> bookMap =
	 * jedis.hgetAll(bookId); if (bookMap.isEmpty()) {
	 * System.out.println("Book not found in Redis for user " + username +
	 * " with ID: " + bookId); return null; } return Book.fromMap(bookMap); } catch
	 * (JedisConnectionException ex) { logger.log(Level.SEVERE,
	 * "An error occurred in the server application", ex); return null; } catch
	 * (Exception ex) { logger.log(Level.SEVERE,
	 * "An error occurred in the server application", ex); return null; } }
	 */
}
