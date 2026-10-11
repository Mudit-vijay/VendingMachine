package VendingMachine;

import java.util.HashMap;
import java.util.Map;
import VendingMachine.DomainModels.Inventory;

public class VendingMachineChecks {
    public static void main(String[] args) {
        Inventory chips = new Inventory(1, 3, "CHIPS", 1, 30);
        Inventory empty = new Inventory(2, 0, "JUICE", 2, 40);
        Map<Integer, Inventory> stock = new HashMap<>();
        stock.put(1, chips);
        stock.put(2, empty);
        VendingMachine machine = new VendingMachine(stock);

        rejects(IllegalStateException.class, () -> machine.insertMoney(10));
        rejects(IllegalStateException.class, machine::dispense);
        rejects(IllegalArgumentException.class, () -> machine.selectProduct(99));
        rejects(IllegalStateException.class, () -> machine.selectProduct(2));

        machine.selectProduct(1);
        rejects(IllegalStateException.class, () -> machine.selectProduct(1));
        rejects(IllegalArgumentException.class, () -> machine.insertMoney(0));
        rejects(IllegalArgumentException.class, () -> machine.insertMoney(-5));
        machine.insertMoney(20);
        rejects(IllegalStateException.class, machine::dispense);
        check(chips.getInStock() == 3, "Failed dispense must preserve stock");
        machine.insertMoney(10);
        check(machine.dispense() == 0, "Exact payment after partial payment");
        check(chips.getInStock() == 2, "Successful sale reduces stock once");
        rejects(IllegalStateException.class, machine::dispense);
        check(machine.cancel() == 0, "Successful sale resets balance");

        machine.selectProduct(1);
        machine.insertMoney(40);
        check(machine.dispense() == 10, "Overpayment returns change");
        machine.selectProduct(1);
        machine.insertMoney(20);
        check(machine.cancel() == 20, "Cancellation refunds all payment");
        check(chips.getInStock() == 1, "Cancellation preserves stock");
        check(machine.cancel() == 0, "Repeated cancellation refunds zero");

        machine.selectProduct(1);
        machine.insertMoney(Long.MAX_VALUE);
        rejects(IllegalArgumentException.class, () -> machine.insertMoney(1));
        check(machine.cancel() == Long.MAX_VALUE, "Overflow rejection preserves balance");

        machine.selectProduct(1);
        machine.insertMoney(30);
        machine.dispense();
        rejects(IllegalStateException.class, () -> machine.selectProduct(1));
        rejects(IllegalArgumentException.class, () -> chips.setInStock(-1));
        rejects(IllegalArgumentException.class, () -> new Inventory(3, 1, "BAD", 3, -1));
        rejects(IllegalArgumentException.class, () -> new Inventory(3, -1, "BAD", 3, 10));
        System.out.println("All vending-machine checks passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void rejects(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            if (type.isInstance(exception)) return;
            throw new AssertionError("Unexpected exception", exception);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}
