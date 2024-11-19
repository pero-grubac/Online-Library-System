package org.unibl.etf.mdp.library.gui;

import javax.swing.*;

import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.UserDto;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class EditUserFrame extends JFrame {


	private static final long serialVersionUID = 1L;
	private final UserDto user;
    private final Consumer<UserDto> callback;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField addressField;
    private JTextField emailField;
    private JComboBox<StatusEnum> statusComboBox;

    public EditUserFrame(UserDto user, Consumer<UserDto> callback) {
        this.user = user;
        this.callback = callback;
        initializeFrame();
    }

    private void initializeFrame() {
        setTitle("Edit User: " + user.getUsername());
        setSize(400, 300);
        setLayout(new GridLayout(6, 2, 10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        add(new JLabel("First Name:"));
        firstNameField = new JTextField(user.getFirstName());
        add(firstNameField);

        add(new JLabel("Last Name:"));
        lastNameField = new JTextField(user.getLastName());
        add(lastNameField);

        add(new JLabel("Address:"));
        addressField = new JTextField(user.getAddress());
        add(addressField);

        add(new JLabel("Email:"));
        emailField = new JTextField(user.getEmail());
        add(emailField);

        add(new JLabel("Status:"));
        statusComboBox = new JComboBox<>(StatusEnum.values()); // Korišćenje values()
        statusComboBox.setSelectedItem(user.getStatus());
        add(statusComboBox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this::saveUserChanges);
        add(saveButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteUser());
        add(deleteButton);
    }

    private void saveUserChanges(ActionEvent e) {
        boolean modified = false;

        if (!firstNameField.getText().equals(user.getFirstName())) {
            user.setFirstName(firstNameField.getText());
            modified = true;
        }
        if (!lastNameField.getText().equals(user.getLastName())) {
            user.setLastName(lastNameField.getText());
            modified = true;
        }
        if (!addressField.getText().equals(user.getAddress())) {
            user.setAddress(addressField.getText());
            modified = true;
        }
        if (!emailField.getText().equals(user.getEmail())) {
            user.setEmail(emailField.getText());
            modified = true;
        }

        StatusEnum selectedStatus = (StatusEnum) statusComboBox.getSelectedItem();
        if (selectedStatus != user.getStatus()) {
            user.setStatus(selectedStatus);
            Data.getUserService().changeStatus(user.getUsername(), selectedStatus);
            modified = true;
        }

        if (modified) {
            Data.getUserService().update(user);
            callback.accept(user);
            JOptionPane.showMessageDialog(this, "User updated successfully!", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No changes made.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        dispose();
    }

    private void deleteUser() {
        Data.getUserService().delete(user);
        callback.accept(null);
        JOptionPane.showMessageDialog(this, "User deleted successfully!", "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
