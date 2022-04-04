import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Supplier {

    String name;
    ArrayList<String> products;
    Scanner scanner = new Scanner(System.in);
    String exchange_name = "SERVICES";

    public Supplier() throws IOException, TimeoutException {

        System.out.println("SUPPLIER");

        System.out.println("Put your name here: ");
        name = scanner.nextLine();

        products = new ArrayList<>();

        defineItems();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(exchange_name, BuiltinExchangeType.TOPIC);

        registerProducts(channel);

    }

    private void defineItems(){
        System.out.println("Put the list of available items");
        System.out.println("Writing submit ends the list");
        while(true){
            String product = scanner.nextLine();
            if(product.equals("submit")) break;
            products.add(product);
        }
    }

    private void registerProducts(Channel channel) throws IOException {
        for(String product: products){
            channel.queueDeclare(product, false, false, false, null);
            String key = "*." + product;
            channel.queueBind(product, exchange_name, key);
            System.out.println("Created product queue: " + product);
            handleMessage(channel, product);
        }
    }

    private void handleMessage(Channel channel, String queueName) throws IOException {
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body, StandardCharsets.UTF_8);
                String item = getItemType(envelope.getRoutingKey());
                System.out.println("Received: " + message + " want " + item);
            }
        };

        channel.basicConsume(queueName, true, consumer);
    }

    private String getItemType(String key){
        return key.split("\\.")[1];
    }
}
