package tfus.algorithm.result;

import tfus.domain.FuzzyItem;
import java.util.List;

/**
 * Result of evaluating a single candidate pattern during extension.
 */
public record PatternEvaluation(
    List<FuzzyItem> pattern,
    double fu,
    double rfu,
    double mstfu,
    double rTFUUBR,
    double tFUR,
    int startPeriod,
    boolean pruned,
    boolean isResult
) {}
