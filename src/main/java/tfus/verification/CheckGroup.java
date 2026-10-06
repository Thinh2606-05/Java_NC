package tfus.verification;

/**
 * Categories of verification checks.
 */
public enum CheckGroup {
    U_FV("Utility and Fuzzy Values (CT1-CT2)"),
    TFU_MTFU("Transaction Fuzzy Utility (CT6-CT7)"),
    FUUB_TFUUBR("Fuzzy Utility Upper Bound (CT8-CT9)"),
    PRUNED_ITEMS("Pruned Items"),
    PROCESSING_ORDER("Processing Order"),
    TFUS_LISTS("TFUS-List Entries"),
    BATCH_REMOVAL("Batch Removal (CT11)"),
    CONDITIONAL_LISTS("Conditional Lists"),
    PRUNING_DECISIONS("Pruning Decisions (CT13)"),
    TFUR_VALUES("tFUR Values (CT12)"),
    PATTERN_COUNT("Pattern Count"),
    PATTERN_SET("Pattern Set"),
    RESULT_SET("Result Set"),
    WINDOW_DELTA("Changes Between Windows"),
    LEMMA_PRUNING("Lemma-based Pruning"),
    RECURSION_LITERAL("Recursion Literal |NSL|>=2"),
    CANDIDATE_FU("Exhaustive Candidate Fuzzy Utility (FU)"),
    CANDIDATE_TFUR("Exhaustive Candidate Temporal Ratio (tFUR)"),
    CANDIDATE_STP("Exhaustive Candidate Start Period (PS)");

    private final String description;

    CheckGroup(String description) {
        this.description = description;
    }

    public String description() { return description; }
}
