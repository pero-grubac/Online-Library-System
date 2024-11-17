package org.unibl.etf.mdp.libraryserver.service;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unibl.etf.mdp.libraryservice.logger.FileLogger;
import org.unibl.etf.mdp.libraryservice.properties.AppConfig;
import org.unibl.etf.mdp.model.User;

public class UserXMLService {
	private static final AppConfig conf = new AppConfig();
	private static final Logger logger = FileLogger.getLogger(UserXMLService.class.getName());
	private static final String USERS_FOLDER = conf.getUsersFolder();
	private static final String USERS_FILE = conf.getUsersFile();

	public User add(User user) {
		List<User> users = getAll();
		int nextId = users.isEmpty() ? 1 : users.stream().mapToInt(User::getId).max().orElse(0) + 1;
        user.setId(nextId);
        
		if (!users.contains(user)) {
			users.add(user);
			create(users);
			return user;
		}
		logger.log(Level.INFO, "User exists: " + user.getUsername());
		return null;
	}

	private void create(List<User> users) {
		try (XMLEncoder encoder = new XMLEncoder(new FileOutputStream(getFilePath()))) {
			encoder.writeObject(users);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while creating: " + e.getMessage(), e);
		}

	}

	public List<User> getAll() {
		try (XMLDecoder decoder = new XMLDecoder(new FileInputStream(getFilePath()))) {
			return (List<User>) decoder.readObject();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Errore while reading: " + e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	public User getByUsername(String username) {
		List<User> users = getAll();
		Optional<User> user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
		return user.orElse(null);
	}

	private String getFilePath() {
		File folder = new File(USERS_FOLDER);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(Paths.get(USERS_FOLDER, USERS_FILE).toString());
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Error while creating file: " + file.getPath(), e);
			}
		}
		return file.getPath();
	}
}
