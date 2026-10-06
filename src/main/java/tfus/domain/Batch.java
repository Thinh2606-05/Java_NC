package tfus.domain;

import java.util.Collections;
import java.util.List;

/**
 * A batch groups consecutive transactions.
 * batchNumber: 1-based batch index
 */
public final class Batch {
    private final int batchNumber;
    private final List<Transaction> transactions;

    public Batch(int batchNumber, List<Transaction> transactions) {
        this.batchNumber = batchNumber;
        this.transactions = Collections.unmodifiableList(List.copyOf(transactions));
    }

    public int batchNumber() { return batchNumber; }
    public List<Transaction> transactions() { return transactions; }

    @Override
    public String toString() {
        return "B" + batchNumber + transactions;
    }
}
