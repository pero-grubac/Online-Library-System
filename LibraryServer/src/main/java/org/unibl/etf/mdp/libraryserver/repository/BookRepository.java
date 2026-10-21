package org.unibl.etf.mdp.libraryserver.repository;

import org.unibl.etf.mdp.libraryserver.logger.FileLogger;
import org.unibl.etf.mdp.libraryserver.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookRepository {
    private static final AppConfig conf = new AppConfig();
    private static final Logger logger = FileLogger.getLogger(BookRepository.class.getName());
    private static final String BOOK_KEY_PREFIX = conf.getBookKeyPrefix();
    private static final int SCAN_PAGE_SIZE = 200;
    private static BookRepository instance = null;
    private final Supplier<JedisPool> pool;

    private BookRepository() {
        this.pool = RedisConnection::pool;
    }

    public BookRepository(JedisPool pool) {
        this.pool = () -> pool;
    }

    public static synchronized BookRepository getInstance() {
        if (instance == null) {
            instance = new BookRepository();
        }
        return instance;
    }

    private String generateKey(Book book) {
        // The release date is part of the key but may be unknown (Gutenberg headers do not always have one)
        if (book == null || book.getTitle() == null || book.getAuthor() == null || book.getLanguage() == null) {
            throw new IllegalArgumentException("Book, title, author and language must not be null.");
        }
        return BOOK_KEY_PREFIX + book.getKey();
    }

    public void create(Book book) {
        String bookKey = generateKey(book);
        Map<String, String> bookMap = book.toHashMap();

        try (Jedis jedis = pool.get().getResource()) {
            jedis.hset(bookKey, bookMap);
            logger.info("Book saved with key: " + bookKey);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Redis error while saving book: " + e.getMessage(), e);
            throw new IllegalStateException("Redis error while saving book", e);
        }
    }

    public Book getByKey(String key) {
        String bookKey = BOOK_KEY_PREFIX + key;

        try (Jedis jedis = pool.get().getResource()) {
            if (!jedis.exists(bookKey)) {
                logger.warning("Book with key " + bookKey + " does not exist.");
                return null;
            }

            Map<String, String> bookMap = jedis.hgetAll(bookKey);
            return Book.fromHashMap(bookMap);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Redis error while fetching book: " + e.getMessage(), e);
            throw new IllegalStateException("Redis error while fetching book", e);
        }
    }

    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();

        try (Jedis jedis = pool.get().getResource()) {
            // SCAN walks the keys in pages instead of blocking Redis like KEYS does
            ScanParams params = new ScanParams().match(BOOK_KEY_PREFIX + "*").count(SCAN_PAGE_SIZE);
            Set<String> keys = new LinkedHashSet<>(); // SCAN may return a key twice
            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                ScanResult<String> page = jedis.scan(cursor, params);
                keys.addAll(page.getResult());
                cursor = page.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));

            // One round trip for all books
            Pipeline pipeline = jedis.pipelined();
            List<Response<Map<String, String>>> hashes = new ArrayList<>();
            for (String key : keys) {
                hashes.add(pipeline.hgetAll(key));
            }
            pipeline.sync();
            for (Response<Map<String, String>> hash : hashes) {
                if (!hash.get().isEmpty()) {
                    books.add(Book.fromHashMap(hash.get()));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Redis error while fetching all books: " + e.getMessage(), e);
            throw new IllegalStateException("Redis error while fetching all books", e);
        }

        return books;
    }

    public boolean deleteByKey(String key) {

        String bookKey = BOOK_KEY_PREFIX + key;

        try (Jedis jedis = pool.get().getResource()) {
            if (!jedis.exists(bookKey)) {
                logger.warning("Book with key " + bookKey + " does not exist.");
                return false;
            }

            jedis.del(bookKey);
            logger.info("Book with key " + bookKey + " deleted successfully.");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Redis error while deleting book: " + e.getMessage(), e);
            throw new IllegalStateException("Redis error while deleting book", e);
        }
    }
}
