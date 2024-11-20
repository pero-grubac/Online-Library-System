package org.unibl.etf.mdp.library.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.library.services.SupplierService;

public class SuppliersBookFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private List<BookDto> cart; // Korpa za knjige
	private final Dimension buttonSize = new Dimension(100, 30);
	private String username;

	public SuppliersBookFrame(String supplierName, int port) {
		super("Books of " + supplierName);
		this.username = supplierName;
		setSize(1000, 700);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		cart = new ArrayList<>(); // Inicijalizacija korpe

		JPanel bookListPanel = new JPanel();
		bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(bookListPanel);

		add(scrollPane, BorderLayout.CENTER);

		// Dugme za naručivanje
		JPanel topPanel = new JPanel();
		JButton orderButton = createButton("Order");
		orderButton.addActionListener(e -> orderBooks(supplierName, port));

		JButton cartButton = createButton("Cart");
		cartButton.addActionListener(e -> showCartDialog());

		topPanel.add(orderButton);
		topPanel.add(cartButton);

		add(topPanel, BorderLayout.NORTH);

		List<BookDto> books = SupplierService.getOfferedBooks(supplierName, port);

		for (BookDto book : books) {
			bookListPanel.add(createBookPanel(book));
		}
	}

	private JPanel createBookPanel(BookDto book) {
		JPanel bookPanel = new JPanel();
		bookPanel.setLayout(new BorderLayout());
		bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bookPanel.setMaximumSize(new Dimension(900, 180));

		JLabel coverLabel = new JLabel();
		ImageIcon coverImage = null;

		if (book.getCoverImageBytes() != null) {
			BufferedImage bi = null;
			try (ByteArrayInputStream bais = new ByteArrayInputStream(book.getCoverImageBytes())) {
				bi = ImageIO.read(bais);
			} catch (Exception e) {
				System.err.println("Error while converting bytes to cover image: " + e.getMessage());
			}
			if (bi != null) {
				coverImage = new ImageIcon(bi);
				Image scaledImage = coverImage.getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH);
				coverLabel.setIcon(new ImageIcon(scaledImage));
			}
		} else {
			coverLabel.setText("No Image");
		}

		JPanel attributesPanel = new JPanel();
		attributesPanel.setLayout(new BoxLayout(attributesPanel, BoxLayout.Y_AXIS));

		JLabel authorLabel = new JLabel("Author: " + book.getAuthor());
		JLabel titleLabel = new JLabel("Title: " + book.getTitle());
		JLabel languageLabel = new JLabel("Language: " + book.getLanguage());
		JLabel releaseDateLabel = new JLabel("Release Date: " + book.getFormatedDate());

		attributesPanel.add(authorLabel);
		attributesPanel.add(titleLabel);
		attributesPanel.add(languageLabel);
		attributesPanel.add(releaseDateLabel);

		// Dugmad za interakciju
		JPanel buttonPanel = new JPanel();
		JButton previewButton = createButton("Preview");
		previewButton.addActionListener(e -> showPreviewDialog(book.getPreview()));

		JButton addButton = createButton("Add");
		addButton.addActionListener(e -> {
			if (cart.contains(book)) {
				JOptionPane.showMessageDialog(this, "Book already in cart!", "Info", JOptionPane.INFORMATION_MESSAGE);
			} else {
				cart.add(book);
				JOptionPane.showMessageDialog(this, "Book added to cart!", "Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		buttonPanel.add(previewButton);
		buttonPanel.add(addButton);

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

	private void showCartDialog() {
		JTextArea cartTextArea = new JTextArea();
		cartTextArea.setLineWrap(true);
		cartTextArea.setWrapStyleWord(true);
		cartTextArea.setEditable(false);

		StringBuilder cartContent = new StringBuilder();
		for (int i = 0; i < cart.size(); i++) {
			cartContent.append(i + 1).append(". ").append(cart.get(i).displayPrice()).append("\n");
		}

		cartTextArea.setText(cartContent.toString());

		JScrollPane scrollPane = new JScrollPane(cartTextArea);
		scrollPane.setPreferredSize(new Dimension(600, 400));

		JButton removeButton = createButton("Remove");
		removeButton.addActionListener(e -> {
			String indexStr = JOptionPane.showInputDialog(this, "Enter the number of the book to remove:");
			try {
				int index = Integer.parseInt(indexStr) - 1;
				if (index >= 0 && index < cart.size()) {
					cart.remove(index);
					JOptionPane.showMessageDialog(this, "Book removed from cart!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					showCartDialog(); // Refresh cart dialog
				} else {
					JOptionPane.showMessageDialog(this, "Invalid number!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(scrollPane, BorderLayout.CENTER);
		dialogPanel.add(removeButton, BorderLayout.SOUTH);

		JOptionPane.showMessageDialog(this, dialogPanel, "Cart", JOptionPane.INFORMATION_MESSAGE);
	}

	private void orderBooks(String supplierName, int port) {
		if (cart.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Cart is empty!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String request = conf.getRequestMsg();
		Message msg = new Message(request, "library", cart);
		try {
			Data.initSender();
			Data.getSender().sendMessage(username, msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Data.shutdownMQ();
		cart.clear();
	}

	private JButton createButton(String text) {
		JButton button = new JButton(text);
		button.setPreferredSize(buttonSize);
		return button;
	}
}
