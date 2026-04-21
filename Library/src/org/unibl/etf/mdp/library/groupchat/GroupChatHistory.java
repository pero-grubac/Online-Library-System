package org.unibl.etf.mdp.library.groupchat;

import org.unibl.etf.mdp.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class GroupChatHistory {
    private static GroupChatHistory instance;
    private List<ChatMessage> messages;
    private List<GroupChatListener> listeners;

    private GroupChatHistory() {
        messages = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public static synchronized GroupChatHistory getInstance() {
        if (instance == null)
            instance = new GroupChatHistory();
        return instance;
    }

    public void addListener(GroupChatListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (GroupChatListener listener : listeners)
            listener.onNewMessage();
    }

    public void addMessage(ChatMessage message) {
        if (!messages.contains(message)) {
            messages.add(message);
            notifyListeners();
        }
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

}
