package VendingMachine;

import java.util.HashMap;
import java.util.Map;

import VendingMachine.DomainModels.Inventory;

public class ConcurrencyDemo {
    public static void main(String[] args) throws InterruptedException {
        Map<Integer, Inventory> inventory = new HashMap<>();
        inventory.put(1, new Inventory(1, 3, "CHIPS", 1, 30));
        VendingMachine machine = new VendingMachine(inventory);

        // Each thread writes only to its own slot. Main reads after join().
        Purchase[] purchases = new Purchase[2];
        boolean[] busy = new boolean[2];
        Throwable[] failures = new Throwable[2];

        Thread customerA = new Thread(
                () -> tryPurchase(machine, 0, purchases, busy, failures), "Customer A");
        Thread customerB = new Thread(
                () -> tryPurchase(machine, 1, purchases, busy, failures), "Customer B");

        customerA.start();
        customerB.start();
        customerA.join();
        customerB.join();

        int started = 0;
        int rejected = 0;
        Purchase winner = null;
        for (int i = 0; i < 2; i++) {
            if (failures[i] != null) {
                throw new AssertionError("Unexpected customer failure", failures[i]);
            }
            if (purchases[i] != null) {
                started++;
                winner = purchases[i];
            }
            if (busy[i]) rejected++;
        }
        if (started != 1 || rejected != 1) {
            throw new AssertionError("Expected one successful start and one busy rejection");
        }
        System.out.println("PASS: Exactly one purchase started; the other customer was rejected.");

        // Only finish the winner after both threads have attempted to start.
        long finishedId = winner.getId();
        machine.cancel(finishedId);
        Purchase next = machine.startPurchase();
        if (next.getId() == finishedId) {
            throw new AssertionError("New purchase must have a different ID");
        }
        try {
            machine.cancel(finishedId);
            throw new AssertionError("Finished purchase was accepted");
        } catch (IllegalStateException expected) {
            if (!"Invalid or finished purchase".equals(expected.getMessage())) throw expected;
        }
        machine.selectProduct(next.getId(), 1);
        machine.insertMoney(next.getId(), 40);
        long change = machine.dispense(next.getId());
        if (change != 10 || inventory.get(1).getInStock() != 2) {
            throw new AssertionError("Unexpected change or stock after purchase");
        }
        System.out.println("PASS: Old ID rejected; next purchase dispensed with Rs 10 change.");
    }

    private static void tryPurchase(VendingMachine machine, int slot,
            Purchase[] purchases, boolean[] busy, Throwable[] failures) {
        try {
            purchases[slot] = machine.startPurchase();
            System.out.println(Thread.currentThread().getName()
                    + " started purchase " + purchases[slot].getId());
            // Leave it active so the other customer's attempt must see busy.
        } catch (IllegalStateException exception) {
            if ("Machine is busy".equals(exception.getMessage())) {
                busy[slot] = true;
                System.out.println(Thread.currentThread().getName() + ": Machine is busy");
            } else {
                failures[slot] = exception;
            }
        } catch (Throwable failure) {
            failures[slot] = failure;
        }
    }
}