package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.library.services.DeliveryService;

import javax.swing.*;
import java.awt.*;

/** Invoices and rejections received from suppliers, newest first. */
public class NoticesFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JTextArea text = new JTextArea();
    private final DeliveryService deliveries;

    public NoticesFrame(DeliveryService deliveries) {
        super("Notices from suppliers");
        this.deliveries = deliveries;
        setSize(620, 460);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(text), BorderLayout.CENTER);
        deliveries.onNotice(() -> SwingUtilities.invokeLater(this::refresh));
        refresh();
    }

    private void refresh() {
        String all = String.join("\n----------------------------------------\n", deliveries.getNotices());
        text.setText(all.isEmpty() ? "Nothing yet." : all);
        text.setCaretPosition(0);
    }
}
