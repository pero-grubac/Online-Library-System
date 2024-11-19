package org.unibl.etf.mdp.supplier.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.unibl.etf.mdp.model.Book;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Invoice;

public class RequestsFrame extends GeneralFrame {

	private static final long serialVersionUID = 1L;
	private final Data data;

	public RequestsFrame(String title) {
		super(title);
		setTitle("Requests");
		setSize(800, 600);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		data = Data.getInstance(null, null);

		// Panel koji sadrži listu zahteva
		JPanel requestsPanel = new JPanel();
		requestsPanel.setLayout(new BoxLayout(requestsPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(requestsPanel);
		scrollPane.setPreferredSize(new Dimension(780, 550)); // Veličina scroll panela
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(scrollPane, BorderLayout.CENTER);

		refreshRequests(requestsPanel);
	}

	private void refreshRequests(JPanel requestsPanel) {
		requestsPanel.removeAll(); // Očisti postojeći sadržaj panela
		List<List<BookDto>> requests = data.getRequests();

		for (List<BookDto> request : requests) {
			JPanel requestPanel = createRequestPanel(request);
			requestsPanel.add(requestPanel);
		}

		// Revalidiraj i osveži prikaz panela
		requestsPanel.revalidate();
		requestsPanel.repaint();
	}

	private JPanel createRequestPanel(List<BookDto> request) {
		JPanel requestPanel = new JPanel();
		requestPanel.setLayout(new BorderLayout());
		requestPanel.setBorder(BorderFactory.createTitledBorder("Request"));

		// Prikaz knjiga u zahtevu
		StringBuilder booksString = new StringBuilder();
		for (BookDto book : request) {
			booksString.append(book.toString()).append("\n");
		}
		JLabel booksLabel = new JLabel("<html>" + booksString.toString().replace("\n", "<br>") + "</html>");

		// Dugmad za Approve i Reject
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 10, 10));

		JButton approveButton = new JButton("Approve");
		approveButton.addActionListener(e -> approveRequest(request));

		JButton rejectButton = new JButton("Reject");
		rejectButton.addActionListener(e -> rejectRequest(request));

		buttonPanel.add(approveButton);
		buttonPanel.add(rejectButton);

		requestPanel.add(booksLabel, BorderLayout.CENTER);
		requestPanel.add(buttonPanel, BorderLayout.SOUTH);

		return requestPanel;
	}

	private void approveRequest(List<BookDto> request) {
		try {
			List<Book> listBooks = Data.getServerservice().getBooks(data.getUsername(), request);
			Invoice invoice = Data.getLibraryservice().approveBook(listBooks, data.getUsername());
			JOptionPane.showMessageDialog(this, "Request approved!\n" + invoice.toString(), "Success",
					JOptionPane.INFORMATION_MESSAGE);
			data.removeRequest(request);
			refreshRequests((JPanel) ((JScrollPane) getContentPane().getComponent(0)).getViewport().getView());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error approving request: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void rejectRequest(List<BookDto> request) {
		data.removeRequest(request);
		JOptionPane.showMessageDialog(this, "Request rejected!", "Info", JOptionPane.INFORMATION_MESSAGE);
		refreshRequests((JPanel) ((JScrollPane) getContentPane().getComponent(0)).getViewport().getView());
	}
}
