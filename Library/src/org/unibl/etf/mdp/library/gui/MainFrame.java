package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookForm;
import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The library's catalogue (CRUD) and the way to every other part of the application. */
public class MainFrame extends GeneralFrame {
    private static final long serialVersionUID = 1L;
    private final Map<String, JFrame> openForms = new HashMap<>();
    private final Data data;
    private final JPanel bookListPanel = new JPanel();
    private final JLabel status = new JLabel(" ");
    private final JButton noticesButton = new JButton("Notices");

    public MainFrame(Data data) {
        super(data.getUsername());
        this.data = data;
        setSize(960, 680);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(button("Refresh", this::loadBooks));
        topPanel.add(button("Add book", this::addBook));
        topPanel.add(button("Members", () -> open("members", UserFrame::new)));
        topPanel.add(button("Reservations", () -> open("reservations", ReservationsFrame::new)));
        topPanel.add(button("Order from supplier", () -> open("suppliers", SuppliersFrame::new)));
        topPanel.add(button("Proposals", () -> open("proposals", () -> new ProposalsFrame(data))));
        topPanel.add(button("Group chat", () -> open("chat", () -> new GroupChatForm(data))));
        noticesButton.addActionListener(e -> open("notices", () -> new NoticesFrame(data.getDeliveries())));
        topPanel.add(noticesButton);
        add(topPanel, BorderLayout.NORTH);

        bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(bookListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);

        // Books approved by a supplier appear without pressing Refresh
        data.getDeliveries().onCatalogueChange(() -> SwingUtilities.invokeLater(this::loadBooks));
        data.getDeliveries().onNotice(() -> SwingUtilities.invokeLater(() -> {
            noticesButton.setText("Notices (" + data.getDeliveries().getNotices().size() + ")");
            status.setText("New notice: " + data.getDeliveries().getNotices().get(0).lines().findFirst().orElse(""));
        }));
        loadBooks();
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void loadBooks() {
        status.setText("Loading the catalogue...");
        Tasks.load(this, "The catalogue could not be loaded.", () -> BookService.getInstance().getAll(), this::show);
    }

    private void show(List<BookDto> books) {
        bookListPanel.removeAll();
        for (BookDto book : books) {
            bookListPanel.add(BookViews.bookRow(book,
                    button("Details", () -> BookViews.showDetails(this, book)),
                    button("Edit", () -> editBook(book)),
                    button("Delete", () -> deleteBook(book))));
        }
        bookListPanel.revalidate();
        bookListPanel.repaint();
        status.setText(BookViews.books(books.size()) + " in the catalogue");
    }

    private void addBook() {
        Book book = new BookForm(this, "Add a book", null, true, true).showDialog();
        if (book != null) {
            Tasks.run(this, "The book was not added.", () -> BookService.getInstance().add(book), this::loadBooks);
        }
    }

    private void editBook(BookDto summary) {
        Tasks.load(this, "The book could not be loaded.", () -> BookService.getInstance().getBook(summary.getKey()),
                stored -> {
                    Book changes = new BookForm(this, "Edit book", stored, false, true).showDialog();
                    if (changes != null) {
                        Tasks.run(this, "The book was not saved.",
                                () -> BookService.getInstance().update(summary.getKey(), changes), this::loadBooks);
                    }
                });
    }

    private void deleteBook(BookDto book) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + book.getTitle() + "\" from the catalogue?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Tasks.run(this, "The book was not deleted.", () -> BookService.getInstance().delete(book), this::loadBooks);
        }
    }

    private void open(String key, Supplier<JFrame> creator) {
        JFrame existing = openForms.get(key);
        if (existing == null || !existing.isDisplayable()) {
            existing = creator.get();
            existing.setLocationRelativeTo(this);
            openForms.put(key, existing);
        }
        existing.setVisible(true);
        existing.toFront();
    }
}
