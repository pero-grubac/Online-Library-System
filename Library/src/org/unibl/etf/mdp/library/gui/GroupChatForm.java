package org.unibl.etf.mdp.library.gui;

import org.unibl.etf.mdp.common.ui.BookViews;
import org.unibl.etf.mdp.common.ui.Tasks;
import org.unibl.etf.mdp.model.ChatMessage;

import javax.swing.*;
import java.awt.*;

/** The multicast group chat shared with all members. */
public class GroupChatForm extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Data data;
    private final JTextArea chatArea = new JTextArea();
    private final JTextField messageField = new JTextField();
    private final Runnable listener = () -> SwingUtilities.invokeLater(this::loadChatHistory);

    public GroupChatForm(Data data) {
        super("Group Chat");
        this.data = data;
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(sendButton);

        data.getChat().addListener(listener);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                data.getChat().removeListener(listener);
            }
        });
        loadChatHistory();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            BookViews.error(this, "Message cannot be empty.");
            return;
        }
        Tasks.run(this, "The message was not sent.", () -> data.sendGroupMessage(message), () -> messageField.setText(""));
    }

    private void loadChatHistory() {
        StringBuilder text = new StringBuilder();
        for (ChatMessage message : data.getChat().getMessages()) {
            text.append(message.getUsername()).append(": ").append(message.getMessage()).append("\n");
        }
        chatArea.setText(text.toString());
    }
}
