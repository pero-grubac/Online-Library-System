package org.unibl.etf.mdp.libraryservice.mock;

import java.util.ArrayList;
import java.util.List;

import org.unibl.etf.mdp.libraryserver.repository.UserRepository;
import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;

public class MockUsers {

	public static void createUsers() {
		List<User> mockUsers = new ArrayList<>();
		mockUsers.add(
				new User("John", "Doe", "123 Main St", "john.doe@example.com", "john", "john", StatusEnum.APPROVED));
		mockUsers.add(
				new User("Jane", "Smith", "456 Elm St", "jane.smith@example.com", "jane", "jane", StatusEnum.REJECTED));
		mockUsers.add(new User("Michael", "Brown", "789 Oak St", "michael.brown@example.com", "michael", "michael",
				StatusEnum.BLOCKED));
		mockUsers.add(new User("Emily", "Jones", "321 Pine St", "emily.jones@example.com", "emily", "emily",
				StatusEnum.APPROVED));
		mockUsers.add(new User("David", "Johnson", "654 Cedar St", "david.johnson@example.com", "david", "david",
				StatusEnum.REJECTED));
		mockUsers.add(new User("Sarah", "Williams", "987 Maple St", "sarah.williams@example.com", "sarah", "sarah",
				StatusEnum.BLOCKED));
		mockUsers.add(new User("Chris", "Taylor", "135 Birch St", "chris.taylor@example.com", "chris", "chris",
				StatusEnum.APPROVED));
		mockUsers.add(new User("Anna", "Moore", "246 Walnut St", "anna.moore@example.com", "anna", "anna",
				StatusEnum.APPROVED));
		mockUsers.add(new User("James", "Anderson", "579 Spruce St", "james.anderson@example.com", "james", "james",
				StatusEnum.APPROVED));
		mockUsers.add(new User("Laura", "Martinez", "864 Poplar St", "laura.martinez@example.com", "laura", "laura",
				StatusEnum.APPROVED));
		mockUsers.add(new User("Ema", "Martinez", "864 Poplar St", "ema.martinez@example.com", "ema", "ema",
				StatusEnum.APPROVED));

		UserService service = new UserService();
		for (User user : mockUsers) {
			service.add(user);
		}
	}
}
