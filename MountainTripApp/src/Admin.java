import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Admin {

    Scanner scanner = new Scanner(System.in);
    String exchange_name = "SERVICES";
    Channel channel;
    ArrayList<String> targets = new ArrayList<>();

    public Admin() throws IOException, TimeoutException {
        System.out.println("ADMIN");

        targets.add("suppliers");
        targets.add("crews");
        targets.add("all");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(exchange_name, BuiltinExchangeType.TOPIC);

        listen();

        sendMessage();
    }

    private void listen() throws IOException {
        String queueName = "admin";
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchange_name, "#");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Received: " + message);
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    private void sendMessage() throws IOException {
        while(true) {
            System.out.println("Write your message here: ");
            String message = scanner.nextLine();
            message = "[ADMIN] " + message;

            System.out.println("Who should receive your message?");
            String target = scanner.nextLine();
            while(!targets.contains(target)){
                System.out.println("Possible targets are: suppliers, crews or all");
                target = scanner.nextLine();
            }

            String key;
            if(target.equals("suppliers")) key = "admin.suppliers";
            else if (target.equals("crews")) key = "admin.crews";
            else key = "admin.suppliers.crews";

            channel.basicPublish(exchange_name, key, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent: " + message + " to " + target);
        }
    }

}
