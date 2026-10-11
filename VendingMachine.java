package VendingMachine;

import java.util.HashMap;
import java.util.Map;
import VendingMachine.DomainModels.Inventory;

// Single-customer version: concurrent access is not supported yet.
public class VendingMachine {
    private long insertedMoney;
    private final Map<Integer, Inventory> inventory;
    private Inventory selectedItem;

    public VendingMachine(Map<Integer, Inventory> inventory) {
        if (inventory == null || inventory.containsValue(null)) {
            throw new IllegalArgumentException("Inventory and its entries must not be null");
        }
        this.inventory = new HashMap<>(inventory);
    }

    public void selectProduct(int productId) {
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

    public void insertMoney(long amount) {
        requireSelection();
        if (amount <= 0 || amount > Long.MAX_VALUE - insertedMoney) {
            throw new IllegalArgumentException("Amount must be positive and fit in the balance");
        }
        insertedMoney += amount;
    }

    public long dispense() {
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

    public long cancel() {
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
    }
}
