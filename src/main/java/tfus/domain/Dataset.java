package tfus.domain;

import java.util.*;

/**
 * Complete dataset including transactions, external utility (EU) map,
 * max quantity (M) per item, and configuration parameters.
 */
public final class Dataset {
    private final String name;
    private final List<Transaction> transactions;
    private final List<Batch> batches;
    private final Map<String, Double> externalUtility;  // EU per item
    private final Map<String, Integer> maxQuantity;      // M(i) per item
    private final int batchSize;
    private final int periodSize;

    private Dataset(Builder b) {
        this.name = b.name;
        this.transactions = Collections.unmodifiableList(b.transactions);
        this.batches = Collections.unmodifiableList(b.batches);
        this.externalUtility = Collections.unmodifiableMap(b.externalUtility);
        this.maxQuantity = Collections.unmodifiableMap(b.maxQuantity);
        this.batchSize = b.batchSize;
        this.periodSize = b.periodSize;
    }

    public String name() { return name; }
    public List<Transaction> transactions() { return transactions; }
    public List<Batch> batches() { return batches; }
    public Map<String, Double> externalUtility() { return externalUtility; }
    public Map<String, Integer> maxQuantity() { return maxQuantity; }
    public int batchSize() { return batchSize; }
    public int periodSize() { return periodSize; }

    public double eu(String item) {
        return externalUtility.getOrDefault(item, 0.0);
    }

    public int m(String item) {
        return maxQuantity.getOrDefault(item, 1);
    }

    // -------------------------------------------------------------------------
    public static final class Builder {
        private String name = "dataset";
        private final List<ItemQuantity[]> rawTransactions = new ArrayList<>();
        private final List<String> tidList = new ArrayList<>();
        private final Map<String, Double> externalUtility = new LinkedHashMap<>();
        private Map<String, Integer> maxQuantity = new LinkedHashMap<>();
        private int batchSize = 2;
        private int periodSize = 1; // default: each transaction is one period

        private List<Transaction> transactions;
        private List<Batch> batches;

        public Builder name(String n) { this.name = n; return this; }
        public Builder batchSize(int bs) { this.batchSize = bs; return this; }
        public Builder periodSize(int ps) { this.periodSize = ps; return this; }

        public Builder addTransaction(String tid, List<ItemQuantity> items) {
            rawTransactions.add(items.toArray(new ItemQuantity[0]));
            tidList.add(tid);
            return this;
        }

        public Builder eu(String item, double value) {
            externalUtility.put(item, value);
            return this;
        }

        public Builder maxQuantity(String item, int value) {
            maxQuantity.put(item, value);
            return this;
        }

        public Builder maxQuantities(Map<String, Integer> map) {
            maxQuantity.putAll(map);
            return this;
        }

        public Dataset build() {
            // Derive M(i) if not provided: max quantity seen across all transactions
            Map<String, Integer> derivedMax = new LinkedHashMap<>(maxQuantity);
            for (ItemQuantity[] row : rawTransactions) {
                for (ItemQuantity iq : row) {
                    derivedMax.merge(iq.item(), iq.quantity(), Math::max);
                }
            }
            // Only set derived max for items not explicitly declared
            for (Map.Entry<String, Integer> e : derivedMax.entrySet()) {
                maxQuantity.putIfAbsent(e.getKey(), e.getValue());
            }

            // Build transactions with batch/period assignment
            transactions = new ArrayList<>();
            for (int i = 0; i < rawTransactions.size(); i++) {
                int batchNum = (i / batchSize) + 1;
                int periodNum = (i / periodSize) + 1;
                transactions.add(new Transaction(
                    tidList.get(i), batchNum, periodNum,
                    Arrays.asList(rawTransactions.get(i))
                ));
            }

            // Group into batches
            batches = new ArrayList<>();
            Map<Integer, List<Transaction>> grouped = new LinkedHashMap<>();
            for (Transaction t : transactions) {
                grouped.computeIfAbsent(t.batchNumber(), k -> new ArrayList<>()).add(t);
            }
            for (Map.Entry<Integer, List<Transaction>> e : grouped.entrySet()) {
                batches.add(new Batch(e.getKey(), e.getValue()));
            }

            this.maxQuantity = derivedMax;
            return new Dataset(this);
        }
    }
}
