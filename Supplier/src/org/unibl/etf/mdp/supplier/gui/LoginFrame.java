package org.unibl.etf.mdp.supplier.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.unibl.etf.mdp.model.BookSupplier;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import java.util.logging.Logger;

public class LoginFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = FileLogger.getLogger(LoginFrame.class.getName());
    private JComboBox<String> supplierComboBox;

	public LoginFrame() {
		super("Login");
		 setSize(300, 150);
	        setLayout(new GridBagLayout());
	        GridBagConstraints gbc = new GridBagConstraints();
	        gbc.insets = new Insets(5, 5, 5, 5);

	        JLabel label = new JLabel("Select Supplier:");
	        gbc.gridx = 0;
	        gbc.gridy = 0;
	        add(label, gbc);

	        supplierComboBox = new JComboBox<>();
	        for (BookSupplier supplier : BookSupplier.values()) {
	            supplierComboBox.addItem(supplier.getName());
	        }
	        gbc.gridx = 1;
	        add(supplierComboBox, gbc);

	        JButton loginButton = new JButton("Login");
	        gbc.gridx = 0;
	        gbc.gridy = 1;
	        gbc.gridwidth = 2;
	        add(loginButton, gbc);

	        loginButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String selectedSupplier = (String) supplierComboBox.getSelectedItem();
	                new MainFrame(selectedSupplier).setVisible(true);
	                dispose(); 
	            }
	        });
	}

}
