package tfus.verification;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Complete verification report from comparing algorithm output to hand-run data.
 */
public final class VerificationReport {
    private final String datasetName;
    private final List<Check> checks;

    public VerificationReport(String datasetName, List<Check> checks) {
        this.datasetName = datasetName;
        this.checks = Collections.unmodifiableList(List.copyOf(checks));
    }

    public String datasetName() { return datasetName; }
    public List<Check> checks() { return checks; }

    public long passCount() {
        return checks.stream().filter(c -> c.status() == Check.Status.PASS).count();
    }

    public long roundingCount() {
        return checks.stream().filter(c -> c.status() == Check.Status.ROUNDING).count();
    }

    public long mismatchCount() {
        return checks.stream().filter(c -> c.status() == Check.Status.MISMATCH).count();
    }

    public long missingCount() {
        return checks.stream().filter(c -> c.status() == Check.Status.MISSING).count();
    }

    public long infoCount() {
        return checks.stream().filter(c -> c.status() == Check.Status.INFO).count();
    }

    public boolean isFullyPassing() {
        return mismatchCount() == 0 && missingCount() == 0;
    }

    public List<Check> byGroup(CheckGroup group) {
        return checks.stream().filter(c -> c.group() == group).toList();
    }

    public String summary() {
        return String.format("Dataset: %s | Total: %d | Pass: %d | Rounding: %d | Mismatch: %d | Missing: %d | Info: %d",
            datasetName, checks.size(), passCount(), roundingCount(), mismatchCount(), missingCount(), infoCount());
    }
}
