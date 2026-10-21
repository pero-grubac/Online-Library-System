package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.library.services.SupplierService;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.OrderItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A supplier's offer; the librarian puts books with a quantity in the cart and orders them. */
public class SuppliersBookFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Data data;
    private final String supplier;
    // Keyed by book key, in the order the books were added
    private final Map<String, OrderItem> cart = new LinkedHashMap<>();
    private final JPanel bookListPanel = new JPanel();
    private final JButton cartButton = new JButton("Cart (0)");

    public SuppliersBookFrame(Data data, String supplier, int port) {
        super("Books of " + supplier);
        this.data = data;
        this.supplier = supplier;
        setSize(900, 680);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(bookListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cartButton.addActionListener(e -> showCart());
        JButton orderButton = new JButton("Order");
        orderButton.addActionListener(e -> order());
        topPanel.add(cartButton);
        topPanel.add(orderButton);
        add(topPanel, BorderLayout.NORTH);

        bookListPanel.add(new JLabel("  Loading the offer..."));
        Tasks.load(this, "The offer of " + supplier + " could not be loaded.",
                () -> SupplierService.getOfferedBooks(data.getUsername(), port), this::show);
    }

    private void show(List<BookDto> books) {
        bookListPanel.removeAll();
        if (books.isEmpty()) {
            bookListPanel.add(new JLabel("  " + supplier + " offers no books."));
        }
        for (BookDto book : books) {
            JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            quantity.setPreferredSize(new Dimension(60, quantity.getPreferredSize().height));
            JButton details = new JButton("Details");
            details.addActionListener(e -> BookViews.showDetails(this, book));
            JButton add = new JButton("Add to cart");
            add.addActionListener(e -> addToCart(book, (Integer) quantity.getValue()));
            bookListPanel.add(BookViews.bookRow(book, details, new JLabel("Quantity:"), quantity, add));
        }
        bookListPanel.revalidate();
        bookListPanel.repaint();
    }

    private void addToCart(BookDto book, int quantity) {
        OrderItem existing = cart.get(book.getKey());
        int total = existing == null ? quantity : existing.getQuantity() + quantity;
        cart.put(book.getKey(), new OrderItem(book, total));
        updateCartButton();
    }

    private void updateCartButton() {
        int copies = cart.values().stream().mapToInt(OrderItem::getQuantity).sum();
        cartButton.setText("Cart (" + copies + ")");
    }

    /** Shows the cart; quantities can be changed and rows removed. */
    private void showCart() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Book", "Author", "Quantity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 2 ? Integer.class : String.class;
            }
        };
        List<OrderItem> items = new ArrayList<>(cart.values());
        for (OrderItem item : items) {
            model.addRow(new Object[]{item.getBook().getTitle(), item.getBook().getAuthor(), item.getQuantity()});
        }
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(520, 220));
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(scroll, BorderLayout.CENTER);
        JButton remove = new JButton("Remove selected");
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
                items.remove(row);
            }
        });
        panel.add(remove, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(this, panel, "Cart", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        cart.clear();
        for (int i = 0; i < items.size(); i++) {
            Object value = model.getValueAt(i, 2);
            int quantity = value instanceof Integer ? (Integer) value : 0;
            if (quantity > 0) {
                cart.put(items.get(i).getBook().getKey(), new OrderItem(items.get(i).getBook(), quantity));
            }
        }
        updateCartButton();
    }

    private void order() {
        if (cart.isEmpty()) {
            BookViews.error(this, "The cart is empty.");
            return;
        }
        List<OrderItem> items = new ArrayList<>(cart.values());
        Tasks.run(this, "The order was not sent; the cart is kept.", () -> data.sendOrder(supplier, items), () -> {
            cart.clear();
            updateCartButton();
            BookViews.info(this, "Order sent to " + supplier + ". Approved books will appear in the catalogue.");
        });
    }
}
