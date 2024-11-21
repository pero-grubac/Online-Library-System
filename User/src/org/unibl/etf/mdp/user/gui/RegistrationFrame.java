package org.unibl.etf.mdp.user.gui;

import java.awt.*;
import javax.swing.*;

import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.user.service.LibraryService;

public class RegistrationFrame extends GeneralFrame {

    private static final long serialVersionUID = 1L;
    private static final LibraryService service = LibraryService.getInstance();

    public RegistrationFrame() {
        super("Register");
        setSize(400, 600);

        // Glavni panel sa BoxLayout za vertikalno poravnanje
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dodavanje polja za unos
        mainPanel.add(createLabeledField("Username:", new JTextField()));
        JTextField usernameField = (JTextField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        mainPanel.add(createLabeledField("First Name:", new JTextField()));
        JTextField firstNameField = (JTextField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        mainPanel.add(createLabeledField("Last Name:", new JTextField()));
        JTextField lastNameField = (JTextField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        mainPanel.add(createLabeledField("Email:", new JTextField()));
        JTextField emailField = (JTextField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        mainPanel.add(createLabeledField("Address:", new JTextField()));
        JTextField addressField = (JTextField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        mainPanel.add(createLabeledField("Password:", new JPasswordField()));
        JPasswordField passwordField = (JPasswordField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        mainPanel.add(createLabeledField("Confirm Password:", new JPasswordField()));
        JPasswordField confirmPasswordField = (JPasswordField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

        // Dodavanje dugmadi
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Back to Login");

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String address = addressField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || address.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = new User(0, firstName, lastName, address, email, username, password);

            boolean registered = service.register(user);
            if (registered) {
                JOptionPane.showMessageDialog(this, "Registration successful!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);

        // Dodavanje na glavni okvir
        mainPanel.add(Box.createVerticalStrut(10)); // Razmak između poslednjeg polja i dugmadi
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(120, 25));
        field.setPreferredSize(new Dimension(200, 25));

        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Razmak između polja

        return panel;
    }
}
