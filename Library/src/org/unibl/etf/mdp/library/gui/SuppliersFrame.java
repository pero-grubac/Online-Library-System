package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.library.services.DiscoveryServerService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/** The suppliers that are online right now, from the DiscoveryServer. */
public class SuppliersFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JComboBox<String> supplierComboBox = new JComboBox<>();
    private Map<String, String> suppliers = new HashMap<>();

    public SuppliersFrame() {
        super("Choose Supplier");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 170);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Supplier:"), gbc);
        supplierComboBox.setPrototypeDisplayValue("BookSupplier_XXXXXXXX");
        gbc.gridx = 1;
        add(supplierComboBox, gbc);

        JButton selectButton = new JButton("Show offer");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(selectButton, gbc);
        JButton refreshButton = new JButton("Refresh");
        gbc.gridx = 1;
        add(refreshButton, gbc);

        selectButton.addActionListener(e -> {
            String selected = (String) supplierComboBox.getSelectedItem();
            if (selected == null) {
                BookViews.error(this, "No supplier is online.");
                return;
            }
            JFrame offer = new SuppliersBookFrame(Data.getInstance(), selected, Integer.parseInt(suppliers.get(selected)));
            offer.setLocationRelativeTo(this);
            offer.setVisible(true);
        });
        refreshButton.addActionListener(e -> refreshSuppliers());
        refreshSuppliers();
    }

    private void refreshSuppliers() {
        Tasks.load(this, "The suppliers could not be loaded.", DiscoveryServerService::getSuppliers, found -> {
            suppliers = found;
            supplierComboBox.removeAllItems();
            found.keySet().stream().sorted().forEach(supplierComboBox::addItem);
        });
    }
}
