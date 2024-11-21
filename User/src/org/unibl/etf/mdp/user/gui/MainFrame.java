package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.user.service.BookService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private static final BookService service = BookService.getInstance();
	private DefaultTableModel tableModel;
	private JTable bookTable;
	private JTextField searchField;

	public MainFrame() {
		super("Library");
		setSize(800, 600);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// Top buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton btn1 = new JButton("Btn1");
		JButton btn2 = new JButton("Btn2");
		JButton btn3 = new JButton("Btn3");
		buttonPanel.add(btn1);
		buttonPanel.add(btn2);
		buttonPanel.add(btn3);

		// Search field
		searchField = new JTextField(30);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(e -> searchBooks());
		buttonPanel.add(new JLabel("Search:"));
		buttonPanel.add(searchField);
		buttonPanel.add(searchButton);

		mainPanel.add(buttonPanel, BorderLayout.NORTH);

		// Book table
		tableModel = new DefaultTableModel(
				new Object[] { "Title", "Author", "Language", "Release Date", "Price", "Details" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 5; // Make "Details" button clickable
			}
		};
		bookTable = new JTable(tableModel);
		bookTable.getColumn("Details").setCellRenderer(new ButtonRenderer());
		bookTable.getColumn("Details").setCellEditor(new ButtonEditor(new JCheckBox(), service.getAll()));

		JScrollPane scrollPane = new JScrollPane(bookTable);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		add(mainPanel);
		loadBooks();
	}

	private void loadBooks() {
		List<BookDto> books = service.getAll(); // Fetch books from service
		updateTable(books);
	}

	private void searchBooks() {
		String query = searchField.getText().trim().toLowerCase();
		if (query.isEmpty()) {
			loadBooks();
		} else {
			List<BookDto> books = service.getAll();
			books = books.stream()
					.filter(book -> book.getTitle().toLowerCase().contains(query)
							|| book.getAuthor().toLowerCase().contains(query))
					.collect(Collectors.toCollection(ArrayList::new)); // Zamena za .toList()
			updateTable(books);
		}
	}

	private void updateTable(List<BookDto> books) {
		tableModel.setRowCount(0); // Clear existing data
		for (BookDto book : books) {
			tableModel.addRow(new Object[] { book.getTitle(), book.getAuthor(), book.getLanguage(),
					book.getFormatedDate(), book.getPrice(), "Details" // Prikazuje "Details" u poslednjoj koloni
			});
		}

		// Dodajemo listener za klik na dugme "Details"
		bookTable.getColumn("Details").setCellRenderer(new ButtonRenderer());
		bookTable.getColumn("Details").setCellEditor(new ButtonEditor(new JCheckBox(), books));
	}

	// Custom button renderer for JTable
	private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
		public ButtonRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setText(value == null ? "Details" : value.toString());
			return this;
		}
	}

	// Custom button editor for JTable
	private class ButtonEditor extends DefaultCellEditor {
		private JButton button;
		private String label;
		private boolean isPushed;
		private List<BookDto> books; // Referenca na knjige

		public ButtonEditor(JCheckBox checkBox, List<BookDto> books) {
			super(checkBox);
			this.books = books;
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(e -> fireEditingStopped());
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			label = (value == null) ? "Details" : value.toString();
			button.setText(label);
			isPushed = true;
			return button;
		}

		@Override
		public Object getCellEditorValue() {
			if (isPushed) {
				int row = bookTable.getSelectedRow();
				if (row != -1 && row < books.size()) {
					BookDto book = books.get(row);
					showBookDetails(book);
				}
			}
			isPushed = false;
			return label;
		}

		private void showBookDetails(BookDto book) {
			JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));

			// Slika knjige
			JLabel coverLabel = new JLabel();
			if (book.getCoverImageBytes() != null) {
				BufferedImage bi = null;
				try (ByteArrayInputStream bais = new ByteArrayInputStream(book.getCoverImageBytes())) {
					bi = ImageIO.read(bais);
				} catch (Exception e) {
					System.err.println("Error while converting bytes to cover image: " + e.getMessage());
				}
				if (bi != null) {
					ImageIcon coverImage = new ImageIcon(bi);
					Image scaledImage = coverImage.getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH);
					coverLabel.setIcon(new ImageIcon(scaledImage));
				}
			} else {
				coverLabel.setText("No Image");
			}

			// Book preview
			JTextArea previewArea = new JTextArea(book.getPreview());
			previewArea.setLineWrap(true);
			previewArea.setWrapStyleWord(true);
			previewArea.setEditable(false);

			JScrollPane previewScrollPane = new JScrollPane(previewArea);
			previewScrollPane.setPreferredSize(new Dimension(300, 200));

			detailsPanel.add(coverLabel, BorderLayout.WEST);
			detailsPanel.add(previewScrollPane, BorderLayout.CENTER);

			JOptionPane.showMessageDialog(MainFrame.this, detailsPanel, "Book Details",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
