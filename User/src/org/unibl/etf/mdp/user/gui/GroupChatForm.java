package org.unibl.etf.mdp.user.gui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.user.groupchat.GroupChatClient;
import org.unibl.etf.mdp.user.groupchat.GroupChatHistory;
import org.unibl.etf.mdp.user.groupchat.GroupChatListener;

public class GroupChatForm extends JFrame implements GroupChatListener {
	private static final long serialVersionUID = 1L;
	private JTextArea chatArea;
	private JTextField messageField;
	private JButton sendButton;
	private GroupChatHistory chatHistory;
	private GroupChatClient chatClient;
	private String username;

	public GroupChatForm(String username) {
		this.chatHistory = GroupChatHistory.getInstance();
		this.chatClient = GroupChatClient.getInstance();
		this.username = username;

		setTitle("Group Chat");
		setSize(600, 400);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Chat panel
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BorderLayout());

		chatArea = new JTextArea();
		chatArea.setEditable(false);
		JScrollPane chatScrollPane = new JScrollPane(chatArea);
		chatPanel.add(chatScrollPane, BorderLayout.CENTER);

		// Input panel
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());

		messageField = new JTextField();
		sendButton = new JButton("Send");
		sendButton.addActionListener(e -> sendMessage());

		inputPanel.add(messageField, BorderLayout.CENTER);
		inputPanel.add(sendButton, BorderLayout.EAST);
		chatPanel.add(inputPanel, BorderLayout.SOUTH);

		add(chatPanel, BorderLayout.CENTER);

		// Add listener for group chat
		chatHistory.addListener(this);

		// Load existing messages
		loadChatHistory();
	}

	private void sendMessage() {
		String message = messageField.getText().trim();
		if (message.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Message cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		chatClient.sendMulticastMessage(username, message);
		loadChatHistory();
		messageField.setText("");
	}

	private void loadChatHistory() {
		chatArea.setText("");
		List<ChatMessage> messages = chatHistory.getMessages();
		for (ChatMessage message : messages) {
			chatArea.append(message.getUsername() + ": " + message.getMessage() + "\n");
		}
	}

	@Override
	public void onNewMessage() {
		loadChatHistory();
	}
}
