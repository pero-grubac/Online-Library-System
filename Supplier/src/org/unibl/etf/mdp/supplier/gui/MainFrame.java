package org.unibl.etf.mdp.supplier.gui;

import org.unibl.etf.mdp.common.ui.BookForm;
import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.supplier.app.App;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The supplier's offer: loaded from linkovi.txt by the SupplierServer, extended by hand or by link. */
public class MainFrame extends GeneralFrame {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = FileLogger.getLogger(MainFrame.class.getName());
    private final Data data;
    private final JPanel bookListPanel = new JPanel();
    private final JLabel status = new JLabel(" ");
    private final JButton requestsButton = new JButton("Requests");
    private final JButton reloadButton = new JButton("Reload offer");
    private RequestsFrame requestsFrame;

    public MainFrame(Data data) {
        super(data.getUsername());
        this.data = data;
        setSize(900, 680);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBookButton = new JButton("Add book");
        JButton addLinkButton = new JButton("Add from Gutenberg link");
        topPanel.add(requestsButton);
        topPanel.add(addBookButton);
        topPanel.add(addLinkButton);
        topPanel.add(reloadButton);
        add(topPanel, BorderLayout.NORTH);

        bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(bookListPanel), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);

        requestsButton.addActionListener(e -> openRequests());
        addBookButton.addActionListener(e -> addBook());
        addLinkButton.addActionListener(e -> addFromLink());
        reloadButton.addActionListener(e -> loadOffer());
        data.addListener(() -> SwingUtilities.invokeLater(this::refresh));

        if (data.getQueueProblem() != null) {
            SwingUtilities.invokeLater(() -> BookViews.error(this, data.getQueueProblem()));
        }
        if (App.getAccountingService() == null) {
            SwingUtilities.invokeLater(() -> BookViews.error(this,
                    "The AccountingServer is not reachable. Orders can be approved once it is running."));
        }
        loadOffer();
    }

    private void refresh() {
        bookListPanel.removeAll();
        for (BookDto book : data.getBooks()) {
            JButton preview = new JButton("Details");
            preview.addActionListener(e -> BookViews.showDetails(this, book));
            bookListPanel.add(BookViews.bookRow(book, preview));
        }
        bookListPanel.revalidate();
        bookListPanel.repaint();
        int open = data.getRequests().size();
        requestsButton.setText(open == 0 ? "Requests" : "Requests (" + open + ")");
        if (!reloadButton.isEnabled()) {
            return;
        }
        status.setText(BookViews.books(data.getBooks().size()) + " on offer");
    }

    /** Downloads in the background; books appear one by one as the SupplierServer sends them. */
    private void loadOffer() {
        reloadButton.setEnabled(false);
        status.setText("Loading the offer from linkovi.txt...");
        new SwingWorker<SupplierServerService.Offer, BookDto>() {
            @Override
            protected SupplierServerService.Offer doInBackground() throws Exception {
                return data.getServerService().loadOffer(data.getUsername(), this::publish);
            }

            @Override
            protected void process(List<BookDto> chunk) {
                status.setText("Downloaded: " + chunk.get(chunk.size() - 1).getTitle());
            }

            @Override
            protected void done() {
                reloadButton.setEnabled(true);
                try {
                    SupplierServerService.Offer offer = get();
                    data.replaceOffer(offer.books);
                    if (!offer.errors.isEmpty()) {
                        BookViews.error(MainFrame.this, "Some books could not be loaded:\n- "
                                + String.join("\n- ", offer.errors));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "Loading the offer failed", e);
                    BookViews.error(MainFrame.this, "Cannot reach the SupplierServer: " + e.getCause().getMessage());
                }
                refresh();
            }
        }.execute();
    }

    private void addBook() {
        Book book = new BookForm(this, "Add a book to the offer", null, true, false).showDialog();
        if (book != null) {
            runInBackground("Saving \"" + book.getTitle() + "\"...",
                    () -> data.getServerService().addBook(data.getUsername(), book));
        }
    }

    private void addFromLink() {
        String link = JOptionPane.showInputDialog(this,
                "Project Gutenberg link, e.g. https://www.gutenberg.org/cache/epub/24022/pg24022.txt",
                "Add from link", JOptionPane.PLAIN_MESSAGE);
        if (link != null && !link.isBlank()) {
            runInBackground("Downloading " + link.trim() + "...",
                    () -> data.getServerService().addFromLink(data.getUsername(), link.trim()));
        }
    }

    private interface BookTask {
        BookDto run() throws Exception;
    }

    private void runInBackground(String message, BookTask task) {
        status.setText(message);
        new SwingWorker<BookDto, Void>() {
            @Override
            protected BookDto doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                try {
                    BookDto book = get();
                    data.addBook(book);
                    status.setText("Added \"" + book.getTitle() + "\" to the offer");
                } catch (InterruptedException | ExecutionException e) {
                    status.setText(" ");
                    BookViews.error(MainFrame.this, "The book was not added: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void openRequests() {
        if (requestsFrame == null || !requestsFrame.isDisplayable()) {
            requestsFrame = new RequestsFrame(data);
        }
        requestsFrame.setVisible(true);
        requestsFrame.toFront();
    }
}
