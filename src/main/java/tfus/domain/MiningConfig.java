package tfus.domain;

/**
 * Configuration for a mining run.
 */
public final class MiningConfig {
    private final int windowSize;   // ws: max batches in window
    private final double delta;     // δ: minimum utility threshold
    private final String pruningStrategy;   // "REVISED_UPPER_BOUND" | "NONE"
    private final RecursionRule recursionRule;

    public enum RecursionRule {
        /** Continue recursion if at least 1 conditional list exists (paper algorithm) */
        AT_LEAST_ONE,
        /** Continue recursion only if at least 2 conditional lists exist */
        AT_LEAST_TWO
    }

    private MiningConfig(Builder b) {
        this.windowSize = b.windowSize;
        this.delta = b.delta;
        this.pruningStrategy = b.pruningStrategy;
        this.recursionRule = b.recursionRule;
    }

    public int windowSize() { return windowSize; }
    public double delta() { return delta; }
    public String pruningStrategy() { return pruningStrategy; }
    public RecursionRule recursionRule() { return recursionRule; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int windowSize = 3;
        private double delta = 0.2;
        private String pruningStrategy = "REVISED_UPPER_BOUND";
        private RecursionRule recursionRule = RecursionRule.AT_LEAST_ONE;

        public Builder windowSize(int ws) { this.windowSize = ws; return this; }
        public Builder delta(double d) { this.delta = d; return this; }
        public Builder pruningStrategy(String ps) { this.pruningStrategy = ps; return this; }
        public Builder recursionRule(RecursionRule rr) { this.recursionRule = rr; return this; }

        public MiningConfig build() {
            if (windowSize < 1) throw new IllegalArgumentException("windowSize must be >= 1");
            if (delta <= 0 || delta > 1) throw new IllegalArgumentException("delta must be in (0, 1]");
            return new MiningConfig(this);
        }
    }
}
