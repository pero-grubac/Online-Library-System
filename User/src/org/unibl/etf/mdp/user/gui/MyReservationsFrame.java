package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.user.service.ReservationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/** The member's reservations and the library's decisions. */
public class MyReservationsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Book", "Author", "Requested", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public MyReservationsFrame() {
        super("My reservations");
        setSize(640, 360);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JTable table = new JTable(model);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        add(south, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        Tasks.load(this, "The reservations could not be loaded.", () -> ReservationService.getInstance().mine(),
                reservations -> {
                    model.setRowCount(0);
                    for (Reservation r : reservations) {
                        model.addRow(new Object[]{r.getBookTitle(), r.getBookAuthor(), r.getCreatedAt().format(TIME),
                                r.getStatus()});
                    }
                });
    }
}
