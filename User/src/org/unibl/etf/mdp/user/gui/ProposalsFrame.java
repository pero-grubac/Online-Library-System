package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.model.Proposal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Proposals for new books: a member's proposal goes by multicast to the librarians and all members,
 * and the library's answer comes back to everyone the same way.
 */
public class ProposalsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Data data;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"From", "Title", "Author", "Note", "Library's decision", "Answer"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final Runnable listener = () -> SwingUtilities.invokeLater(this::populate);

    public ProposalsFrame(Data data) {
        super("Book proposals");
        this.data = data;
        setSize(880, 440);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JTextField title = new JTextField(16);
        JTextField author = new JTextField(14);
        JTextField note = new JTextField(18);
        JButton send = new JButton("Propose");
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Title:"));
        form.add(title);
        form.add(new JLabel("Author:"));
        form.add(author);
        form.add(new JLabel("Why:"));
        form.add(note);
        form.add(send);
        add(form, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new JLabel("  Proposals and answers sent while this application runs appear here."), BorderLayout.SOUTH);

        send.addActionListener(e -> {
            if (title.getText().isBlank() || author.getText().isBlank()) {
                BookViews.error(this, "Title and author are required.");
                return;
            }
            Tasks.run(this, "The proposal was not sent.",
                    () -> data.propose(title.getText().trim(), author.getText().trim(), note.getText().trim()), () -> {
                        title.setText("");
                        author.setText("");
                        note.setText("");
                    });
        });
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
        model.setRowCount(0);
        for (Proposal p : data.getProposals().getProposals()) {
            model.addRow(new Object[]{p.getUsername(), p.getTitle(), p.getAuthor(), p.getNote(), p.getStatusText(),
                    p.getAnswer()});
        }
    }
}
