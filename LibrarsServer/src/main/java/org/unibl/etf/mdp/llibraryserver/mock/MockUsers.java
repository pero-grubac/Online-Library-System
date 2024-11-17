package org.unibl.etf.mdp.llibraryserver.mock;

import java.util.ArrayList;
import java.util.List;

import org.unibl.etf.mdp.libraryserver.service.UserService;
import org.unibl.etf.mdp.model.User;

public class MockUsers {

	public static void createUsers() {
		List<User> mockUsers = new ArrayList<>();
		mockUsers.add(new User("John", "Doe", "123 Main St", "john.doe@example.com", "john", "john"));
		mockUsers.add(new User("Jane", "Smith", "456 Elm St", "jane.smith@example.com", "jane", "jane"));
		mockUsers.add(new User("Michael", "Brown", "789 Oak St", "michael.brown@example.com", "michael", "michael"));
		mockUsers.add(new User("Emily", "Jones", "321 Pine St", "emily.jones@example.com", "emily", "emily"));
		mockUsers.add(new User("David", "Johnson", "654 Cedar St", "david.johnson@example.com", "david", "david"));
		mockUsers.add(new User("Sarah", "Williams", "987 Maple St", "sarah.williams@example.com", "sarah", "sarah"));
		mockUsers.add(new User("Chris", "Taylor", "135 Birch St", "chris.taylor@example.com", "chris", "chris"));
		mockUsers.add(new User("Anna", "Moore", "246 Walnut St", "anna.moore@example.com", "anna", "anna"));
		mockUsers.add(new User("James", "Anderson", "579 Spruce St", "james.anderson@example.com", "james", "james"));
		mockUsers.add(new User("Laura", "Martinez", "864 Poplar St", "laura.martinez@example.com", "laura", "laura"));

		UserService service = new UserService();

		for (User user : mockUsers) {
			service.add(user);
		}

		service.approver("john");
		service.block("jane");
		service.reject("michael");
	}
}
