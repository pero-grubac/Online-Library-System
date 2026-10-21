package org.unibl.etf.mdp.supplier.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.model.Invoice;
import org.unibl.etf.mdp.model.OrderItem;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/** Orders from the library, oldest first, each to approve (with an invoice) or reject. */
public class RequestsFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
    private final Data data;
    private final JPanel requestsPanel = new JPanel();
    private final Runnable listener = () -> SwingUtilities.invokeLater(this::refreshRequests);

    public RequestsFrame(Data data) {
        super("Requests - " + data.getUsername());
        this.data = data;
        setSize(640, 520);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(requestsPanel), BorderLayout.CENTER);
        data.addListener(listener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                data.removeListener(listener);
            }
        });
        setLocationRelativeTo(null);
        refreshRequests();
    }

    private void refreshRequests() {
        requestsPanel.removeAll();
        if (data.getRequests().isEmpty()) {
            requestsPanel.add(new JLabel("  No open requests."));
        }
        int n = 1;
        for (Data.Request request : data.getRequests()) {
            requestsPanel.add(createRequestPanel(request, n++));
        }
        requestsPanel.revalidate();
        requestsPanel.repaint();
    }

    private JPanel createRequestPanel(Data.Request request, int number) {
        JPanel requestPanel = new JPanel(new BorderLayout(8, 8));
        requestPanel.setBorder(BorderFactory.createTitledBorder(
                "Request " + number + " - received " + request.getReceived().format(TIME)));
        requestPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60 + 20 * request.getItems().size()));

        StringBuilder books = new StringBuilder("<html>");
        for (OrderItem item : request.getItems()) {
            books.append(item.getBook().getTitle()).append(" - ").append(item.getBook().getAuthor())
                    .append(" &nbsp;<b>x ").append(item.getQuantity()).append("</b><br>");
        }
        requestPanel.add(new JLabel(books.append("</html>").toString()), BorderLayout.CENTER);

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        approveButton.addActionListener(e -> decide(request, true, approveButton, rejectButton));
        rejectButton.addActionListener(e -> decide(request, false, approveButton, rejectButton));
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        requestPanel.add(buttonPanel, BorderLayout.EAST);
        return requestPanel;
    }

    /** Runs in the background; the request stays open if any step fails. */
    private void decide(Data.Request request, boolean approve, JButton... buttons) {
        for (JButton b : buttons) {
            b.setEnabled(false);
        }
        new SwingWorker<Invoice, Void>() {
            @Override
            protected Invoice doInBackground() throws Exception {
                if (approve) {
                    return data.approve(request);
                }
                data.reject(request);
                return null;
            }

            @Override
            protected void done() {
                try {
                    Invoice invoice = get();
                    if (approve) {
                        JTextArea text = new JTextArea(invoice.toString());
                        text.setEditable(false);
                        JOptionPane.showMessageDialog(RequestsFrame.this, text,
                                "Request approved - VAT to pay: " + String.format(Locale.ROOT, "%.2f", invoice.getVAT()),
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        BookViews.info(RequestsFrame.this, "Request rejected; the library has been told.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    for (JButton b : buttons) {
                        b.setEnabled(true);
                    }
                    BookViews.error(RequestsFrame.this, (approve ? "The request was not approved: "
                            : "The request was not rejected: ") + e.getCause().getMessage()
                            + "\nIt stays open; try again.");
                }
            }
        }.execute();
    }
}
