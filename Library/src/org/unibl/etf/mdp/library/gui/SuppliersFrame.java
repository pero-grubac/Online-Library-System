package org.unibl.etf.mdp.library.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.unibl.etf.mdp.library.services.DiscoveryServerService;

public class SuppliersFrame extends GeneralFrame {

    private static final long serialVersionUID = 1L;
    private JComboBox<String> supplierComboBox;
    private Map<String, String> suppliers;

    public SuppliersFrame() {
        super("Choose Supplier");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(400, 200);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel label = new JLabel("Select Supplier:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(label, gbc);

        supplierComboBox = new JComboBox<>();
        refreshSuppliers();

        gbc.gridx = 1;
        add(supplierComboBox, gbc);

        JButton selectButton = new JButton("Select");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(selectButton, gbc);

        JButton refreshButton = new JButton("Refresh");
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(refreshButton, gbc);

        // Akcija za dugme Select
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSupplier = (String) supplierComboBox.getSelectedItem();
                if (selectedSupplier != null) {
                    int port = Integer.parseInt(suppliers.get(selectedSupplier));
                    System.out.println(port + " " + selectedSupplier);
                    new SuppliersBookFrame(selectedSupplier, port).setVisible(true);
                    dispose();
                }
            }
        });

        // Akcija za dugme Refresh
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshSuppliers();
            }
        });
    }

    private void refreshSuppliers() {
        suppliers = DiscoveryServerService.getSuppliers();
        supplierComboBox.removeAllItems();
        for (String supplierName : suppliers.keySet()) {
            supplierComboBox.addItem(supplierName);
        }
    }
}
