package org.unibl.etf.mdp.user.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.unibl.etf.mdp.common.ApiException;
import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.user.properties.AppConfig;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** The library's catalogue, as a member sees it. */
public class BookService {
    private static AppConfig conf = new AppConfig();
    private static final String BOOKS_URL = conf.getServerBooksUrl();
    // The date format of the books API
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private static BookService instance;

    private BookService() {
    }

    public static synchronized BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }

    public List<BookDto> getAll() throws IOException {
        return Arrays.asList(gson.fromJson(ApiException.check(RestClient.shared().get(BOOKS_URL)).body(),
                BookDto[].class));
    }

    /**
     * Has the library mail the books to the member as a ZIP.
     *
     * @return the server's confirmation
     * @throws ApiException with the server's reason when nothing was sent
     */
    public String sendBooksToEmail(List<BookDto> selectedBooks, String username) throws IOException {
        String url = BOOKS_URL + "email/" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        return ApiException.check(RestClient.shared().put(url, gson.toJson(selectedBooks))).body();
    }
}
