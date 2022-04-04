import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Supplier {

    String name;
    ArrayList<String> products;
    Scanner scanner = new Scanner(System.in);
    String exchange_name = "SERVICES";
    Channel channel;

    public Supplier() throws IOException, TimeoutException {

        System.out.println("SUPPLIER");
        System.out.println("Put your name here: ");
        name = scanner.nextLine();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(exchange_name, BuiltinExchangeType.TOPIC);

        products = new ArrayList<>();
        defineItems();
        registerProducts();

    }

    private void defineItems(){
        System.out.println("Put the list of available items");
        System.out.println("Writing 'submit' ends the list");
        while(true){
            String product = scanner.nextLine();
            if(product.equals("submit")) break;
            products.add(product);
        }
        System.out.println("List saved successfully");
    }

    private void registerProducts() throws IOException {
        for(String product: products){
            channel.queueDeclare(product, false, false, false, null);
            String key = "*." + product;
            channel.queueBind(product, exchange_name, key);
            getOrders(product);
        }
    }

    private void getOrders(String queueName) throws IOException {
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                String item = getItemType(envelope.getRoutingKey());
                System.out.println("Received: " + message + " want " + item);

                /// response
                String key = "order."+message;
                String response = "Supplier " + name + " realised order: " + item + " with id: " + generateOrderID();
                channel.basicPublish(exchange_name, key, null, response.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sent: " + response + " to " + message);
            }
        };

        channel.basicConsume(queueName, true, consumer);
    }

    private String getItemType(String key){
        return key.split("\\.")[1];
    }

    private int generateOrderID(){
        return new Random().nextInt(1, 999999);
    }
}
