package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.user.chat.ChatHistory;
import org.unibl.etf.mdp.user.service.ChatService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ChatForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private ChatService chatService;
    private String username;

    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private ChatHistory chatHistory;

    public ChatForm(String username) {
        this.username = username;
        this.chatService = ChatService.getInstance(username, 0); // Port se postavlja u registraciji
        this.chatHistory = ChatHistory.getInstance();

        setTitle("Chat");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Leva strana: Lista korisnika
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, getHeight()));
        add(userScrollPane, BorderLayout.WEST);

        // Desna strana: Chat i unos poruka
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.CENTER);

        // Učitavanje korisnika
        loadUsers();

        // Listener za promenu selekcije korisnika
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null) {
                    loadChatHistory(selectedUser);
                }
            }
        });
    }

    private void loadUsers() {
        Map<String, String> users = chatService.getUsers();
        userListModel.clear();
        for (String user : users.keySet()) {
            if (!user.equals(username)) {
                userListModel.addElement(user);
            }
        }
    }

    private void loadChatHistory(String selectedUser) {
        chatArea.setText(""); // Očisti trenutni prikaz
        List<ChatMessage> messages = chatHistory.getMessages(selectedUser);
        for (ChatMessage message : messages) {
            chatArea.append(message.getUsername() + ": " + message.getMessage() + "\n");
        }
    }

    private void sendMessage() {
        String recipient = userList.getSelectedValue();
        if (recipient == null) {
            JOptionPane.showMessageDialog(this, "Please select a user to send a message.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Message cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, String> users = chatService.getUsers();
        String recipientPort = users.get(recipient);

        if (recipientPort == null) {
            JOptionPane.showMessageDialog(this, "User not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int port = Integer.parseInt(recipientPort);
        chatService.sendMessage(recipient, port, message);


        loadChatHistory(recipient);

        messageField.setText("");
    }
}
