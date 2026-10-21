package org.unibl.etf.mdp.user.chat;

import org.unibl.etf.mdp.model.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Private conversations by member; written by the network threads, read by the GUI. */
public class ChatHistory {
    private static ChatHistory instance;
    private final Map<String, List<ChatMessage>> conversations = new HashMap<>();
    private final List<ChatListener> listeners = new CopyOnWriteArrayList<>();

    private ChatHistory() {
    }

    public static synchronized ChatHistory getInstance() {
        if (instance == null)
            instance = new ChatHistory();
        return instance;
    }

    public void addMessage(String username, ChatMessage message) {
        synchronized (conversations) {
            conversations.computeIfAbsent(username, k -> new ArrayList<>()).add(message);
        }
        for (ChatListener listener : listeners) {
            listener.onNewMessage(username);
        }
    }

    public List<ChatMessage> getMessages(String username) {
        synchronized (conversations) {
            return new ArrayList<>(conversations.getOrDefault(username, List.of()));
        }
    }

    public List<String> getActiveConversations() {
        synchronized (conversations) {
            return new ArrayList<>(conversations.keySet());
        }
    }

    public void addListener(ChatListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ChatListener listener) {
        listeners.remove(listener);
    }
}
