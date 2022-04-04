import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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

        getOrders(channel);
    }

    private void getOrders(Channel channel) throws IOException {
        while(true){
            System.out.println("Put your order here:");
            String requestedItem = scanner.nextLine();
            String key = name + "." + requestedItem.toLowerCase();
            String message = name;
            channel.basicPublish(exchange_name, key, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent: " + message + " order " + requestedItem);
        }
    }
}
