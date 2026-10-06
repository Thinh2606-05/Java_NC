package tfus.io;

import tfus.verification.Check;
import tfus.verification.CheckGroup;
import tfus.verification.VerificationReport;

import java.io.PrintStream;
import java.util.List;

/**
 * In báo cáo kiểm thử và đối chiếu kết quả dạng bảng text có hỗ trợ mã màu ANSI XANH/ĐỎ.
 */
public final class TextReportWriter {

    // Mã màu ANSI cho terminal
    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE   = "\u001B[34m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_BOLD   = "\u001B[1m";

    private TextReportWriter() {}

    public static void printReport(VerificationReport report, PrintStream out) {
        boolean isColor = (out == System.out || out == System.err);

        out.println("================================================================================");
        out.println("                         TFUS VERIFICATION REPORT                               ");
        out.println("================================================================================");
        out.printf("Dataset: %s%n", report.datasetName());
        
        String summaryColor = report.isFullyPassing() ? ANSI_GREEN : ANSI_RED;
        if (isColor) out.print(summaryColor + ANSI_BOLD);
        out.printf("Total checks: %d | PASS: %d | ROUNDING: %d | MISMATCH: %d | MISSING: %d | INFO: %d%n",
                report.checks().size(), report.passCount(), report.roundingCount(),
                report.mismatchCount(), report.missingCount(), report.infoCount());
        if (isColor) out.print(ANSI_RESET);
        out.println("--------------------------------------------------------------------------------");

        for (CheckGroup group : CheckGroup.values()) {
            List<Check> list = report.byGroup(group);
            if (list.isEmpty()) continue;

            if (isColor) out.print(ANSI_CYAN + ANSI_BOLD);
            out.printf("[%s] (%d checks)%n", group.description(), list.size());
            if (isColor) out.print(ANSI_RESET);

            for (Check c : list) {
                String color = switch (c.status()) {
                    case PASS -> isColor ? ANSI_GREEN : "";
                    case ROUNDING -> isColor ? ANSI_YELLOW : "";
                    case MISMATCH, MISSING -> isColor ? ANSI_RED : "";
                    case INFO -> isColor ? ANSI_BLUE : "";
                };
                String reset = isColor ? ANSI_RESET : "";

                String flag = switch (c.status()) {
                    case PASS -> "[PASS]    ";
                    case ROUNDING -> "[ROUNDING]";
                    case MISMATCH -> "[MISMATCH]";
                    case MISSING ->  "[MISSING] ";
                    case INFO ->     "[INFO]    ";
                };

                if (c.status() == Check.Status.PASS) {
                    out.printf("  %s%s%s %-32s = %s%n", color, flag, reset, c.name(), c.actual());
                } else if (c.status() == Check.Status.ROUNDING) {
                    out.printf("  %s%s%s %-32s expected=%s, actual=%s (%s)%n", color, flag, reset, c.name(), c.expected(), c.actual(), c.note());
                } else {
                    out.printf("  %s%s%s %-32s expected=%s, actual=%s%n", color, flag, reset, c.name(), c.expected(), c.actual());
                }
            }
            out.println();
        }

        out.println("================================================================================");
        if (report.isFullyPassing()) {
            if (isColor) out.print(ANSI_GREEN + ANSI_BOLD);
            out.println("RESULT: ALL CHECKS PASSED (OR WITHIN TOLERANCE) - 100% MATCH!");
            if (isColor) out.print(ANSI_RESET);
        } else {
            if (isColor) out.print(ANSI_RED + ANSI_BOLD);
            out.println("RESULT: VERIFICATION FAILED - FOUND MISMATCHES OR MISSING ITEMS.");
            if (isColor) out.print(ANSI_RESET);
        }
        out.println("================================================================================");
    }
}
