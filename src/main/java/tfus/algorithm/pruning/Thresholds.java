package tfus.algorithm.pruning;

/**
 * Floating-point comparison utilities for threshold checks.
 * Uses epsilon to handle accumulated rounding errors.
 */
public final class Thresholds {
    /** Epsilon for comparisons at exact threshold boundaries */
    public static final double EPSILON = 1e-12;

    private Thresholds() {}

    /** Returns true if value >= threshold (allowing tiny rounding at boundary) */
    public static boolean atLeast(double value, double threshold) {
        return value >= threshold - EPSILON;
    }

    /** Returns true if value < threshold (strictly less, accounting for epsilon) */
    public static boolean lessThan(double value, double threshold) {
        return value < threshold - EPSILON;
    }
}
