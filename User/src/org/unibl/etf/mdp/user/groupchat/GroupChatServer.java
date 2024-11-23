package org.unibl.etf.mdp.user.groupchat;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;

import org.unibl.etf.mdp.model.ChatMessage;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.user.properties.AppConfig;

public class GroupChatServer extends Thread {
	private static final AppConfig conf = new AppConfig();
	private static final String MULTICAST_ADD = conf.getMulticastAdd();
	private static final int PORT = conf.getChatUsersPort();
	private volatile boolean running = true;
	private static final GroupChatHistory history = GroupChatHistory.getInstance();

	@Override
	public void run() {
		try (MulticastSocket socket = new MulticastSocket(PORT)) {
			InetAddress group = InetAddress.getByName(MULTICAST_ADD);
			socket.joinGroup(group);

			System.out.println("Server is listening for multicast messages...");

			while (running) {
				try {
					// Priprema za prijem multicast paketa
					byte[] buffer = new byte[1024];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);

					// Deserializacija poruke
					ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
					ObjectInputStream objectStream = new ObjectInputStream(byteStream);
					Message message = (Message) objectStream.readObject();

					// Obrada poruke
					ChatMessage cm = new ChatMessage(message.getUsername(), (String) message.getBody(),
							LocalDateTime.now());
					history.addMessage(cm);
				} catch (Exception e) {
					System.err.println("Error while receiving or processing message: " + e.getMessage());
					e.printStackTrace();
				}
			}

			socket.leaveGroup(group);
			System.out.println("Server stopped listening for multicast messages.");
		} catch (Exception e) {
			System.err.println("Error in multicast server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void stopServer() {
		running = false;
	}
}
