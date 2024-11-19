package org.unibl.etf.mdp.library.gui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class MainFrame extends GeneralFrame {

    private static final long serialVersionUID = 1L;

    public MainFrame() {
        super("Library");
        setSize(800, 600);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        JButton chooseSupplierButton = new JButton("Choose Supplier");
        chooseSupplierButton.addActionListener(e -> new SuppliersFrame().setVisible(true));

        panel.add(chooseSupplierButton);
        add(panel, BorderLayout.NORTH);
    }


}
