package VendingMachine.DomainModels;

public class Inventory {
    private final int id;
    private int inStock;
    private final String code;
    private final int itemId;
    private final long price;

    public Inventory(int id, int inStock, String code, int itemId, long price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.id = id;
        this.code = code;
        this.itemId = itemId;
        this.price = price;
        setInStock(inStock);
    }

    public int getId() { return id; }
    public int getInStock() { return inStock; }
    public String getCode() { return code; }
    public int getItemId() { return itemId; }
    public long getPrice() { return price; }

    public void setInStock(int inStock) {
        if (inStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.inStock = inStock;
    }
}
