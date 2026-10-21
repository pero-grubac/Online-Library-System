package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.library.services.UserService;
import org.unibl.etf.mdp.model.StatusEnum;
import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.UserDto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** All member accounts: approve or reject registrations, block, add, edit and delete. */
public class UserFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String ALL = "All";
    private final DefaultTableModel tableModel;
    private final JTable userTable;
    private final JComboBox<Object> filter = new JComboBox<>();
    private List<UserDto> users = new ArrayList<>();
    private List<UserDto> shown = new ArrayList<>();

    public UserFrame() {
        super("Members");
        setSize(900, 520);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Username", "First Name", "Last Name", "Address", "Email", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(24);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Show:"));
        filter.addItem(ALL);
        for (StatusEnum status : StatusEnum.values()) {
            filter.addItem(status);
        }
        filter.addActionListener(e -> populateTable());
        top.add(filter);
        top.add(button("Refresh", this::loadUsers));
        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(button("Add member", this::addMember));
        buttons.add(button("Edit", this::editMember));
        buttons.add(button("Approve", () -> setStatus(StatusEnum.APPROVED)));
        buttons.add(button("Reject", () -> setStatus(StatusEnum.REJECTED)));
        buttons.add(button("Block", () -> setStatus(StatusEnum.BLOCKED)));
        buttons.add(button("Delete", this::deleteMember));
        add(buttons, BorderLayout.SOUTH);
        loadUsers();
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void loadUsers() {
        Tasks.load(this, "The members could not be loaded.", () -> UserService.getInstance().getAll(), loaded -> {
            users = loaded;
            populateTable();
        });
    }

    private void populateTable() {
        Object selected = filter.getSelectedItem();
        shown = users.stream().filter(u -> ALL.equals(selected) || u.getStatus() == selected)
                .collect(Collectors.toList());
        tableModel.setRowCount(0);
        for (UserDto user : shown) {
            tableModel.addRow(new Object[]{user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
                    user.getAddress(), user.getEmail(), user.getStatus()});
        }
    }

    private UserDto selected() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            BookViews.error(this, "Select a member first.");
            return null;
        }
        return shown.get(userTable.convertRowIndexToModel(row));
    }

    private void setStatus(StatusEnum status) {
        UserDto user = selected();
        if (user != null) {
            Tasks.run(this, "The status was not changed.",
                    () -> UserService.getInstance().changeStatus(user.getUsername(), status), this::loadUsers);
        }
    }

    private void editMember() {
        UserDto user = selected();
        if (user != null && new EditUserFrame(this, user).showDialog()) {
            Tasks.run(this, "The member was not saved.", () -> UserService.getInstance().update(user), this::loadUsers);
        }
    }

    private void deleteMember() {
        UserDto user = selected();
        if (user != null && JOptionPane.showConfirmDialog(this, "Delete the account of " + user.getUsername() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Tasks.run(this, "The member was not deleted.", () -> UserService.getInstance().delete(user),
                    this::loadUsers);
        }
    }

    private void addMember() {
        JTextField username = new JTextField(18);
        JTextField firstName = new JTextField(18);
        JTextField lastName = new JTextField(18);
        JTextField address = new JTextField(18);
        JTextField email = new JTextField(18);
        JPasswordField password = new JPasswordField(18);
        JPasswordField confirm = new JPasswordField(18);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        String[] labels = {"Username:", "First name:", "Last name:", "Address:", "Email:", "Password:",
                "Confirm password:"};
        JComponent[] fields = {username, firstName, lastName, address, email, password, confirm};
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            form.add(fields[i]);
        }
        while (JOptionPane.showConfirmDialog(this, form, "Add member", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String pass = new String(password.getPassword());
            if (username.getText().isBlank() || firstName.getText().isBlank() || lastName.getText().isBlank()
                    || address.getText().isBlank() || email.getText().isBlank() || pass.isEmpty()) {
                BookViews.error(this, "All fields are required.");
            } else if (!pass.equals(new String(confirm.getPassword()))) {
                BookViews.error(this, "Passwords do not match.");
            } else {
                User user = new User(firstName.getText().trim(), lastName.getText().trim(), address.getText().trim(),
                        email.getText().trim(), username.getText().trim(), pass);
                Tasks.run(this, "The member was not added.", () -> UserService.getInstance().create(user),
                        this::loadUsers);
                return;
            }
        }
    }
}
