package org.unibl.etf.mdp.library.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;

import org.unibl.etf.mdp.library.services.BookService;
import org.unibl.etf.mdp.model.BookDto;

public class MainFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private JPanel bookListPanel;

	public MainFrame() {
		super("Library");
		setSize(800, 600);
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> loadBooks());
		topPanel.add(refreshButton);

		JButton chooseSupplierButton = new JButton("Choose Supplier");
		chooseSupplierButton.addActionListener(e -> new SuppliersFrame().setVisible(true));
		topPanel.add(chooseSupplierButton);

		JButton manageUsersButton = new JButton("Manage Users");
		manageUsersButton.addActionListener(e -> new UserFrame().setVisible(true));
		topPanel.add(manageUsersButton);

		add(topPanel, BorderLayout.NORTH);

		bookListPanel = new JPanel();
		bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(bookListPanel);
		scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(scrollPane, BorderLayout.CENTER);

		loadBooks();
	}

	private void loadBooks() {
		List<BookDto> books = BookService.getInstance().getAll();
		bookListPanel.removeAll();

		for (BookDto book : books) {
			bookListPanel.add(createBookPanel(book));
		}

		bookListPanel.revalidate();
		bookListPanel.repaint();
	}

	private JPanel createBookPanel(BookDto book) {
		JPanel bookPanel = new JPanel();
		bookPanel.setLayout(new BorderLayout());
		bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bookPanel.setMaximumSize(new Dimension(700, 150));

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

		// Informacije o knjizi
		JPanel attributesPanel = new JPanel();
		attributesPanel.setLayout(new BoxLayout(attributesPanel, BoxLayout.Y_AXIS));
		attributesPanel.add(new JLabel("Author: " + book.getAuthor()));
		attributesPanel.add(new JLabel("Title: " + book.getTitle()));
		attributesPanel.add(new JLabel("Language: " + book.getLanguage()));
		attributesPanel.add(new JLabel("Release Date: " + book.getFormatedDate()));

		// Dugme za pregled
		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(e -> showPreviewDialog(book.getPreview()));

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> deleteBook(book));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(previewButton);
		buttonPanel.add(deleteButton);

		bookPanel.add(coverLabel, BorderLayout.WEST);
		bookPanel.add(attributesPanel, BorderLayout.CENTER);
		bookPanel.add(buttonPanel, BorderLayout.EAST);

		return bookPanel;
	}

	private void showPreviewDialog(String preview) {
		JTextArea previewTextArea = new JTextArea(preview);
		previewTextArea.setLineWrap(true);
		previewTextArea.setWrapStyleWord(true);
		previewTextArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(previewTextArea);
		scrollPane.setPreferredSize(new Dimension(600, 400));

		JOptionPane.showMessageDialog(this, scrollPane, "Book Preview", JOptionPane.INFORMATION_MESSAGE);
	}

	private void deleteBook(BookDto book) {
		int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to delete the book \"" + book.getTitle() + "\"?", "Confirm Delete",
				JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			boolean deleted = BookService.getInstance().delete(book);
			if (deleted) {
				JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				loadBooks();
			} else {
				JOptionPane.showMessageDialog(this, "Failed to delete the book.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
