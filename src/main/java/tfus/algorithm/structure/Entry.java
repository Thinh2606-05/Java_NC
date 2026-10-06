package tfus.algorithm.structure;

/**
 * A single entry in a TFUS-list.
 * Represents one transaction's contribution to a fuzzy item pattern.
 *
 * Fields:
 *   tidIndex    : transaction index (0-based sequential ID for linking)
 *   tid         : transaction ID string (T1, T2, ...)
 *   batchNumber : batch this transaction belongs to
 *   periodNumber: period index for temporal utility calculation
 *   utility     : U(i,T) = quantity × EU(i)  [CT1]
 *   minFv       : min membership value for the pattern in this transaction [CT2/CT5]
 *   remainingUtility : RU - remaining utility of items after this pattern [CT10]
 */
public final class Entry {
    private final int tidIndex;
    private final String tid;
    private final int batchNumber;
    private final int periodNumber;
    private final double utility;       // U(i,T)
    private final double minFv;         // minimum fuzzy value of pattern in T
    private double remainingUtility;    // RU (mutable, set during restructure)

    public Entry(int tidIndex, String tid, int batchNumber, int periodNumber,
                 double utility, double minFv) {
        this.tidIndex = tidIndex;
        this.tid = tid;
        this.batchNumber = batchNumber;
        this.periodNumber = periodNumber;
        this.utility = utility;
        this.minFv = minFv;
        this.remainingUtility = 0.0;
    }

    public int tidIndex() { return tidIndex; }
    public String tid() { return tid; }
    public int batchNumber() { return batchNumber; }
    public int periodNumber() { return periodNumber; }
    public double utility() { return utility; }
    public double minFv() { return minFv; }
    public double remainingUtility() { return remainingUtility; }

    /** CT3: fuzzy utility of this entry = minFv × U */
    public double fuzzyUtility() {
        return minFv * utility;
    }

    /** CT10: fuzzy remaining utility = minFv × RU */
    public double fuzzyRemainingUtility() {
        return minFv * remainingUtility;
    }

    public void setRemainingUtility(double ru) {
        this.remainingUtility = ru;
    }

    @Override
    public String toString() {
        return String.format("Entry{tid=%s,batch=%d,period=%d,U=%.4f,Fv=%.4f,RU=%.4f}",
            tid, batchNumber, periodNumber, utility, minFv, remainingUtility);
    }
}
