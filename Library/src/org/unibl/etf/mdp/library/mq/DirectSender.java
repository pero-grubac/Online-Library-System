package org.unibl.etf.mdp.library.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.library.properties.AppConfig;
import org.unibl.etf.mdp.model.Message;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Sends orders to a supplier's durable queue as persistent JSON messages,
 * so an order survives a supplier that is offline and a broker restart.
 */
public class DirectSender {
    private static final AppConfig conf = new AppConfig();
    private static final String EXCHANGE_NAME = conf.getExchangeName();
    private static final String DIRECT = conf.getDirect();
    private static final String QUEUE_PREFIX = conf.getQueuePrefix();
    private static DirectSender instance;
    private Channel channel;
    private Connection connection;

    private DirectSender() throws IOException, TimeoutException {
        this(ConnectionFactoryUtil.createConnection());
    }

    public DirectSender(Connection connection) throws IOException {
        this.connection = connection;
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, DIRECT, true);
    }

    public static synchronized DirectSender getInstance() throws Exception {
        if (instance == null) {
            instance = new DirectSender();
        }
        return instance;
    }

    public void sendMessage(String routingKey, Message message) throws IOException {
        // Declared here too, so the order is kept even if the supplier has never been started
        String queueName = QUEUE_PREFIX + routingKey;
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);

        channel.basicPublish(EXCHANGE_NAME, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, Json.toBytes(message));
    }

    public void shutdown() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        if (instance != null) {
            instance = null;
        }
    }
}
