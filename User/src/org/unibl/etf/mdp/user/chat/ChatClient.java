package org.unibl.etf.mdp.user.chat;

import org.unibl.etf.mdp.user.properties.AppConfig;

public class ChatClient {
	private static final AppConfig conf = new AppConfig();
	private static final String MULTICAST_ADD = conf.getMulticastAdd();
	private static final int USERS_PORT = conf.getChatUsersPort();
}
