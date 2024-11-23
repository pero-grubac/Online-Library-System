package org.unibl.etf.mdp.library.groupchat;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;

import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;

public class GroupChatClient {
	private static final AppConfig conf = new AppConfig();
	private static final String GROUP_MSG = conf.getChatMsg();
	private static final String MULTICAST_ADD = conf.getMulticastAdd();
	private static final int PORT = conf.getChatUsersPort();
	private static GroupChatHistory history;
	private static GroupChatClient instance;

	private GroupChatClient() {
		history = GroupChatHistory.getInstance();
	}

	public static synchronized GroupChatClient getInstance() {
		if (instance == null)
			instance = new GroupChatClient();
		return instance;
	}

	public void sendMulticastMessage(String username, String text) {
		try (DatagramSocket socket = new DatagramSocket()) {
			InetAddress group = InetAddress.getByName(MULTICAST_ADD);

			Message message = new Message(GROUP_MSG, username, text);

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(message);
			objectStream.flush();
			byte[] messageBytes = byteStream.toByteArray();

			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, group, PORT);
			socket.send(packet);

			ChatMessage cm = new ChatMessage(username, text, LocalDateTime.now());
			history.addMessage(cm);
			System.out.println("Message sent to group: " + text);

		} catch (Exception e) {
			System.err.println("Error while sending multicast message: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
