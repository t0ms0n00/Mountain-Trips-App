import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Main {

    public static void main(String[] args) throws IOException, TimeoutException {
        boolean hasRole = false;
        Scanner scanner = new Scanner(System.in);
        while(!hasRole) {
            System.out.println("Define your role: ");
            String role = scanner.nextLine();
            switch (role) {
                case "supplier" -> {
                    new Supplier();
                    hasRole = true;
                }
                case "crew" -> {
                    new Crew();
                    hasRole = true;
                }
                case "admin" -> {
                    System.out.println("Admin");
                    hasRole = true;
                }
                default -> System.out.println("Possible roles are: admin, crew or supplier");
            }
        }
    }
}
