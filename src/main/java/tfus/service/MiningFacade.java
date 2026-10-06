package tfus.service;

import tfus.algorithm.*;
import tfus.algorithm.pruning.*;
import tfus.algorithm.result.WindowResult;
import tfus.domain.*;
import tfus.event.*;
import tfus.fuzzy.*;
import tfus.io.SectionedFile;
import tfus.verification.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Facade: single coordination point for CLI and web consumers.
 * Hides the complexity of setting up algorithm, strategy, observer, etc.
 */
public final class MiningFacade {

    private MiningFacade() {}

    /**
     * Run the mining algorithm with the given input.
     * @param input dataset + config
     * @param listener optional event listener (use NullMiningListener.INSTANCE if not needed)
     * @return list of window results
     */
    public static List<WindowResult> run(MiningInput input, MiningListener listener) {
        MiningConfig config = input.config();
        Dataset dataset = input.dataset();

        // Create strategy objects
        MembershipFunction membership = MembershipFunctionFactory.defaultFunction();
        PruningStrategy pruning = createPruningStrategy(config.pruningStrategy());

        // Create event publisher and subscribe listener
        EventPublisher publisher = new EventPublisher();
        publisher.subscribe(listener);

        // Create and run miner
        TFUSMiner miner = new TFUSMiner(dataset, config, membership, pruning, publisher);
        return miner.processStream(dataset);
    }

    /**
     * Run with a TraceRecorder so caller can access all events.
     */
    public static RunResult runWithTrace(MiningInput input) {
        TraceRecorder recorder = new TraceRecorder();
        List<WindowResult> results = run(input, recorder);
        return new RunResult(results, recorder.events());
    }

    /**
     * Run three verification modes and compare to hand-run reference.
     * Modes: algorithm (standard), full (no pruning), paperRecursion (AT_LEAST_TWO)
     */
    public static VerificationReport verify(MiningInput input, Path handRunPath) throws Exception {
        SectionedFile handRun = SectionedFile.read(handRunPath);

        // Mode 1: standard algorithm
        MiningInput algorithmInput = withConfig(input, input.config());
        RunResult algorithmRun = runWithTrace(algorithmInput);

        // Mode 2: no pruning (full)
        MiningConfig fullConfig = MiningConfig.builder()
            .windowSize(input.config().windowSize())
            .delta(input.config().delta())
            .pruningStrategy("NONE")
            .recursionRule(MiningConfig.RecursionRule.AT_LEAST_ONE)
            .build();
        RunResult fullRun = runWithTrace(withConfig(input, fullConfig));

        // Mode 3: paper recursion (AT_LEAST_TWO)
        MiningConfig paperConfig = MiningConfig.builder()
            .windowSize(input.config().windowSize())
            .delta(input.config().delta())
            .pruningStrategy(input.config().pruningStrategy())
            .recursionRule(MiningConfig.RecursionRule.AT_LEAST_TWO)
            .build();
        RunResult paperRun = runWithTrace(withConfig(input, paperConfig));

        // Verify algorithm run against hand-run
        Verifier verifier = new Verifier(handRun);
        return verifier.verify(algorithmRun.events(), algorithmRun.results(),
            fullRun.results(), paperRun.results(),
            input.dataset().name());
    }

    private static MiningInput withConfig(MiningInput input, MiningConfig config) {
        return new MiningInput(input.dataset(), config);
    }

    private static PruningStrategy createPruningStrategy(String name) {
        return switch (name.toUpperCase()) {
            case "NONE", "NO_PRUNING" -> new NoPruning();
            default -> new RevisedUpperBoundPruning();
        };
    }

    // -------------------------------------------------------------------------
    /** Result of a mining run with trace */
    public record RunResult(List<WindowResult> results, List<MiningEvent> events) {}
}
