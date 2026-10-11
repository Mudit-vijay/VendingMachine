package VendingMachine;

import java.util.HashMap;
import java.util.Map;

import VendingMachine.DomainModels.Inventory;

// One active purchase; operations synchronize on this machine instance.
public class VendingMachine {
    private long insertedMoney;
    private final Map<Integer, Inventory> inventory;
    private Inventory selectedItem;
    private Purchase purchaseSession;
    private long nextPurchaseId;

    public synchronized Purchase startPurchase() {
        if (purchaseSession != null) {
            throw new IllegalStateException("Machine is busy");
        }

        this.purchaseSession = new Purchase(nextPurchaseId++);
        return this.purchaseSession;
    }

    public VendingMachine(Map<Integer, Inventory> inventory) {

        if (inventory == null || inventory.containsValue(null)) {
            throw new IllegalArgumentException("Inventory and its entries must not be null");
        }
        this.inventory = new HashMap<>(inventory);
    }

    public synchronized void selectProduct(long purchaseId, int productId) {

        requireActivePurchase(purchaseId);


        if (selectedItem != null) {
            throw new IllegalStateException("A product is already selected");
        }
        Inventory item = inventory.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Unknown product ID");
        }
        if (item.getInStock() <= 0) {
            throw new IllegalStateException("Product is out of stock");
        }
        selectedItem = item;
    }

    public synchronized void insertMoney(long purchaseId, long amount) {

        requireActivePurchase(purchaseId);

        requireSelection();
        if (amount <= 0 || amount > Long.MAX_VALUE - insertedMoney) {
            throw new IllegalArgumentException("Amount must be positive and fit in the balance");
        }
        insertedMoney += amount;
    }

    public synchronized long dispense(long purchaseId) {

        requireActivePurchase(purchaseId);

        requireSelection();
        if (selectedItem.getInStock() <= 0) {
            throw new IllegalStateException("Product is out of stock");
        }
        if (insertedMoney < selectedItem.getPrice()) {
            throw new IllegalStateException("Insufficient payment");
        }
        long change = insertedMoney - selectedItem.getPrice();
        selectedItem.setInStock(selectedItem.getInStock() - 1);
        resetPurchase();
        return change;
    }

    public synchronized long cancel(long purchaseId) {

        requireActivePurchase(purchaseId);

        long refund = insertedMoney;
        resetPurchase();
        return refund;
    }

    private void requireSelection() {
        if (selectedItem == null) {
            throw new IllegalStateException("Select a product first");
        }
    }

    private void resetPurchase() {
        insertedMoney = 0;
        selectedItem = null;
        purchaseSession = null;
    }

    private void requireActivePurchase(long purchaseId) {
        if (purchaseSession == null
                || purchaseSession.getId() != purchaseId) {
            throw new IllegalStateException(
                    "Invalid or finished purchase");
        }
    }

}
