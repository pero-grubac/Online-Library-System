package org.unibl.etf.mdp.library.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.Charset;

import org.unibl.etf.mdp.library.logger.FileLogger;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.UserDto;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserService {
	private static AppConfig conf = new AppConfig();
	private static final String USERS_URL = conf.getServerUsersUrl();
	private static final Logger logger = FileLogger.getLogger(UserService.class.getName());
	private static UserService instance;

	private UserService() {
	}

	public static synchronized UserService getInstance() {
		if (instance == null)
			instance = new UserService();
		return instance;
	}

	public List<UserDto> getAll() {
		List<UserDto> users = new ArrayList<>();
		try (InputStream is = new URL(USERS_URL).openStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {

			String jsonText = readAll(rd);
			JSONArray jsonArray = new JSONArray(jsonText);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				UserDto user = new UserDto();
				user.setId(jsonObject.getInt("id"));
				user.setFirstName(jsonObject.getString("firstName"));
				user.setLastName(jsonObject.getString("lastName"));
				user.setAddress(jsonObject.getString("address"));
				user.setEmail(jsonObject.getString("email"));
				user.setUsername(jsonObject.getString("username"));
				user.setStatus(StatusEnum.valueOf(jsonObject.getString("status")));

				users.add(user);
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to fetch users.", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected error occurred while fetching users.", e);
		}

		return users;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public void delete(UserDto user) {
		try {
			URL url = new URL(USERS_URL + user.getUsername());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
				logger.log(Level.SEVERE, "Unexpected error occurred while deleting users.", responseCode);
			}
			conn.disconnect();
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE, "Unexpected error occurred while deleting users.", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unexpected error occurred while deleting users.", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unexpected error occurred while deleting users.", e);
		}

	}

	public void update(UserDto user) {
		try {
			URL url = new URL(USERS_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");

			JSONObject json = new JSONObject();
			json.put("id", user.getId());
			json.put("firstName", user.getFirstName());
			json.put("lastName", user.getLastName());
			json.put("address", user.getAddress());
			json.put("email", user.getEmail());
			json.put("username", user.getUsername());
			json.put("status", user.getStatus().toString());

			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = json.toString().getBytes("UTF-8");
				os.write(input, 0, input.length);
			}

			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				logger.log(Level.SEVERE, "Failed to update user. HTTP response code: " + responseCode);
			}

			conn.disconnect();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while updating user.", e);
		}
	}

	public void changeStatus(String username, StatusEnum status) {
		try {
			URL url = new URL(USERS_URL + "/" + username + "/status?status=" + status.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("PATCH");
			conn.setRequestProperty("Content-Type", "text/plain");

			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				logger.log(Level.SEVERE, "Failed to update status. HTTP error code: " + responseCode);
			} else {
				logger.log(Level.INFO, "User status updated successfully.");
			}

			conn.disconnect();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error while changing status.", e);
		}
	}

}
