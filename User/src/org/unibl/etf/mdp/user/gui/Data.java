package org.unibl.etf.mdp.user.gui;

import org.unibl.etf.mdp.user.chat.ChatServer;
import org.unibl.etf.mdp.user.groupchat.GroupChatServer;

public class Data {
	private static Data instance;
	private ChatServer serverInstance;
	private Thread serverThread;
	private String username;
	private GroupChatServer groupChatServer;

	private Data(String name) {
		this.username = name;
		initServer();
		initGroupChatServer();
	}

	private void initGroupChatServer() {
		groupChatServer = new GroupChatServer();
		groupChatServer.start();
		
	}

	public static synchronized Data getInstance(String name) {
		if (instance == null)
			instance = new Data(name);
		return instance;
	}

	private void initServer() {
		serverInstance = ChatServer.getInstance(username);
		serverThread = new Thread(serverInstance);
		serverThread.start();
	}

	public String getUsername() {
		return username;
	}
}
