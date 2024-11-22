package org.unibl.etf.mdp.user.gui;

import java.awt.*;
import javax.swing.*;

import org.unibl.etf.mdp.user.service.LibraryService;

public class LoginFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private static final LibraryService service = LibraryService.getInstance();

	public LoginFrame() {
		super("Login");
		setSize(400, 300);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		mainPanel.add(createLabeledField("Username:", new JTextField()));
		JTextField usernameField = (JTextField) ((JPanel) mainPanel.getComponent(mainPanel.getComponentCount() - 1))
				.getComponent(1);

		mainPanel.add(createLabeledField("Password:", new JPasswordField()));
		JPasswordField passwordField = (JPasswordField) ((JPanel) mainPanel
				.getComponent(mainPanel.getComponentCount() - 1)).getComponent(1);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JButton loginButton = new JButton("Login");
		JButton registerButton = new JButton("Register");

		loginButton.addActionListener(e -> {
			String username = usernameField.getText();
			String password = new String(passwordField.getPassword());

			if (username.isEmpty() || password.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Both fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			boolean loggedIn = service.login(username, password);
			if (loggedIn) {
				JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
				dispose();
				new MainFrame(username).setVisible(true);
			} else {
				JOptionPane.showMessageDialog(this, "Invalid username or password!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		registerButton.addActionListener(e -> {
			dispose();
			new RegistrationFrame().setVisible(true);
		});

		buttonPanel.add(loginButton);
		buttonPanel.add(registerButton);

		mainPanel.add(Box.createVerticalStrut(10));
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
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		return panel;
	}
}
