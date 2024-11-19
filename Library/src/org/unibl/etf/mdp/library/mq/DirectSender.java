package org.unibl.etf.mdp.library.mq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeoutException;

import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Message;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class DirectSender {
	private static final AppConfig conf = new AppConfig();
	private static final String EXCHANGE_NAME = conf.getExchangeName();
	private static final String DIRECT = conf.getDirect();

	private static DirectSender instance;
	private Channel channel;
	private Connection connection;

	private DirectSender() throws IOException, TimeoutException {
		connection = ConnectionFactoryUtil.createConnection();
		channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE_NAME, DIRECT);
	}

	public static synchronized DirectSender getInstance() throws Exception {
		if (instance == null) {
			instance = new DirectSender();
		}
		return instance;
	}

	public void sendMessage(String routingKey, Message message) throws IOException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
		objOut.writeObject(message);
		objOut.flush();

		channel.basicPublish(EXCHANGE_NAME, routingKey, null, byteOut.toByteArray());
		System.out.println("Sent message: " + message + " to routing key: " + routingKey);

	}

	public void shutdown() throws IOException, TimeoutException {
		if (channel != null) {
			channel.close();
		}
		if (connection != null) {
			connection.close();
		}if(instance != null) {
			instance = null;
		}
		System.out.println("DirectSender shutdown complete.");
	}
}
