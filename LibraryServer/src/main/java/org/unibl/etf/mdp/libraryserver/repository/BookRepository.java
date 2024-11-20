package org.unibl.etf.mdp.libraryserver.repository;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class BookRepository {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(BookRepository.class.getName());
	private static final String BOOK_KEY_PREFIX = conf.getBookKeyPrefix();
	private final JedisPool pool;
	private static BookRepository instance = null;

	private BookRepository() {
		String redisHost = conf.getRedisHost();
		int redisPort = conf.getRedisPort();
		System.out.println("Connecting to Redis at " + redisHost + ":" + redisPort);

		pool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
	}

	public static synchronized BookRepository getInstance() {
		if (instance == null) {
			instance = new BookRepository();
		}
		return instance;
	}

	private String generateKey(Book book) {
		if (book == null || book.getTitle() == null || book.getAuthor() == null || book.getReleaseDate() == null
				|| book.getLanguage() == null) {
			throw new IllegalArgumentException("Book, title, and author must not be null.");
		}
		return BOOK_KEY_PREFIX + book.getKey();
	}

	public void create(Book book) {
		String bookKey = generateKey(book);
		Map<String, String> bookMap = book.toHashMap();

		try (Jedis jedis = pool.getResource()) {
			jedis.hmset(bookKey, bookMap);
			logger.info("Book saved with key: " + bookKey);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Redis error while saving book: " + e.getMessage(), e);
		}
	}

	public Book getByKey(String key) {
		String bookKey = BOOK_KEY_PREFIX + key;

		try (Jedis jedis = pool.getResource()) {
			if (!jedis.exists(bookKey)) {
				logger.warning("Book with key " + bookKey + " does not exist.");
				return null;
			}

			Map<String, String> bookMap = jedis.hgetAll(bookKey);
			return Book.fromHashMap(bookMap);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Redis error while fetching book: " + e.getMessage(), e);
			return null;
		}
	}

	public List<Book> getAll() {
		List<Book> books = new ArrayList<>();

		try (Jedis jedis = pool.getResource()) {
			Set<String> keys = jedis.keys(BOOK_KEY_PREFIX + "*");

			for (String key : keys) {
				Map<String, String> bookMap = jedis.hgetAll(key);
				books.add(Book.fromHashMap(bookMap));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Redis error while fetching all books: " + e.getMessage(), e);
		}

		return books;
	}

	public boolean deleteByKey(String key) {

		String bookKey = BOOK_KEY_PREFIX + key;

		try (Jedis jedis = pool.getResource()) {
			if (!jedis.exists(bookKey)) {
				logger.warning("Book with key " + bookKey + " does not exist.");
				return false;
			}

			jedis.del(bookKey);
			logger.info("Book with key " + bookKey + " deleted successfully.");
			return true;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Redis error while deleting book: " + e.getMessage(), e);
			return false;
		}
	}
}
