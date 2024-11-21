package org.unibl.etf.mdp.user.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.user.logger.FileLogger;
import org.unibl.etf.mdp.user.properties.AppConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LibraryService {
	private static AppConfig conf = new AppConfig();
	private static final String USERS_URL = conf.getServerUserUrl();
	private static final Logger logger = FileLogger.getLogger(LibraryService.class.getName());
	private static LibraryService instance;

	private LibraryService() {
	}

	public static synchronized LibraryService getInstance() {
		if (instance == null)
			instance = new LibraryService();
		return instance;
	}

	public boolean login(String username, String password) {
		try {
			URL url = new URL(USERS_URL + "login");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			String jsonInputString = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return true;
			} else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				logger.warning("Invalid credentials for user: " + username);
				return false;
			} else {
				logger.warning("Unexpected response code: " + responseCode);
				return false;
			}
		} catch (Exception e) {
			logger.severe("Error during login: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public boolean register(User user) {
		try {
			URL url = new URL(USERS_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			String jsonInputString = new Gson().toJson(user);

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonInputString.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int responseCode = conn.getResponseCode();
			return responseCode == HttpURLConnection.HTTP_CREATED;
		} catch (Exception e) {
			logger.severe("Error during registration: " + e.getMessage());
			return false;
		}
	}

}
