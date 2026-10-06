package tfus.domain;

/**
 * Combines dataset + mining configuration as input to the mining algorithm.
 */
public record MiningInput(Dataset dataset, MiningConfig config) {
    public MiningInput {
        if (dataset == null) throw new IllegalArgumentException("dataset must not be null");
        if (config == null) throw new IllegalArgumentException("config must not be null");
    }
}
