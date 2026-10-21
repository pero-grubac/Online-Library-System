package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.user.chat.ChatHistory;
import org.unibl.etf.mdp.user.chat.ChatListener;
import org.unibl.etf.mdp.user.service.ChatService;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Private chat over TLS with the members who are online; new messages are marked until read. */
public class ChatForm extends JFrame implements ChatListener {
    private static final long serialVersionUID = 1L;
    private static final String UNREAD = " ●";
    private final ChatService chatService;
    private final String username;
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);
    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();
    private final JLabel status = new JLabel(" ");
    private final ChatHistory chatHistory = ChatHistory.getInstance();
    private final Set<String> unread = new HashSet<>();
    private Map<String, String> online = new HashMap<>();
    private boolean rebuilding;

    public ChatForm(String username) {
        super("Chat - " + username);
        this.username = username;
        this.chatService = ChatService.getInstance(username);
        setSize(640, 420);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel left = new JPanel(new BorderLayout());
        left.add(new JScrollPane(userList), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadUsers());
        left.add(refresh, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(170, 0));
        add(left, BorderLayout.WEST);

        chatArea.setEditable(false);
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(sendButton);

        userList.addListSelectionListener(e -> {
            String selected = selectedUser();
            // Rebuilding the list selects again; that must not start another rebuild
            if (!e.getValueIsAdjusting() && selected != null && !rebuilding) {
                if (unread.remove(selected)) {
                    showUsers();
                }
                loadChatHistory(selected);
            }
        });
        chatHistory.addListener(this);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                chatHistory.removeListener(ChatForm.this);
            }
        });
        loadUsers();
    }

    private String selectedUser() {
        String value = userList.getSelectedValue();
        return value == null ? null : value.replace(UNREAD, "");
    }

    private void loadUsers() {
        status.setText(chatService.getProblem() == null ? " " : chatService.getProblem());
        Tasks.load(this, "The members online could not be loaded.", chatService::getUsers, users -> {
            online = users;
            showUsers();
        });
    }

    /** Members online plus anyone who wrote to us, the unread ones marked. */
    private void showUsers() {
        rebuilding = true;
        try {
            rebuildUserList();
        } finally {
            rebuilding = false;
        }
    }

    private void rebuildUserList() {
        String selected = selectedUser();
        Set<String> names = new TreeSet<>(online.keySet());
        names.addAll(chatHistory.getActiveConversations());
        names.remove(username);
        userListModel.clear();
        for (String name : names) {
            userListModel.addElement(unread.contains(name) ? name + UNREAD : name);
        }
        if (selected != null) {
            for (int i = 0; i < userListModel.size(); i++) {
                if (userListModel.get(i).replace(UNREAD, "").equals(selected)) {
                    userList.setSelectedIndex(i);
                }
            }
        }
    }

    private void loadChatHistory(String selectedUser) {
        StringBuilder text = new StringBuilder();
        for (ChatMessage message : chatHistory.getMessages(selectedUser)) {
            text.append(message.getUsername()).append(": ").append(message.getMessage()).append("\n");
        }
        chatArea.setText(text.toString());
    }

    private void sendMessage() {
        String recipient = selectedUser();
        if (recipient == null) {
            BookViews.error(this, "Please select a member to write to.");
            return;
        }
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            BookViews.error(this, "Message cannot be empty.");
            return;
        }
        Tasks.run(this, "The message was not delivered.", () -> {
            String port = chatService.getUsers().get(recipient);
            if (port == null) {
                throw new IllegalStateException(recipient + " is not online.");
            }
            chatService.sendMessage(recipient, Integer.parseInt(port), message);
        }, () -> {
            messageField.setText("");
            loadChatHistory(recipient);
        });
    }

    // Called from the network thread that received the message
    @Override
    public void onNewMessage(String sender) {
        SwingUtilities.invokeLater(() -> {
            if (sender.equals(selectedUser())) {
                loadChatHistory(sender);
            } else if (!sender.equals(username)) {
                unread.add(sender);
                showUsers();
            }
        });
    }
}
