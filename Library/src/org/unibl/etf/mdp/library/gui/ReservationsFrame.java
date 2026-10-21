package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.library.services.ReservationService;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.model.ReservationStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** The members' book reservations, to confirm or reject. */
public class ReservationsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JCheckBox pendingOnly = new JCheckBox("Pending only", true);
    private List<Reservation> shown = new ArrayList<>();
    private List<Reservation> all = new ArrayList<>();

    public ReservationsFrame() {
        super("Reservations");
        setSize(820, 460);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"#", "Member", "Book", "Author", "Requested", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pendingOnly.addActionListener(e -> populate());
        top.add(pendingOnly);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> load());
        top.add(refresh);
        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approve = new JButton("Confirm");
        approve.addActionListener(e -> decide(ReservationStatus.APPROVED));
        JButton reject = new JButton("Reject");
        reject.addActionListener(e -> decide(ReservationStatus.REJECTED));
        buttons.add(approve);
        buttons.add(reject);
        add(buttons, BorderLayout.SOUTH);
        load();
    }

    private void load() {
        Tasks.load(this, "The reservations could not be loaded.", () -> ReservationService.getInstance().getAll(),
                loaded -> {
                    all = loaded;
                    populate();
                });
    }

    private void populate() {
        shown = all.stream().filter(r -> !pendingOnly.isSelected() || r.getStatus() == ReservationStatus.PENDING)
                .collect(Collectors.toList());
        tableModel.setRowCount(0);
        for (Reservation r : shown) {
            tableModel.addRow(new Object[]{r.getId(), r.getUsername(), r.getBookTitle(), r.getBookAuthor(),
                    r.getCreatedAt().format(TIME), r.getStatus()});
        }
    }

    private void decide(ReservationStatus decision) {
        int row = table.getSelectedRow();
        if (row == -1) {
            BookViews.error(this, "Select a reservation first.");
            return;
        }
        Reservation reservation = shown.get(row);
        Tasks.run(this, "The reservation was not changed.",
                () -> ReservationService.getInstance().decide(reservation, decision), this::load);
    }
}
