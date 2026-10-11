package VendingMachine;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import VendingMachine.DomainModels.Inventory;

public class Main {
    public static void main(String[] args) {
        Map<Integer, Inventory> inventory = new LinkedHashMap<>();
        inventory.put(1, new Inventory(1, 3, "CHIPS", 1, 30));
        inventory.put(2, new Inventory(2, 2, "JUICE", 2, 40));
        VendingMachine machine = new VendingMachine(inventory);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                inventory.forEach((id, item) -> System.out.println(
                        id + ". " + item.getCode() + " - Rs " + item.getPrice()
                                + " - Stock: " + item.getInStock()));
                System.out.println("""
                        1. Select product
                        2. Insert money
                        3. Dispense
                        4. Cancel purchase
                        5. Exit application
                        """);
                System.out.print("Enter choice: ");
                if (!scanner.hasNextLine()) {
                    System.out.println("Refund: Rs " + machine.cancel());
                    break;
                }
                try {
                    int choice = Integer.parseInt(scanner.nextLine());
                    switch (choice) {
                        case 1 -> {
                            System.out.print("Enter product ID: ");
                            if (!scanner.hasNextLine()) continue;
                            machine.selectProduct(Integer.parseInt(scanner.nextLine()));
                            System.out.println("Product selected");
                        }
                        case 2 -> {
                            System.out.print("Enter amount in rupees: ");
                            if (!scanner.hasNextLine()) continue;
                            machine.insertMoney(Long.parseLong(scanner.nextLine()));
                            System.out.println("Payment accepted");
                        }
                        case 3 -> System.out.println(
                                "Product dispensed. Change: Rs " + machine.dispense());
                        case 4 -> System.out.println("Refund: Rs " + machine.cancel());
                        case 5 -> {
                            System.out.println("Refund: Rs " + machine.cancel());
                            running = false;
                        }
                        default -> System.out.println("Choose between 1 and 5");
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
