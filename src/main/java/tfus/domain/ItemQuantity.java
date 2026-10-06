package tfus.domain;

/** Represents an item with its quantity in a transaction. */
public record ItemQuantity(String item, int quantity) {
    public ItemQuantity {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("Item name must not be blank");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive: " + quantity);
    }
}
