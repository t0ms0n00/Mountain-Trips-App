import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Crew {

    String name;
    Scanner scanner = new Scanner(System.in);
    String exchange_name = "SERVICES";

    public Crew() throws IOException, TimeoutException {

        System.out.println("CREW");
        System.out.println("Put your name here: ");
        name = scanner.nextLine();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchange_name, BuiltinExchangeType.TOPIC);

        listenSupplierResponses(channel);

        makeOrders(channel);
    }

    private void makeOrders(Channel channel) throws IOException {
        while(true){
            System.out.println("Put your order here:");
            String requestedItem = scanner.nextLine();
            if(requestedItem.equals("exit")) break;
            String key = name + "." + requestedItem.toLowerCase();
            String message = name;
            channel.basicPublish(exchange_name, key, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent: " + message + " order " + requestedItem);
        }
    }

    private void listenSupplierResponses(Channel channel) throws IOException {
        String queueName = name+"_orders";
        channel.queueDeclare(queueName, false, false, false, null);
        String key = "order."+name;
        channel.queueBind(queueName, exchange_name, key);
        System.out.println("Initialize queue for responses from suppliers with name: " + queueName);
        handleResponse(channel, queueName);
    }

    private void handleResponse(Channel channel, String queueName) throws IOException {
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
