package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.user.chat.ChatServer;
import org.unibl.etf.mdp.user.service.BookService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

public class MainFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private static final BookService service = BookService.getInstance();

	private JTable bookTable;
	private BookTableModel bookTableModel;
	private Data data;
	private static final Map<String, JFrame> openForms = new HashMap<>();
	private JTextField searchField;

	public MainFrame(String username) {
		super("Library - " + username);
		setSize(800, 600);
		data = Data.getInstance(username);
		JPanel mainPanel = new JPanel(new BorderLayout());

		// Top buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton sendBooksButton = new JButton("Send Books");
		sendBooksButton.addActionListener(e -> sendSelectedBooks());

		JButton btn2 = new JButton("Group chat");
		JButton btn3 = new JButton("Chat");
		btn3.addActionListener(e -> openForm("ChatForm", () -> new ChatForm(data.getUsername())));
		btn2.addActionListener(e -> openForm("GroupChatForm", () -> new GroupChatForm(data.getUsername())));

		buttonPanel.add(sendBooksButton);
		buttonPanel.add(btn3);
		buttonPanel.add(btn2);

		mainPanel.add(buttonPanel, BorderLayout.NORTH);

		// Search field
		searchField = new JTextField(30);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(e -> searchBooks());
		buttonPanel.add(new JLabel("Search:"));
		buttonPanel.add(searchField);
		buttonPanel.add(searchButton);

		mainPanel.add(buttonPanel, BorderLayout.NORTH);

		// Book table
		bookTableModel = new BookTableModel(BookService.getInstance().getAll());
		bookTable = new JTable(bookTableModel);

		bookTable.getColumn("Details").setCellRenderer(new ButtonRenderer());
		bookTable.getColumn("Details").setCellEditor(new ButtonEditor(new JCheckBox(), bookTableModel.getBooks()));

		JScrollPane scrollPane = new JScrollPane(bookTable);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		add(mainPanel);
	}

	private void openForm(String formKey, FormCreator creator) {
		JFrame existingForm = openForms.get(formKey);
		if (existingForm == null || !existingForm.isVisible()) {
			JFrame newForm = creator.create();
			openForms.put(formKey, newForm);
			newForm.setVisible(true);
		} else {
			existingForm.toFront();
		}
	}

	private void searchBooks() {
		String query = searchField.getText().trim().toLowerCase();

		// Ako je string za pretragu prazan, prikazujemo sve knjige
		if (query.isEmpty()) {
			bookTableModel.updateBooks(BookService.getInstance().getAll());
		} else {
			// Filtriramo knjige po naslovu
			List<BookDto> filteredBooks = BookService.getInstance().getAll().stream()
					.filter(book -> book.getTitle().toLowerCase().contains(query)).collect(Collectors.toList());
			bookTableModel.updateBooks(filteredBooks);
		}
	}

	private void sendSelectedBooks() {
		List<BookDto> selectedBooks = bookTableModel.getSelectedBooks();
		if (selectedBooks.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select at least one book.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		BookService.getInstance().sendBooksToEmail(selectedBooks, data.getUsername());
		JOptionPane.showMessageDialog(this, "Books sent to " + data.getUsername() + ".", "Success",
				JOptionPane.INFORMATION_MESSAGE);
	}

	@FunctionalInterface
	interface FormCreator {
		JFrame create();
	}

	// Table model for books
	private static class BookTableModel extends AbstractTableModel {

		private  List<BookDto> books;
		private final String[] columnNames = { "Select", "Title", "Author", "Language", "Release Date", "Price",
				"Details" };
		private final Class<?>[] columnClasses = { Boolean.class, String.class, String.class, String.class,
				String.class, Double.class, String.class };
		private  Boolean[] selected;

		public BookTableModel(List<BookDto> books) {
			 this.books = new ArrayList<>(books);
			this.selected = new Boolean[books.size()];
			for (int i = 0; i < selected.length; i++) {
				selected[i] = false;
			}
		}

		@Override
		public int getRowCount() {
			return books.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
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
			case 6:
				return "Details";
			default:
				return null;
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

		public List<BookDto> getBooks() {
			return books;
		}

		public List<BookDto> getSelectedBooks() {
			return books.stream().filter(book -> selected[books.indexOf(book)])
					.collect(Collectors.toCollection(ArrayList::new));
		}

		public void updateBooks(List<BookDto> newBooks) {
		    books = new ArrayList<>(newBooks); // Kreiramo novu listu sa novim knjigama

		    // Resetujemo izbor za sve knjige
		    selected = new Boolean[newBooks.size()];
		    for (int i = 0; i < selected.length; i++) {
		        selected[i] = false;
		    }

		    // Obaveštavamo JTable da su podaci promenjeni
		    fireTableDataChanged();
		}



	}

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

	private class ButtonEditor extends DefaultCellEditor {
		private JButton button;
		private List<BookDto> books;
		private int currentRow;

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
			button.setText(value == null ? "Details" : value.toString());
			currentRow = row;
			return button;
		}

		@Override
		public Object getCellEditorValue() {
			showBookDetails(books.get(currentRow));
			return "Details";
		}

		private void showBookDetails(BookDto book) {
			JPanel detailsPanel = new JPanel(new BorderLayout(20, 20)); // Dodan veći razmak između elemenata
			detailsPanel.setPreferredSize(new Dimension(500, 300)); // Povećana dimenzija panela

			// Slika knjige
			JLabel coverLabel = new JLabel();
			if (book.getCoverImageBytes() != null) {
				try (ByteArrayInputStream bais = new ByteArrayInputStream(book.getCoverImageBytes())) {
					BufferedImage bi = ImageIO.read(bais);
					coverLabel.setIcon(new ImageIcon(bi.getScaledInstance(150, 200, Image.SCALE_SMOOTH))); // Veća slika
				} catch (Exception e) {
					coverLabel.setText("No Image");
				}
			} else {
				coverLabel.setText("No Image");
			}

			// Book preview
			JTextArea previewArea = new JTextArea(book.getPreview());
			previewArea.setEditable(false);
			previewArea.setLineWrap(true);
			previewArea.setWrapStyleWord(true);

			JScrollPane previewScrollPane = new JScrollPane(previewArea);
			previewScrollPane.setPreferredSize(new Dimension(300, 200)); // Povećana dimenzija za tekstualni pregled

			detailsPanel.add(coverLabel, BorderLayout.WEST);
			detailsPanel.add(previewScrollPane, BorderLayout.CENTER);

			JOptionPane.showMessageDialog(MainFrame.this, detailsPanel, "Book Details",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}
}
