package org.unibl.etf.mdp.supplier.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.supplier.server.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoginFrame extends GeneralFrame {
    private static final long serialVersionUID = 1L;
    private final JComboBox<String> supplierComboBox = new JComboBox<>();

    public LoginFrame() {
        super("Supplier login");
        setSize(340, 160);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Supplier:"), gbc);
        conf.getSupplierNames().forEach(supplierComboBox::addItem);
        gbc.gridx = 1;
        add(supplierComboBox, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(loginButton, gbc);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(e -> login((String) supplierComboBox.getSelectedItem()));
    }

    private void login(String supplier) {
        if (supplier == null) {
            return;
        }
        try {
            Data data = Data.start(supplier);
            new MainFrame(data).setVisible(true);
            dispose();
        } catch (Server.NameTakenException e) {
            BookViews.error(this, supplier + " is already running in another window.\n"
                    + "Choose another supplier, or wait half a minute if that window was just closed.");
        } catch (IOException e) {
            BookViews.error(this, "Cannot register with the Discovery Server: " + e.getMessage()
                    + "\nStart the DiscoveryServer and try again.");
        }
    }
}
