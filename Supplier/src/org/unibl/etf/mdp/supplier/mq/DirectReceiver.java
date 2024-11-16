package org.unibl.etf.mdp.supplier.mq;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeoutException;

import org.unibl.etf.mdp.library.model.Message;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class DirectReceiver {
	private static final AppConfig conf = new AppConfig();
	private static final String EXCHANGE_NAME = conf.getExchangeName();
	private static final String DIRECT = conf.getDirect();

	private static DirectReceiver instance;
	private Channel channel;
	private Connection connection;
	
	private DirectReceiver() throws Exception {
        connection = ConnectionFactoryUtil.createConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
    }

    public static synchronized DirectReceiver getInstance() throws Exception {
        if (instance == null) {
            instance = new DirectReceiver();
        }
        return instance;
    }

    public void startListening(String routingKey, MessageHandler handler) throws IOException {
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);

        System.out.println("Waiting for messages with routing key: " + routingKey);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                try {
                    ByteArrayInputStream byteIn = new ByteArrayInputStream(body);
                    ObjectInputStream objIn = new ObjectInputStream(byteIn);
                    Message message = (Message) objIn.readObject();

                    handler.handleMessage(message); 
                } catch (ClassNotFoundException e) {
                    System.err.println("Failed to deserialize message: " + e.getMessage());
                }
            }
        };

        channel.basicConsume(queueName, true, consumer);
    }

    public void shutdown() throws IOException, TimeoutException {
        if (channel != null) {
            channel.close();
        }
        if (connection != null) {
            connection.close();
        }
        System.out.println("DirectReceiver shutdown complete.");
    }

    @FunctionalInterface
    public interface MessageHandler {
        void handleMessage(Message message);
    }
}
