package org.unibl.etf.mdp.common;

import org.unibl.etf.mdp.model.ChatMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Group chat messages in arrival order, each one once; written by the network, read by the GUI. */
public class GroupChatHistory {
    private final Map<String, ChatMessage> messages = new LinkedHashMap<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /** @return false when the message was already there (e.g. our own message looped back) */
    public boolean addMessage(ChatMessage message) {
        synchronized (messages) {
            if (message == null || messages.containsKey(message.getId())) {
                return false;
            }
            messages.put(message.getId(), message);
        }
        listeners.forEach(Runnable::run);
        return true;
    }

    public List<ChatMessage> getMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages.values());
        }
    }
}
