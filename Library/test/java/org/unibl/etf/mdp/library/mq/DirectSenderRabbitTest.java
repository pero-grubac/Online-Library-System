package org.unibl.etf.mdp.library.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.unibl.etf.mdp.common.Json;
import org.unibl.etf.mdp.model.BookDto;
import org.unibl.etf.mdp.model.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Orders through a real RabbitMQ in Docker; skipped when Docker is not available. */
@Testcontainers(disabledWithoutDocker = true)
class DirectSenderRabbitTest {

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-alpine"));

    @Test
    void orderWaitsInTheSuppliersQueueAsPersistentJson() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT.getHost());
        factory.setPort(RABBIT.getAmqpPort());
        factory.setUsername(RABBIT.getAdminUsername());
        factory.setPassword(RABBIT.getAdminPassword());
        BookDto emma = new BookDto();
        emma.setTitle("Emma");

        try (Connection connection = factory.newConnection()) {
            // No supplier is listening yet
            new DirectSender(connection).sendMessage("BookSupplier_Z", new Message("REQUEST", "library", List.of(emma)));

            try (Channel channel = connection.createChannel()) {
                GetResponse waiting = channel.basicGet("orders.BookSupplier_Z", true);
                assertThat(waiting).isNotNull();
                assertThat(waiting.getProps().getDeliveryMode()).isEqualTo(2);
                Message order = Json.messageFromBytes(waiting.getBody(), waiting.getBody().length);
                assertThat(order.getType()).isEqualTo("REQUEST");
                assertThat(order.getBodyList(BookDto.class)).extracting(BookDto::getTitle).containsExactly("Emma");
            }
        }
    }
}
