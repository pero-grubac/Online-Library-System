package org.unibl.etf.mdp.user.groupchat;

import java.util.ArrayList;
import java.util.List;

import org.unibl.etf.mdp.model.ChatMessage;

public class GroupChatHistory {
	private List<ChatMessage> messages;
	private List<GroupChatListener> listeners;
	private static GroupChatHistory instance;

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
		messages.add(message);
		notifyListeners();
	}

	public List<ChatMessage> getMessages() {
		return messages;
	}
	
}
