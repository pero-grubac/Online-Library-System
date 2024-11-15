package org.unibl.etf.mdp.supplier.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.unibl.etf.mdp.supplier.properties.AppConfig;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ConnectionFactoryUtil {
	private static final AppConfig conf = new AppConfig();
	private static final String HOST = conf.getDefaultHost();
	private static final String USER = conf.getRabbitMQUser();
	private static final String PASS = conf.getRabbitMQPass();

	public static Connection createConnection() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(HOST);
		factory.setUsername(USER);
		factory.setPassword(PASS);
		return factory.newConnection();
	}
}
