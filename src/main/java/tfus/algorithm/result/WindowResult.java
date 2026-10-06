package tfus.algorithm.result;

import tfus.domain.Pattern;
import tfus.snapshot.ListsSnapshot;
import java.util.*;

/**
 * Result of mining one window.
 */
public final class WindowResult {
    private final int windowNumber;
    private final List<Pattern> patterns;
    private final List<PatternEvaluation> allEvaluations;
    private final ListsSnapshot snapshot;   // state after restructure (Memento)
    private final long elapsedMs;

    public WindowResult(int windowNumber, List<Pattern> patterns,
                        List<PatternEvaluation> allEvaluations,
                        ListsSnapshot snapshot, long elapsedMs) {
        this.windowNumber = windowNumber;
        this.patterns = Collections.unmodifiableList(List.copyOf(patterns));
        this.allEvaluations = Collections.unmodifiableList(List.copyOf(allEvaluations));
        this.snapshot = snapshot;
        this.elapsedMs = elapsedMs;
    }

    public int windowNumber() { return windowNumber; }
    public List<Pattern> patterns() { return patterns; }
    public List<PatternEvaluation> allEvaluations() { return allEvaluations; }
    public ListsSnapshot snapshot() { return snapshot; }
    public long elapsedMs() { return elapsedMs; }
}
