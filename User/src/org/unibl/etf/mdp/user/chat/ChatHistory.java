package org.unibl.etf.mdp.user.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unibl.etf.mdp.model.ChatMessage;

public class ChatHistory {
	private Map<String, List<ChatMessage>> conversations;
	private static ChatHistory instance;

	private ChatHistory() {
		conversations = new HashMap<>();
	}

	public static synchronized ChatHistory getInstance() {
		if (instance == null)
			instance = new ChatHistory();
		return instance;
	}

	public void addMessage(String username, ChatMessage message) {
		conversations.computeIfAbsent(username, k -> new ArrayList<>()).add(message);
	}

	public List<ChatMessage> getMessages(String username) {
		return conversations.getOrDefault(username, new ArrayList<>());
	}

	public List<String> getActiveConversations() {
		return new ArrayList<>(conversations.keySet());
	}
}
