package org.unibl.etf.mdp.supplier.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.supplier.services.SupplierServerService;

public class MainFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;

	public MainFrame(String supplierName, List<String> bookLinks) {
		super(supplierName);

		setSize(1000, 700);
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		JButton requestsButton = new JButton("Requests");
		topPanel.add(requestsButton);

		add(topPanel, BorderLayout.NORTH); 

		JPanel bookListPanel = new JPanel();
		bookListPanel.setLayout(new BoxLayout(bookListPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(bookListPanel);

		add(scrollPane, BorderLayout.CENTER);

		Data data = Data.getInstance(new ArrayList<>(), supplierName);
		if (data.getBooks().isEmpty()) {
			data.setBooks(Data.getServerservice().getBookDtos(supplierName, bookLinks));
		}
		for (BookDto book : data.getBooks()) {
			bookListPanel.add(createBookPanel(book));
		}

		requestsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openRequestsFrame();
			}
		});
	}

	private JPanel createBookPanel(BookDto book) {
		JPanel bookPanel = new JPanel();
		bookPanel.setLayout(new BorderLayout());
		bookPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		bookPanel.setMaximumSize(new Dimension(900, 180));

		JLabel coverLabel = new JLabel();
		ImageIcon coverImage = null;

		if (book.getCoverImageFromBytes() != null) {
			coverImage = new ImageIcon(book.getCoverImageFromBytes());
			Image scaledImage = coverImage.getImage().getScaledInstance(120, 180, Image.SCALE_SMOOTH); // Veća veličina
			coverLabel.setIcon(new ImageIcon(scaledImage));
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

		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPreviewDialog(book.getPreview());
			}
		});

		bookPanel.add(coverLabel, BorderLayout.WEST);
		bookPanel.add(attributesPanel, BorderLayout.CENTER);
		bookPanel.add(previewButton, BorderLayout.EAST);

		return bookPanel;
	}

	private void showPreviewDialog(String preview) {
		// Napravite scrollable dijalog za preview
		JTextArea previewTextArea = new JTextArea(preview);
		previewTextArea.setLineWrap(true);
		previewTextArea.setWrapStyleWord(true);
		previewTextArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(previewTextArea);
		scrollPane.setPreferredSize(new Dimension(600, 400)); // Postavite dimenzije dijaloga

		JOptionPane.showMessageDialog(this, scrollPane, "Book Preview", JOptionPane.INFORMATION_MESSAGE);
	}

	private void openRequestsFrame() {
		RequestsFrame requestsFrame = new RequestsFrame("Requests");
		requestsFrame.setVisible(true);
	}
}
