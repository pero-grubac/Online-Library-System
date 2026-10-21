package org.unibl.etf.mdp.supplier.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;
import org.unibl.etf.mdp.model.OrderItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Orders through a real RabbitMQ in Docker; skipped when Docker is not available. */
@Testcontainers(disabledWithoutDocker = true)
class DirectReceiverRabbitTest {

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-alpine"));

    private Connection connection;
    private final String supplier = "BookSupplier_" + UUID.randomUUID().toString().substring(0, 8);

    /** One delivered order and its tag. */
    private static final class Delivery {
        final Message message;
        final long tag;

        Delivery(Message message, long tag) {
            this.message = message;
            this.tag = tag;
        }
    }

    private static Connection newConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT.getHost());
        factory.setPort(RABBIT.getAmqpPort());
        factory.setUsername(RABBIT.getAdminUsername());
        factory.setPassword(RABBIT.getAdminPassword());
        return factory.newConnection();
    }

    @BeforeEach
    void connect() throws Exception {
        connection = newConnection();
    }

    @AfterEach
    void disconnect() throws Exception {
        connection.close();
    }

    /** What the library does: declare the supplier's queue, then publish a persistent JSON order. */
    private void sendOrderAsTheLibrary(byte[] body) throws Exception {
        try (Channel channel = connection.createChannel()) {
            channel.exchangeDeclare("library.orders", "direct", true);
            DirectReceiver.declareQueue(channel, supplier);
            channel.basicPublish("library.orders", supplier, MessageProperties.PERSISTENT_TEXT_PLAIN, body);
        }
    }

    private static byte[] order(String... titles) {
        OrderItem[] items = new OrderItem[titles.length];
        for (int i = 0; i < titles.length; i++) {
            BookDto book = new BookDto();
            book.setTitle(titles[i]);
            items[i] = new OrderItem(book, i + 1);
        }
        return Json.toBytes(new Message("REQUEST", "Library", List.of(items)));
    }

    private BlockingQueue<Delivery> listen(DirectReceiver receiver) throws IOException {
        BlockingQueue<Delivery> orders = new LinkedBlockingQueue<>();
        receiver.startListening(supplier, (message, tag) -> orders.add(new Delivery(message, tag)));
        return orders;
    }

    private long waiting() throws Exception {
        try (Channel channel = connection.createChannel()) {
            return channel.queueDeclarePassive("orders." + supplier).getMessageCount();
        }
    }

    @Test
    void orderSentWhileTheSupplierWasOfflineArrivesWithQuantities() throws Exception {
        sendOrderAsTheLibrary(order("Emma", "Dracula"));

        Delivery delivery = listen(new DirectReceiver(connection)).poll(10, TimeUnit.SECONDS);

        assertThat(delivery).isNotNull();
        assertThat(delivery.message.getBodyList(OrderItem.class)).extracting(OrderItem::getQuantity).containsExactly(1, 2);
    }

    @Test
    void orderThatWasNotHandledComesBackAfterTheAppIsClosed() throws Exception {
        sendOrderAsTheLibrary(order("Emma"));
        DirectReceiver first = new DirectReceiver(newConnection());
        assertThat(listen(first).poll(10, TimeUnit.SECONDS)).isNotNull();

        // Closed before approving or rejecting
        first.shutdown();

        Delivery again = listen(new DirectReceiver(connection)).poll(10, TimeUnit.SECONDS);
        assertThat(again).isNotNull();
        assertThat(again.message.getBodyList(OrderItem.class)).extracting(i -> i.getBook().getTitle())
                .containsExactly("Emma");
    }

    @Test
    void handledOrderIsGoneForGood() throws Exception {
        sendOrderAsTheLibrary(order("Emma"));
        DirectReceiver first = new DirectReceiver(newConnection());
        Delivery delivery = listen(first).poll(10, TimeUnit.SECONDS);

        first.ack(delivery.tag);
        first.shutdown();

        assertThat(listen(new DirectReceiver(connection)).poll(1, TimeUnit.SECONDS)).isNull();
        assertThat(waiting()).isZero();
    }

    @Test
    void queueIsDurable() throws Exception {
        sendOrderAsTheLibrary(order("Emma"));

        // The broker refuses to redeclare a durable queue as non-durable
        Channel channel = connection.createChannel();
        assertThatThrownBy(() -> channel.queueDeclare("orders." + supplier, false, false, false, null))
                .isInstanceOf(IOException.class)
                .rootCause().hasMessageContaining("durable");
    }

    @Test
    void malformedMessageIsDroppedAndTheNextOneStillArrives() throws Exception {
        sendOrderAsTheLibrary("not json".getBytes(StandardCharsets.UTF_8));
        sendOrderAsTheLibrary(order("Ulysses"));

        BlockingQueue<Delivery> orders = listen(new DirectReceiver(connection));

        Delivery delivery = orders.poll(10, TimeUnit.SECONDS);
        assertThat(delivery.message.getBodyList(OrderItem.class)).extracting(i -> i.getBook().getTitle())
                .containsExactly("Ulysses");
        assertThat(orders.poll(1, TimeUnit.SECONDS)).isNull();
    }
}
