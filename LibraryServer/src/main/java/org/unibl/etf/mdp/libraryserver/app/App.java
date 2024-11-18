package org.unibl.etf.mdp.libraryserver.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.unibl.etf.mdp.libraryserver.api.UserController;
import org.unibl.etf.mdp.libraryserver.mock.MockUsers;
import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.model.UserDto;

public class App {

	private static String readAll(BufferedReader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// MockUsers.createUsers();
		// System.out.println("Starting server...");
		UserController controller = new UserController();
		List<UserDto> usrs = controller.getAll();
		System.out.println("main");
		usrs.forEach(System.out::println);
		System.out.println("endpoint");
		String getAll = "http://localhost:8080/LibraryServer/api/users/";
		try (InputStream is = new URL(getAll).openStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

			String jsonText = readAll(rd);
			JSONArray json = new JSONArray(jsonText);
			System.out.println("Users from API:");
			System.out.println(json.toString());

		} catch (IOException e) {
			System.err.println("Failed to fetch users from API:");
			e.printStackTrace();
		}

	}
}
