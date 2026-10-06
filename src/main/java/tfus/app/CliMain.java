package tfus.app;

import tfus.domain.MiningInput;
import tfus.domain.Pattern;
import tfus.algorithm.result.WindowResult;
import tfus.event.ConsoleTraceLogger;
import tfus.io.TextDatasetReader;
import tfus.io.TextReportWriter;
import tfus.service.MiningFacade;
import tfus.service.PresetCatalog;
import tfus.verification.VerificationReport;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Entry point chạy giao diện dòng lệnh (CLI).
 */
public final class CliMain {

    public static void main(String[] args) {
        String datasetId = "lab1";
        boolean verify = false;
        boolean verbose = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--dataset", "-d" -> {
                    if (i + 1 < args.length) datasetId = args[++i];
                }
                case "--verify", "-v" -> verify = true;
                case "--trace", "-t" -> verbose = true;
                case "--help", "-h" -> {
                    printUsage();
                    return;
                }
            }
        }

        try {
            Path dataDir = Path.of("data");
            PresetCatalog catalog = new PresetCatalog(dataDir);
            Path datasetPath = catalog.datasetPath(datasetId);

            if (!Files.exists(datasetPath)) {
                System.err.printf("Error: Dataset file not found: %s%n", datasetPath);
                System.exit(1);
            }

            System.out.printf("Loading dataset: %s...%n", datasetId);
            TextDatasetReader reader = new TextDatasetReader(dataDir);
            MiningInput input = reader.read(datasetPath.getFileName().toString());

            System.out.println("--------------------------------------------------");
            System.out.printf("Transactions : %d%n", input.dataset().transactions().size());
            System.out.printf("Batches      : %d%n", input.dataset().batches().size());
            System.out.printf("Window size  : %d%n", input.config().windowSize());
            System.out.printf("Delta (th.)  : %.2f%n", input.config().delta());
            System.out.printf("Pruning      : %s%n", input.config().pruningStrategy());
            System.out.println("--------------------------------------------------");

            if (verify) {
                Path handRunPath = catalog.handRunPath(datasetId);
                if (!Files.exists(handRunPath)) {
                    System.err.printf("Error: Hand-run verification file not found: %s%n", handRunPath);
                    System.exit(1);
                }
                System.out.println("Running 3 verification modes: standard algorithm, full (no pruning), paperRecursion...");
                VerificationReport report = MiningFacade.verify(input, handRunPath);

                TextReportWriter.printReport(report, System.out);

                // Save report to output folder
                Path outputDir = Path.of("output");
                Files.createDirectories(outputDir);
                Path reportFile = outputDir.resolve(datasetId + "_verification_report.txt");
                try (PrintStream ps = new PrintStream(new FileOutputStream(reportFile.toFile()))) {
                    TextReportWriter.printReport(report, ps);
                }
                System.out.printf("Report saved to: %s%n", reportFile.toAbsolutePath());
            } else {
                System.out.println("Running TFUS stream mining...");
                var listener = verbose ? new ConsoleTraceLogger() : tfus.event.NullMiningListener.INSTANCE;
                List<WindowResult> results = MiningFacade.run(input, listener);

                System.out.println("\n================= MINING RESULTS =================");
                for (WindowResult wr : results) {
                    System.out.printf("%n[WINDOW W%d] (took %d ms)%n", wr.windowNumber(), wr.elapsedMs());
                    System.out.printf("Discovered %d patterns (tFUR >= %.2f):%n", wr.patterns().size(), input.config().delta());
                    for (int idx = 0; idx < wr.patterns().size(); idx++) {
                        Pattern p = wr.patterns().get(idx);
                        System.out.printf("  %2d. %-30s | tFUR = %.4f | PS = P%d%n",
                                (idx + 1), p.label(), p.tFUR(), p.startPeriod());
                    }
                }
                System.out.println("==================================================");
            }

        } catch (Exception e) {
            System.err.println("Execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("TFUS - Temporal Fuzzy Utility pattern mining under moving Sliding windows");
        System.out.println("Usage: java -cp target/classes tfus.app.CliMain [options]");
        System.out.println("Options:");
        System.out.println("  -d, --dataset <id>   Dataset preset ID (default: lab1)");
        System.out.println("  -v, --verify         Run automated verification against hand-run file");
        System.out.println("  -t, --trace          Print step-by-step trace events to console");
        System.out.println("  -h, --help           Show this help message");
    }
}
