package org.unibl.etf.mdp.user.service;

import com.google.gson.Gson;
import org.unibl.etf.mdp.common.ApiException;
import org.unibl.etf.mdp.common.RestClient;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

import java.io.IOException;
import java.util.logging.Logger;

/** Login and registration on the LibraryServer. */
public class LibraryService {
    private static final Logger logger = FileLogger.getLogger(LibraryService.class.getName());
    private static AppConfig conf = new AppConfig();
    private static final String USERS_URL = conf.getServerUserUrl();
    private static LibraryService instance;

    private LibraryService() {
    }

    public static synchronized LibraryService getInstance() {
        if (instance == null)
            instance = new LibraryService();
        return instance;
    }

    /**
     * Logs in; the session token is then sent with every request of this application.
     *
     * @throws ApiException with the server's reason, e.g. that the account still waits for approval
     */
    public void login(String username, String password) throws IOException {
        RestClient.Result result = RestClient.shared().login(USERS_URL + "login", username, password);
        if (!result.ok()) {
            logger.warning("Login of " + username + " refused with HTTP " + result.status());
            throw new ApiException(result);
        }
    }

    /** @throws ApiException with the server's reason, e.g. a username that is taken */
    public void register(User user) throws IOException {
        RestClient.Result result = RestClient.shared().post(USERS_URL, new Gson().toJson(user));
        if (result.status() == 409) {
            throw new IOException("The username \"" + user.getUsername() + "\" is already taken.");
        }
        ApiException.check(result);
    }
}
