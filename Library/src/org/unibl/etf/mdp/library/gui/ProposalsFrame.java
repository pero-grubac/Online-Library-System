package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.model.Proposal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Members' proposals for new books, arriving by multicast; the answer goes back to everyone the same way. */
public class ProposalsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Data data;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Proposal> shown = new ArrayList<>();
    private final Runnable listener = () -> SwingUtilities.invokeLater(this::populate);

    public ProposalsFrame(Data data) {
        super("Book proposals");
        this.data = data;
        setSize(900, 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"From", "Title", "Author", "Note", "Decision", "Answer"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton accept = new JButton("Will be acquired");
        accept.addActionListener(e -> answer(true));
        JButton decline = new JButton("Will not be acquired");
        decline.addActionListener(e -> answer(false));
        buttons.add(accept);
        buttons.add(decline);
        buttons.add(new JLabel("  Proposals arrive while the library application is running."));
        add(buttons, BorderLayout.SOUTH);

        data.getProposals().addListener(listener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                data.getProposals().removeListener(listener);
            }
        });
        populate();
    }

    private void populate() {
        shown = data.getProposals().getProposals();
        tableModel.setRowCount(0);
        for (Proposal p : shown) {
            tableModel.addRow(new Object[]{p.getUsername(), p.getTitle(), p.getAuthor(), p.getNote(),
                    p.getStatusText(), p.getAnswer()});
        }
    }

    private void answer(boolean accepted) {
        int row = table.getSelectedRow();
        if (row == -1) {
            BookViews.error(this, "Select a proposal first.");
            return;
        }
        Proposal proposal = shown.get(row);
        if (proposal.isAnswered()) {
            BookViews.error(this, "This proposal has already been answered.");
            return;
        }
        String message = JOptionPane.showInputDialog(this, "Message to the members (optional):",
                accepted ? "We will order it." : "Not this time.");
        if (message != null) {
            Tasks.run(this, "The answer was not sent.", () -> data.answerProposal(proposal, accepted, message.trim()),
                    () -> { });
        }
    }
}
