package org.unibl.etf.mdp.library.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.unibl.etf.mdp.common.ApiException;
import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** The catalogue on the LibraryServer (books in Redis). */
public class BookService {
    private static final AppConfig conf = new AppConfig();
    private static final String BOOKS_URL = conf.getServerBooksUrl();
    // The date format of the books API
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private static BookService instance;
    private final RestClient api = RestClient.shared();

    private BookService() {
    }

    public static synchronized BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }

    private static String url(String key) {
        return BOOKS_URL + URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    public List<BookDto> getAll() throws IOException {
        return Arrays.asList(gson.fromJson(ApiException.check(api.get(BOOKS_URL)).body(), BookDto[].class));
    }

    /** The full book, text and cover included. */
    public Book getBook(String key) throws IOException {
        return gson.fromJson(ApiException.check(api.get(url(key))).body(), Book.class);
    }

    public void add(Book book) throws IOException {
        ApiException.check(api.post(BOOKS_URL, gson.toJson(book)));
    }

    /** Text and cover left null are kept by the server. */
    public void update(String key, Book book) throws IOException {
        ApiException.check(api.put(url(key), gson.toJson(book)));
    }

    public void delete(BookDto book) throws IOException {
        ApiException.check(api.delete(url(book.getKey())));
    }
}
