package tfus.domain;

import java.util.Collections;
import java.util.List;

/**
 * A single transaction in the dataset.
 * tid: transaction ID (T1, T2, ...)
 * batchNumber: which batch this transaction belongs to (1-based)
 * periodNumber: period index within the dataset (1-based, used for time-decay)
 * items: ordered list of item-quantity pairs
 */
public final class Transaction {
    private final String tid;
    private final int batchNumber;
    private final int periodNumber;
    private final List<ItemQuantity> items;

    public Transaction(String tid, int batchNumber, int periodNumber, List<ItemQuantity> items) {
        this.tid = tid;
        this.batchNumber = batchNumber;
        this.periodNumber = periodNumber;
        this.items = Collections.unmodifiableList(List.copyOf(items));
    }

    public String tid() { return tid; }
    public int batchNumber() { return batchNumber; }
    public int periodNumber() { return periodNumber; }
    public List<ItemQuantity> items() { return items; }

    @Override
    public String toString() {
        return tid + "(batch=" + batchNumber + ",period=" + periodNumber + "," + items + ")";
    }
}
