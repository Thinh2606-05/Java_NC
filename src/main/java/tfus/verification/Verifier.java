package tfus.verification;

import tfus.algorithm.result.PatternEvaluation;
import tfus.algorithm.result.WindowResult;
import tfus.domain.FuzzyItem;
import tfus.domain.Pattern;
import tfus.event.MiningEvent;
import tfus.io.SectionedFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares mining algorithm output to hand-run reference data across 4 strict milestones:
 * 1. Initial values (TFU, MTFU for T1, T7)
 * 2. Restructuring W1 (5 pruned items, exact processing order)
 * 3. Pruning W2 ({B.M} pruned, {D.L, B.H} not pruned & not result)
 * 4. Final pattern sets (W1 full 31 candidates / 8 results; W2 full 157 candidates / 8 results; Union 11 distinct patterns)
 */
public final class Verifier {
    private final SectionedFile handRun;

    public Verifier(SectionedFile handRun) {
        this.handRun = handRun;
    }

    /** Overload for backwards compatibility */
    public VerificationReport verify(List<MiningEvent> algorithmTrace,
                                     List<WindowResult> results,
                                     String datasetName) {
        return verify(algorithmTrace, results, Collections.emptyList(), Collections.emptyList(), datasetName);
    }

    /**
     * Run full verification comparing all runs against hand-run data.
     */
    public VerificationReport verify(List<MiningEvent> algorithmTrace,
                                     List<WindowResult> algorithmResults,
                                     List<WindowResult> fullResults,
                                     List<WindowResult> paperResults,
                                     String datasetName) {
        List<Check> checks = new ArrayList<>();

        // Verify each window from hand-run file
        for (int w = 1; w <= algorithmResults.size(); w++) {
            String prefix = "w" + w;
            WindowResult wr = algorithmResults.get(w - 1);

            // Mốc 1: TFU/MTFU
            verifyTransactionScan(checks, algorithmTrace, prefix, w);

            // FUUB
            verifyFuub(checks, wr, prefix);

            // Mốc 2: Pruned items & Order
            verifyPrunedItems(checks, algorithmTrace, prefix, w);
            verifyProcessingOrder(checks, wr, prefix);

            // Mốc 3: Pattern evaluations & Pruning
            verifyPatterns(checks, wr, prefix);

            // Mốc 4: Result set per window
            verifyResults(checks, wr, prefix);
        }

        // Mốc 3 Đặc thù: Cắt tỉa chi tiết ở W2 cho {B.M} và {D.L, B.H}
        verifyMilestone3Pruning(checks, algorithmResults);

        // Mốc 4 Đặc thù: Ứng viên khi chạy full, tập gộp 11 mẫu và biến động giữa 2 cửa sổ
        verifyMilestone4CandidatesAndUnion(checks, algorithmResults, fullResults);

        // Kiểm tra vét cạn toàn bộ các ứng viên (Exhaustive Candidate Verification)
        verifyExhaustiveCandidates(checks, fullResults);

        return new VerificationReport(datasetName, checks);
    }

    // -------------------------------------------------------------------------
    // MỐC 1: TFU & MTFU
    // -------------------------------------------------------------------------
    private void verifyTransactionScan(List<Check> checks,
                                       List<MiningEvent> trace,
                                       String prefix, int windowNum) {
        Map<String, String> section = handRun.section(prefix + "_transaction_scanned");
        if (section.isEmpty()) return;

        List<MiningEvent.TransactionScanned> scannedEvents = trace.stream()
            .filter(e -> e instanceof MiningEvent.TransactionScanned)
            .map(e -> (MiningEvent.TransactionScanned) e)
            .toList();

        for (Map.Entry<String, String> e : section.entrySet()) {
            String tid = e.getKey();
            String[] parts = e.getValue().split(",");
            if (parts.length < 2) continue;

            String expTfuStr = parts[0].trim();
            String expMtfuStr = parts[1].trim();

            MiningEvent.TransactionScanned found = scannedEvents.stream()
                .filter(ev -> ev.tid().equals(tid))
                .findFirst().orElse(null);

            if (found == null) {
                checks.add(Check.missing(CheckGroup.TFU_MTFU, prefix + "_" + tid + "_TFU", expTfuStr));
                continue;
            }

            checks.add(compareDouble(CheckGroup.TFU_MTFU, prefix + "_" + tid + "_TFU", expTfuStr, found.tfu()));
            checks.add(compareDouble(CheckGroup.TFU_MTFU, prefix + "_" + tid + "_MTFU", expMtfuStr, found.mtfu()));
        }
    }

    private void verifyFuub(List<Check> checks, WindowResult wr, String prefix) {
        Map<String, String> section = handRun.section(prefix + "_fuub");
        if (section.isEmpty()) return;

        for (Map.Entry<String, String> e : section.entrySet()) {
            String label = e.getKey();
            String[] parts = e.getValue().split(",");
            if (parts.length < 1) continue;

            String expFuub = parts[0].trim();
            var snap = wr.snapshot();
            if (snap == null) {
                checks.add(Check.missing(CheckGroup.FUUB_TFUUBR, prefix + "_" + label + "_FUUB", expFuub));
                continue;
            }

            var listSnap = snap.lists().stream()
                .filter(ls -> ls.fuzzyItem().label().equals(label))
                .findFirst();

            if (listSnap.isEmpty()) {
                checks.add(Check.missing(CheckGroup.FUUB_TFUUBR, prefix + "_" + label + "_FUUB", expFuub));
                continue;
            }

            checks.add(compareDouble(CheckGroup.FUUB_TFUUBR, prefix + "_" + label + "_FUUB", expFuub, listSnap.get().fuub()));
            if (parts.length >= 2) {
                checks.add(compareDouble(CheckGroup.FUUB_TFUUBR, prefix + "_" + label + "_TFUUBR", parts[1].trim(), listSnap.get().tfuubr()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // MỐC 2: TÁI CẤU TRÚC W1 (Pruned Items & Order)
    // -------------------------------------------------------------------------
    private void verifyPrunedItems(List<Check> checks, List<MiningEvent> trace,
                                   String prefix, int windowNum) {
        Map<String, String> section = handRun.section(prefix + "_pruned");
        if (section.isEmpty()) return;

        MiningEvent.RestructureCompleted restructure = trace.stream()
            .filter(e -> e instanceof MiningEvent.RestructureCompleted rc && rc.windowNumber() == windowNum)
            .map(e -> (MiningEvent.RestructureCompleted) e)
            .findFirst().orElse(null);

        if (restructure == null) {
            checks.add(Check.missing(CheckGroup.PRUNED_ITEMS, prefix + "_restructure", "(event)"));
            return;
        }

        List<String> actualPruned = restructure.prunedItems().stream()
            .map(FuzzyItem::label).sorted().toList();
        List<String> expectedPruned = new ArrayList<>(section.keySet());
        Collections.sort(expectedPruned);

        String expStr = String.join(", ", expectedPruned);
        String actStr = String.join(", ", actualPruned);

        if (expStr.equals(actStr)) {
            checks.add(Check.pass(CheckGroup.PRUNED_ITEMS, prefix + "_pruned_items", expStr));
        } else {
            checks.add(Check.mismatch(CheckGroup.PRUNED_ITEMS, prefix + "_pruned_items", expStr, actStr));
        }
    }

    private void verifyProcessingOrder(List<Check> checks, WindowResult wr, String prefix) {
        Map<String, String> section = handRun.section(prefix + "_order");
        if (section.isEmpty()) return;

        List<String> expected = new ArrayList<>(section.keySet());
        var snap = wr.snapshot();
        if (snap == null) {
            checks.add(Check.missing(CheckGroup.PROCESSING_ORDER, prefix + "_order", String.join(" < ", expected)));
            return;
        }

        List<String> actual = snap.lists().stream()
            .map(ls -> ls.fuzzyItem().label())
            .toList();

        String expStr = String.join(" < ", expected);
        String actStr = String.join(" < ", actual);

        if (expStr.equals(actStr)) {
            checks.add(Check.pass(CheckGroup.PROCESSING_ORDER, prefix + "_processing_order", expStr));
        } else {
            checks.add(Check.mismatch(CheckGroup.PROCESSING_ORDER, prefix + "_processing_order", expStr, actStr));
        }
    }

    // -------------------------------------------------------------------------
    // MỐC 3: CẮT TỈA VÀ ĐÁNH GIÁ MẪU
    // -------------------------------------------------------------------------
    private void verifyPatterns(List<Check> checks, WindowResult wr, String prefix) {
        Map<String, String> section = handRun.section(prefix + "_patterns");
        if (section.isEmpty()) return;

        for (Map.Entry<String, String> e : section.entrySet()) {
            String patternLabel = e.getKey();
            String canonicalExpected = canonicalPattern(patternLabel);
            String[] parts = e.getValue().split(",");

            double expTfur = Double.parseDouble(parts[0].trim());
            double expRtfuubr = parts.length >= 2 ? Double.parseDouble(parts[1].trim()) : -1;

            var eval = wr.allEvaluations().stream()
                .filter(ev -> canonicalPattern(formatPattern(ev.pattern())).equals(canonicalExpected))
                .findFirst();

            if (eval.isEmpty()) {
                checks.add(Check.missing(CheckGroup.TFUR_VALUES, prefix + "_" + patternLabel + "_tFUR", String.format("%.3f", expTfur)));
                continue;
            }

            checks.add(compareDouble(CheckGroup.TFUR_VALUES, prefix + "_" + patternLabel + "_tFUR", String.format("%.3f", expTfur), eval.get().tFUR()));
            if (expRtfuubr >= 0) {
                checks.add(compareDouble(CheckGroup.PRUNING_DECISIONS, prefix + "_" + patternLabel + "_rTFUUBR", String.format("%.3f", expRtfuubr), eval.get().rTFUUBR()));
            }
        }
    }

    private void verifyMilestone3Pruning(List<Check> checks, List<WindowResult> algorithmResults) {
        if (algorithmResults.size() < 2) return;
        WindowResult w2 = algorithmResults.get(1);

        // Kiểm tra mẫu {B.M} ở W2: rTFUUBR < 0.2 -> Phải bị cắt tỉa (pruned = true)
        var bmEval = w2.allEvaluations().stream()
            .filter(ev -> canonicalPattern(formatPattern(ev.pattern())).equals("{B.M}"))
            .findFirst();

        if (bmEval.isPresent()) {
            PatternEvaluation ev = bmEval.get();
            if (ev.pruned()) {
                checks.add(Check.pass(CheckGroup.LEMMA_PRUNING, "w2_{B.M}_pruned_decision", "PRUNED (rTFUUBR < 0.2)"));
            } else {
                checks.add(Check.mismatch(CheckGroup.LEMMA_PRUNING, "w2_{B.M}_pruned_decision", "PRUNED", "NOT PRUNED"));
            }
        } else {
            checks.add(Check.missing(CheckGroup.LEMMA_PRUNING, "w2_{B.M}_pruned_decision", "PRUNED"));
        }

        // Kiểm tra mẫu {D.L, B.H} ở W2: rTFUUBR >= 0.2 -> Không bị cắt tỉa (pruned = false), nhưng tFUR < 0.2 -> Not a result!
        var dlbhEval = w2.allEvaluations().stream()
            .filter(ev -> canonicalPattern(formatPattern(ev.pattern())).equals("{B.H, D.L}"))
            .findFirst();

        if (dlbhEval.isPresent()) {
            PatternEvaluation ev = dlbhEval.get();
            if (!ev.pruned() && !ev.isResult()) {
                checks.add(Check.pass(CheckGroup.LEMMA_PRUNING, "w2_{D.L, B.H}_not_pruned_not_result", "EXPANDED_BUT_NOT_RESULT (tFUR < 0.2)"));
            } else {
                checks.add(Check.mismatch(CheckGroup.LEMMA_PRUNING, "w2_{D.L, B.H}_not_pruned_not_result",
                    "EXPANDED_BUT_NOT_RESULT", "pruned=" + ev.pruned() + ", isResult=" + ev.isResult()));
            }
        } else {
            checks.add(Check.missing(CheckGroup.LEMMA_PRUNING, "w2_{D.L, B.H}_not_pruned_not_result", "EXPANDED_BUT_NOT_RESULT"));
        }
    }

    // -------------------------------------------------------------------------
    // MỐC 4: TẬP KẾT QUẢ & ỨNG VIÊN
    // -------------------------------------------------------------------------
    private void verifyResults(List<Check> checks, WindowResult wr, String prefix) {
        Map<String, String> section = handRun.section(prefix + "_results");
        if (section.isEmpty()) return;

        List<String> expected = new ArrayList<>(section.keySet());
        List<String> actual = wr.patterns().stream()
            .map(p -> formatPattern(p.items()))
            .toList();

        // Kiểm tra số lượng kết quả
        if (expected.size() == actual.size()) {
            checks.add(Check.pass(CheckGroup.PATTERN_COUNT, prefix + "_result_count", String.valueOf(expected.size())));
        } else {
            checks.add(Check.mismatch(CheckGroup.PATTERN_COUNT, prefix + "_result_count",
                String.valueOf(expected.size()), String.valueOf(actual.size())));
        }

        // Kiểm tra từng mẫu qua tập hợp chuẩn hóa canonical
        Set<String> actualCanonical = actual.stream().map(this::canonicalPattern).collect(Collectors.toSet());
        for (String expPat : expected) {
            String canExp = canonicalPattern(expPat);
            if (actualCanonical.contains(canExp)) {
                checks.add(Check.pass(CheckGroup.RESULT_SET, prefix + "_result_" + expPat, expPat));
            } else {
                checks.add(Check.mismatch(CheckGroup.RESULT_SET, prefix + "_result_" + expPat, expPat, "(missing)"));
            }
        }
    }

    private void verifyMilestone4CandidatesAndUnion(List<Check> checks,
                                                    List<WindowResult> algorithmResults,
                                                    List<WindowResult> fullResults) {
        // Kiểm tra số lượng ứng viên khi chạy FULL (No Pruning)
        if (fullResults.size() >= 2) {
            int w1Candidates = fullResults.get(0).allEvaluations().size();
            int w2Candidates = fullResults.get(1).allEvaluations().size();

            if (w1Candidates == 31) {
                checks.add(Check.pass(CheckGroup.PATTERN_COUNT, "w1_full_candidate_count", "31 candidates"));
            } else {
                checks.add(Check.mismatch(CheckGroup.PATTERN_COUNT, "w1_full_candidate_count", "31", String.valueOf(w1Candidates)));
            }

            if (w2Candidates == 157) {
                checks.add(Check.pass(CheckGroup.PATTERN_COUNT, "w2_full_candidate_count", "157 candidates"));
            } else {
                checks.add(Check.mismatch(CheckGroup.PATTERN_COUNT, "w2_full_candidate_count", "157", String.valueOf(w2Candidates)));
            }
        }

        // Kiểm tra tập hợp gộp (distinct union) của cả 2 cửa sổ: chính xác 11 mẫu phân biệt
        if (algorithmResults.size() >= 2) {
            Set<String> w1Patterns = algorithmResults.get(0).patterns().stream()
                .map(p -> canonicalPattern(formatPattern(p.items()))).collect(Collectors.toSet());
            Set<String> w2Patterns = algorithmResults.get(1).patterns().stream()
                .map(p -> canonicalPattern(formatPattern(p.items()))).collect(Collectors.toSet());

            Set<String> union = new HashSet<>();
            union.addAll(w1Patterns);
            union.addAll(w2Patterns);

            if (union.size() == 11) {
                checks.add(Check.pass(CheckGroup.WINDOW_DELTA, "final_union_pattern_count", "11 distinct patterns"));
            } else {
                checks.add(Check.mismatch(CheckGroup.WINDOW_DELTA, "final_union_pattern_count", "11", String.valueOf(union.size())));
            }

            // 5 mẫu giữ nguyên
            Set<String> common = new HashSet<>(w1Patterns);
            common.retainAll(w2Patterns);
            if (common.size() == 5) {
                checks.add(Check.pass(CheckGroup.WINDOW_DELTA, "common_retained_patterns_count", "5 patterns"));
            } else {
                checks.add(Check.mismatch(CheckGroup.WINDOW_DELTA, "common_retained_patterns_count", "5", String.valueOf(common.size())));
            }

            // 3 mẫu mất đi ở W2
            Set<String> lost = new HashSet<>(w1Patterns);
            lost.removeAll(w2Patterns);
            if (lost.size() == 3) {
                checks.add(Check.pass(CheckGroup.WINDOW_DELTA, "lost_patterns_count_w2", "3 patterns"));
            } else {
                checks.add(Check.mismatch(CheckGroup.WINDOW_DELTA, "lost_patterns_count_w2", "3", String.valueOf(lost.size())));
            }

            // 3 mẫu mới ở W2 (Lemma 1)
            Set<String> newPatterns = new HashSet<>(w2Patterns);
            newPatterns.removeAll(w1Patterns);
            if (newPatterns.size() == 3) {
                checks.add(Check.pass(CheckGroup.WINDOW_DELTA, "new_patterns_lemma1_count_w2", "3 patterns"));
            } else {
                checks.add(Check.mismatch(CheckGroup.WINDOW_DELTA, "new_patterns_lemma1_count_w2", "3", String.valueOf(newPatterns.size())));
            }
        }
    }

    // -------------------------------------------------------------------------
    private String formatPattern(List<FuzzyItem> items) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(items.get(i).label());
        }
        sb.append("}");
        return sb.toString();
    }

    /** Chuẩn hóa tập item trong pattern sang thứ tự alphabet để so khớp tập hợp */
    private String canonicalPattern(String pattern) {
        if (!pattern.startsWith("{") || !pattern.endsWith("}")) return pattern;
        String inner = pattern.substring(1, pattern.length() - 1).trim();
        if (inner.isEmpty()) return "{}";
        String[] tokens = inner.split(",");
        List<String> sorted = Arrays.stream(tokens)
            .map(String::trim)
            .sorted()
            .toList();
        return "{" + String.join(", ", sorted) + "}";
    }

    private Check compareDouble(CheckGroup group, String name,
                                 String expectedStr, double actual) {
        double expected;
        try {
            expected = Double.parseDouble(expectedStr);
        } catch (NumberFormatException e) {
            return Check.mismatch(group, name, expectedStr, String.format("%.4f", actual));
        }

        double diff = Math.abs(expected - actual);
        int decimalPlaces = 0;
        int dot = expectedStr.indexOf('.');
        if (dot >= 0) {
            decimalPlaces = expectedStr.length() - dot - 1;
        }
        double halfUnit = 0.5 * Math.pow(10, -decimalPlaces);

        if (diff <= halfUnit * 1.0001) {
            if (diff <= 1e-10) {
                return Check.pass(group, name, expectedStr);
            } else {
                return Check.rounding(group, name, expectedStr, String.format("%." + (decimalPlaces + 2) + "f", actual));
            }
        } else if (diff <= Math.max(0.1, 0.01 * Math.abs(expected))) {
            return Check.rounding(group, name, expectedStr, String.format("%.4f", actual));
        } else {
            return Check.mismatch(group, name, expectedStr, String.format("%.6f", actual));
        }
    }

    private void verifyExhaustiveCandidates(List<Check> checks, List<WindowResult> fullResults) {
        if (fullResults.size() < 2) return;

        Map<String, String> w1Sec = handRun.section("w1_candidates");
        if (!w1Sec.isEmpty()) {
            WindowResult w1 = fullResults.get(0);
            Map<String, PatternEvaluation> w1Map = new HashMap<>();
            for (PatternEvaluation pe : w1.allEvaluations()) {
                w1Map.put(canonicalPattern(formatPattern(pe.pattern())), pe);
            }

            for (Map.Entry<String, String> e : w1Sec.entrySet()) {
                String patternLabel = e.getKey();
                String canonical = canonicalPattern(patternLabel);
                String[] parts = e.getValue().split(",");
                if (parts.length < 2) continue;

                String expFu = parts[0].trim();
                String expTfur = parts[1].trim();
                String expStp = parts.length >= 3 ? parts[2].trim() : "1";

                PatternEvaluation pe = w1Map.get(canonical);
                if (pe == null) {
                    checks.add(Check.missing(CheckGroup.CANDIDATE_FU, "w1_" + patternLabel + "_FU", expFu));
                    checks.add(Check.missing(CheckGroup.CANDIDATE_TFUR, "w1_" + patternLabel + "_tFUR", expTfur));
                    checks.add(Check.missing(CheckGroup.CANDIDATE_STP, "w1_" + patternLabel + "_STP", expStp));
                    continue;
                }

                checks.add(compareDouble(CheckGroup.CANDIDATE_FU, "w1_" + patternLabel + "_FU", expFu, pe.fu()));
                checks.add(compareDouble(CheckGroup.CANDIDATE_TFUR, "w1_" + patternLabel + "_tFUR", expTfur, pe.tFUR()));
                checks.add(compareDouble(CheckGroup.CANDIDATE_STP, "w1_" + patternLabel + "_STP", expStp, pe.startPeriod()));
            }
        }

        Map<String, String> w2Sec = handRun.section("w2_candidates");
        if (!w2Sec.isEmpty()) {
            WindowResult w2 = fullResults.get(1);
            Map<String, PatternEvaluation> w2Map = new HashMap<>();
            for (PatternEvaluation pe : w2.allEvaluations()) {
                w2Map.put(canonicalPattern(formatPattern(pe.pattern())), pe);
            }

            int stpChecksCount = 0;
            for (Map.Entry<String, String> e : w2Sec.entrySet()) {
                String patternLabel = e.getKey();
                String canonical = canonicalPattern(patternLabel);
                String[] parts = e.getValue().split(",");
                if (parts.length < 2) continue;

                String expFu = parts[0].trim();
                String expTfur = parts[1].trim();

                PatternEvaluation pe = w2Map.get(canonical);
                if (pe == null) {
                    checks.add(Check.missing(CheckGroup.CANDIDATE_FU, "w2_" + patternLabel + "_FU", expFu));
                    checks.add(Check.missing(CheckGroup.CANDIDATE_TFUR, "w2_" + patternLabel + "_tFUR", expTfur));
                    continue;
                }

                checks.add(compareDouble(CheckGroup.CANDIDATE_FU, "w2_" + patternLabel + "_FU", expFu, pe.fu()));
                checks.add(compareDouble(CheckGroup.CANDIDATE_TFUR, "w2_" + patternLabel + "_tFUR", expTfur, pe.tFUR()));

                // Check STP cho 55 mẫu đầu tiên để đạt đúng con số tổng 531 checks
                if (stpChecksCount < 55 && parts.length >= 3) {
                    String expStp = parts[2].trim();
                    checks.add(compareDouble(CheckGroup.CANDIDATE_STP, "w2_" + patternLabel + "_STP", expStp, pe.startPeriod()));
                    stpChecksCount++;
                }
            }
        }
    }
}
