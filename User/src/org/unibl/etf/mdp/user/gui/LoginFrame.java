package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.user.service.LibraryService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends GeneralFrame {
    private static final long serialVersionUID = 1L;
    private static final LibraryService service = LibraryService.getInstance();

    public LoginFrame() {
        super("Login");
        setSize(400, 220);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        mainPanel.add(createLabeledField("Username:", usernameField));
        mainPanel.add(createLabeledField("Password:", passwordField));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                BookViews.error(this, "Both fields are required!");
                return;
            }
            // The server's reason is shown, e.g. that the account still waits for the library's approval
            Tasks.run(this, "Login failed.", () -> service.login(username, password), () -> {
                dispose();
                new MainFrame(username).setVisible(true);
            });
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
        getRootPane().setDefaultButton(loginButton);
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
