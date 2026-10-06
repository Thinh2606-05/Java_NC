package tfus.algorithm.structure;

import tfus.domain.FuzzyItem;
import java.util.*;

/**
 * TFUS-List: the core data structure for a fuzzy item (or pattern).
 * Maintains:
 *   - Ordered list of entries (one per transaction containing this item)
 *   - FU: sum of fuzzy utilities across all entries [CT4]
 *   - FUUB: fuzzy utility upper bound [CT8] (sum of MTFU of transactions)
 *   - RFU: remaining fuzzy utility [CT10] (updated during restructure)
 *   - mSTFU: maximum start-period transaction fuzzy utility [CT11]
 */
public final class TFUSList {
    private final FuzzyItem fuzzyItem;
    private final List<Entry> entries;  // ordered by tidIndex
    private double fu;      // sum of minFv * U
    private double fuub;    // sum of MTFU for transactions containing this item
    private double rfu;     // sum of minFv * RU
    private double mstfu;   // max-start-period TFU (for pruning bound)

    public TFUSList(FuzzyItem fuzzyItem) {
        this.fuzzyItem = fuzzyItem;
        this.entries = new ArrayList<>();
        this.fu = 0.0;
        this.fuub = 0.0;
        this.rfu = 0.0;
        this.mstfu = 0.0;
    }

    public FuzzyItem fuzzyItem() { return fuzzyItem; }
    public List<Entry> entries() { return Collections.unmodifiableList(entries); }
    public double fu() { return fu; }
    public double fuub() { return fuub; }
    public double rfu() { return rfu; }
    public double mstfu() { return mstfu; }

    public void setFuub(double fuub) { this.fuub = fuub; }
    public void setRfu(double rfu) { this.rfu = rfu; }
    public void setMstfu(double mstfu) { this.mstfu = mstfu; }

    /** Add entry and update FU (CT4). */
    public void addEntry(Entry e) {
        entries.add(e);
        fu += e.fuzzyUtility();
    }

    /** Increase FUUB by delta (called when scanning transaction). */
    public void addToFuub(double delta) {
        fuub += delta;
    }

    /**
     * Remove all entries from a given batch; update FU and FUUB accordingly.
     * @param batchNumber the batch being removed
     * @param ubTable the UBTable to look up MTFU values
     * @return list of entries that were removed
     */
    public List<Entry> removeBatch(int batchNumber, UBTable ubTable) {
        List<Entry> removed = new ArrayList<>();
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (e.batchNumber() == batchNumber) {
                fu -= e.fuzzyUtility();
                double mtfu = ubTable.getMtfu(e.tidIndex());
                fuub -= mtfu;
                removed.add(e);
                it.remove();
            }
        }
        return removed;
    }

    /** Recompute RFU from current entry RU values. */
    public void recomputeRfu() {
        double sum = 0.0;
        for (Entry e : entries) {
            sum += e.fuzzyRemainingUtility();
        }
        this.rfu = sum;
    }

    public boolean isEmpty() { return entries.isEmpty(); }
    public int size() { return entries.size(); }

    /** Get entry by tidIndex (binary search since entries are ordered). */
    public Entry getByTidIndex(int tidIndex) {
        // Linear search (list is typically small)
        for (Entry e : entries) {
            if (e.tidIndex() == tidIndex) return e;
            if (e.tidIndex() > tidIndex) return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return fuzzyItem.label() + "[FU=" + String.format("%.4f", fu)
            + ",FUUB=" + String.format("%.4f", fuub)
            + ",entries=" + entries.size() + "]";
    }
}
