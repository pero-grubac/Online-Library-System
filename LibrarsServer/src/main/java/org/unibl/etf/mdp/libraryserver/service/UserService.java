package org.unibl.etf.mdp.libraryserver.service;

import java.util.ArrayList;
import java.util.List;

import org.unibl.etf.mdp.libraryserver.repository.UserRepository;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

public class UserService {
	private final UserRepository repository = new UserRepository();

	public UserDto add(User user) {
		List<User> users = repository.findAll();
		if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
			return null;
		}

		int nextId = getNextId(users);
		user.setId(nextId);
		user.setStatus(StatusEnum.PENDING);
		users.add(user);
		repository.saveAll(users);

		return new UserDto(user);
	}

	public List<UserDto> getAll() {
		List<User> users = repository.findAll();
		List<UserDto> userDtos = new ArrayList<>();
		users.forEach(user -> userDtos.add(new UserDto(user)));
		return userDtos;
	}

	public boolean login(User loginUser) {
		if (loginUser.getUsername().isBlank() || loginUser.getPassword().isBlank()) {
			return false;
		}
		return repository.findByUsername(loginUser.getUsername()).filter(
				user -> user.getPassword().equals(loginUser.getPassword()) && user.getStatus() == StatusEnum.APPROVED)
				.isPresent();
	}

	public boolean approver(String username) {
		return changeUserStatus(username, StatusEnum.APPROVED);
	}

	public boolean reject(String username) {
		return changeUserStatus(username, StatusEnum.REJECTED);
	}

	public boolean block(String username) {
		return changeUserStatus(username, StatusEnum.BLOCKED);
	}

	private boolean changeUserStatus(String username, StatusEnum status) {
		List<User> users = repository.findAll();
		User user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);

		if (user == null) {
			return false;
		}

		user.setStatus(status);
		repository.saveAll(users);
		return true;
	}

	public boolean delete(String username) {
		if (username == null || username.isBlank()) {
			return false;
		}
		List<User> users = repository.findAll();
		boolean removed = users.removeIf(u -> u.getUsername().equals(username));
		if (removed) {
			repository.saveAll(users);
		}
		return removed;
	}

	public boolean update(UserDto userDto) {
		if (userDto == null || userDto.getUsername() == null || userDto.getUsername().isBlank()
				|| userDto.getFirstName() == null || userDto.getFirstName().isBlank() || userDto.getLastName() == null
				|| userDto.getLastName().isBlank() || userDto.getAddress() == null || userDto.getAddress().isBlank()
				|| userDto.getEmail() == null || userDto.getEmail().isBlank() || userDto.getStatus() == null) {
			return false;
		}

		List<User> users = repository.findAll();
		for (User user : users) {
			if (user.getUsername().equals(userDto.getUsername())) {
				user.setFirstName(userDto.getFirstName());
				user.setLastName(userDto.getLastName());
				user.setAddress(userDto.getAddress());
				user.setEmail(userDto.getEmail());
				user.setStatus(userDto.getStatus());

				repository.saveAll(users);
				return true;
			}
		}
		return false;
	}

	private int getNextId(List<User> users) {
		return users.isEmpty() ? 1 : users.stream().mapToInt(User::getId).max().orElse(0) + 1;
	}
}
