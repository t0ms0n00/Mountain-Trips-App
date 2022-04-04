import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Admin {

    Scanner scanner = new Scanner(System.in);
    String exchange_name = "SERVICES";
    Channel channel;

    public Admin() throws IOException, TimeoutException {
        System.out.println("ADMIN");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(exchange_name, BuiltinExchangeType.TOPIC);

        listen();
    }

    private void listen() throws IOException {
        String queueName = "admin";
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchange_name, "#");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

}
