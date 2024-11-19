package org.unibl.etf.mdp.library.gui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.unibl.etf.mdp.model.UserDto;

public class UserFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private JTable userTable;
	private DefaultTableModel tableModel;
	private List<UserDto> users;

	public UserFrame() {
		super("Users");
		setSize(800, 600);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		this.users = Data.getUserService().getAll();

		String[] columnNames = { "ID", "First Name", "Last Name", "Address", "Email", "Username", "Status" };
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		userTable = new JTable(tableModel);
		populateTable(users);

		JScrollPane scrollPane = new JScrollPane(userTable);
		add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton editButton = new JButton("Edit");
		editButton.addActionListener(e -> openEditUserFrame());
		buttonPanel.add(editButton);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void populateTable(List<UserDto> users) {
		tableModel.setRowCount(0);
		for (UserDto user : users) {
			tableModel.addRow(new Object[] { user.getId(), user.getFirstName(), user.getLastName(), user.getAddress(),
					user.getEmail(), user.getUsername(), user.getStatus().toString() });
		}
	}

	private void openEditUserFrame() {
		int selectedRow = userTable.getSelectedRow();
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "No user selected!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		UserDto user = users.get(selectedRow);
		new EditUserFrame(user, updatedUser -> {
			if (updatedUser == null) {
				users.remove(user);
			} else {
				int index = users.indexOf(user);
				users.set(index, updatedUser);
			}
			populateTable(users);
		}).setVisible(true);
	}
}
