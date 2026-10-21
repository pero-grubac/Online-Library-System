package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Reservation;
import org.unibl.etf.mdp.user.service.BookService;
import org.unibl.etf.mdp.user.service.ReservationService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** The member's view of the catalogue: search, details, reservation and books by e-mail. */
public class MainFrame extends GeneralFrame {
    private static final long serialVersionUID = 1L;
    private final Map<String, JFrame> openForms = new HashMap<>();
    private final BookTableModel bookTableModel = new BookTableModel();
    private final JTable bookTable = new JTable(bookTableModel);
    private final JTextField searchField = new JTextField(22);
    private final JLabel status = new JLabel(" ");
    private final Data data;
    private List<BookDto> catalogue = new ArrayList<>();

    public MainFrame(String username) {
        super("Library - " + username);
        setSize(980, 620);
        data = Data.getInstance(username);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(button("Send to my e-mail", this::sendSelectedBooks));
        buttonPanel.add(button("Reserve", this::reserveSelectedBooks));
        buttonPanel.add(button("My reservations", () -> open("reservations", MyReservationsFrame::new)));
        buttonPanel.add(button("Propose a book", () -> open("proposals", () -> new ProposalsFrame(data))));
        buttonPanel.add(button("Chat", () -> open("chat", () -> new ChatForm(data.getUsername()))));
        buttonPanel.add(button("Group chat", () -> open("group", () -> new GroupChatForm(data))));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search title, author or language:"));
        searchPanel.add(searchField);
        searchPanel.add(button("Search", this::applySearch));
        searchPanel.add(button("Refresh", this::loadBooks));
        searchField.addActionListener(e -> applySearch());

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(buttonPanel);
        top.add(searchPanel);

        bookTable.setRowHeight(26);
        bookTable.getColumnModel().getColumn(0).setMaxWidth(60);
        bookTable.getColumnModel().getColumn(6).setMaxWidth(90);
        DetailsButton details = new DetailsButton();
        bookTable.getColumnModel().getColumn(6).setCellRenderer(details);
        bookTable.getColumnModel().getColumn(6).setCellEditor(details);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(top, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        mainPanel.add(status, BorderLayout.SOUTH);
        add(mainPanel);
        loadBooks();
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
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

    private void loadBooks() {
        status.setText("Loading the catalogue...");
        Tasks.load(this, "The catalogue could not be loaded.", () -> BookService.getInstance().getAll(), books -> {
            catalogue = books;
            applySearch();
        });
    }

    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        List<BookDto> found = query.isEmpty() ? catalogue : catalogue.stream()
                .filter(b -> contains(b.getTitle(), query) || contains(b.getAuthor(), query)
                        || contains(b.getLanguage(), query))
                .collect(Collectors.toList());
        bookTableModel.updateBooks(found);
        status.setText(found.size() + " of " + BookViews.books(catalogue.size()));
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private List<BookDto> selectedOrWarn() {
        List<BookDto> selected = bookTableModel.getSelectedBooks();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tick at least one book.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        return selected;
    }

    private void sendSelectedBooks() {
        List<BookDto> selectedBooks = selectedOrWarn();
        if (!selectedBooks.isEmpty()) {
            String before = status.getText();
            status.setText("Sending " + BookViews.books(selectedBooks.size()) + "...");
            Tasks.load(this, "The books were not sent.", () -> {
                try {
                    return BookService.getInstance().sendBooksToEmail(selectedBooks, data.getUsername());
                } finally {
                    // Queued before done(), so a success message still replaces it
                    SwingUtilities.invokeLater(() -> status.setText(before));
                }
            }, message -> {
                        status.setText(message);
                        BookViews.info(this, message);
                    });
        }
    }

    private void reserveSelectedBooks() {
        List<BookDto> selectedBooks = selectedOrWarn();
        if (!selectedBooks.isEmpty()) {
            Tasks.load(this, "The reservation failed.", () -> {
                List<String> outcome = new ArrayList<>();
                for (BookDto book : selectedBooks) {
                    try {
                        Reservation r = ReservationService.getInstance().reserve(book);
                        outcome.add(r.getBookTitle() + ": reserved, waiting for the library");
                    } catch (Exception e) {
                        outcome.add(book.getTitle() + ": " + e.getMessage());
                    }
                }
                return outcome;
            }, outcome -> BookViews.info(this, String.join("\n", outcome)));
        }
    }

    /** Rows of the current (possibly searched) list; selection and details always use this same list. */
    private static class BookTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Select", "Title", "Author", "Language", "Release Date", "Price",
                "Details"};
        private final Class<?>[] columnClasses = {Boolean.class, String.class, String.class, String.class,
                String.class, Integer.class, String.class};
        private List<BookDto> books = new ArrayList<>();
        private boolean[] selected = new boolean[0];

        @Override
        public int getRowCount() {
            return books.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        BookDto bookAt(int row) {
            return books.get(row);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BookDto book = books.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return selected[rowIndex];
                case 1:
                    return book.getTitle();
                case 2:
                    return book.getAuthor();
                case 3:
                    return book.getLanguage();
                case 4:
                    return book.getFormatedDate();
                case 5:
                    return book.getPrice();
                default:
                    return "Details";
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                selected[rowIndex] = (Boolean) aValue;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 || columnIndex == 6;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnClasses[columnIndex];
        }

        List<BookDto> getSelectedBooks() {
            List<BookDto> result = new ArrayList<>();
            for (int i = 0; i < books.size(); i++) {
                if (selected[i]) {
                    result.add(books.get(i));
                }
            }
            return result;
        }

        void updateBooks(List<BookDto> newBooks) {
            books = new ArrayList<>(newBooks);
            selected = new boolean[books.size()];
            fireTableDataChanged();
        }
    }

    /** The "Details" button of each row; it opens the book of the row it is in, in the list shown now. */
    private class DetailsButton extends AbstractCellEditor implements TableCellRenderer, javax.swing.table.TableCellEditor {
        private final JButton renderer = new JButton("Details");
        private final JButton editor = new JButton("Details");
        private int row;

        DetailsButton() {
            editor.addActionListener(e -> {
                fireEditingStopped();
                BookViews.showDetails(MainFrame.this, bookTableModel.bookAt(bookTable.convertRowIndexToModel(row)));
            });
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return renderer;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                     int column) {
            this.row = row;
            return editor;
        }

        @Override
        public Object getCellEditorValue() {
            return "Details";
        }
    }
}
