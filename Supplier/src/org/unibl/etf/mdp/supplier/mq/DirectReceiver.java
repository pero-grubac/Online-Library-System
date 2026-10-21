package org.unibl.etf.mdp.supplier.mq;

import com.rabbitmq.client.*;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.supplier.logger.FileLogger;
import org.unibl.etf.mdp.supplier.properties.AppConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receives the library's orders. Each supplier has its own durable queue, so orders sent while the supplier
 * is offline wait for it. An order is acknowledged only when the supplier has approved or rejected it;
 * until then RabbitMQ keeps it, and gives it back if the application is closed.
 */
public class DirectReceiver {
    private static final AppConfig conf = new AppConfig();
    private static final String EXCHANGE_NAME = conf.getExchangeName();
    private static final String DIRECT = conf.getDirect();
    private static final String QUEUE_PREFIX = conf.getQueuePrefix();
    private static final Logger logger = FileLogger.getLogger(DirectReceiver.class.getName());
    private final Channel channel;
    private final Connection connection;

    public DirectReceiver() throws IOException, TimeoutException {
        this(ConnectionFactoryUtil.createConnection());
    }

    public DirectReceiver(Connection connection) throws IOException {
        this.connection = connection;
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, DIRECT, true);
    }

    /** The same declaration is made by the library before it sends, whichever side starts first. */
    public static String declareQueue(Channel channel, String routingKey) throws IOException {
        String queueName = QUEUE_PREFIX + routingKey;
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, EXCHANGE_NAME, routingKey);
        return queueName;
    }

    /** Orders arrive oldest first; each comes with the tag to {@link #ack} once it is handled. */
    public void startListening(String routingKey, OrderHandler handler) throws IOException {
        String queueName = declareQueue(channel, routingKey);
        logger.info("Waiting for orders on " + queueName);

        channel.basicConsume(queueName, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                Message message;
                try {
                    message = Json.messageFromBytes(body, body.length);
                } catch (Exception e) {
                    // Unreadable: drop it instead of redelivering forever
                    logger.log(Level.WARNING, "Discarding malformed order", e);
                    channel.basicReject(envelope.getDeliveryTag(), false);
                    return;
                }
                handler.onOrder(message, envelope.getDeliveryTag());
            }
        });
    }

    /** Removes a handled order from the queue for good. */
    public void ack(long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    public void shutdown() throws IOException, TimeoutException {
        if (channel.isOpen()) {
            channel.close();
        }
        if (connection.isOpen()) {
            connection.close();
        }
    }

    @FunctionalInterface
    public interface OrderHandler {
        void onOrder(Message message, long deliveryTag);
    }
}
