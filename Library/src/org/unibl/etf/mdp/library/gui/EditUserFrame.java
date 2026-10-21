package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.UserDto;

import javax.swing.*;
import java.awt.*;

/** Edits a member's profile and status; the caller saves the result. */
public class EditUserFrame extends JDialog {
    private static final long serialVersionUID = 1L;
    private final UserDto user;
    private final JTextField firstNameField;
    private final JTextField lastNameField;
    private final JTextField addressField;
    private final JTextField emailField;
    private final JComboBox<StatusEnum> statusComboBox = new JComboBox<>(StatusEnum.values());
    private boolean saved;

    public EditUserFrame(Window owner, UserDto user) {
        super(owner, "Edit member: " + user.getUsername(), ModalityType.APPLICATION_MODAL);
        this.user = user;
        firstNameField = new JTextField(user.getFirstName(), 20);
        lastNameField = new JTextField(user.getLastName(), 20);
        addressField = new JTextField(user.getAddress(), 20);
        emailField = new JTextField(user.getEmail(), 20);
        statusComboBox.setSelectedItem(user.getStatus());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("First Name:"));
        form.add(firstNameField);
        form.add(new JLabel("Last Name:"));
        form.add(lastNameField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Status:"));
        form.add(statusComboBox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(saveButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(saveButton);
        pack();
        setLocationRelativeTo(owner);
    }

    private void save() {
        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
                || addressField.getText().isBlank() || emailField.getText().isBlank()) {
            BookViews.error(this, "All fields are required.");
            return;
        }
        user.setFirstName(firstNameField.getText().trim());
        user.setLastName(lastNameField.getText().trim());
        user.setAddress(addressField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setStatus((StatusEnum) statusComboBox.getSelectedItem());
        saved = true;
        dispose();
    }

    /** @return true when the member was changed and should be saved */
    public boolean showDialog() {
        setVisible(true);
        return saved;
    }
}
