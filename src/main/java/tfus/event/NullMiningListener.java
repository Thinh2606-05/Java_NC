package tfus.event;

/**
 * Null Object: ignores all events. Used when no listener is needed.
 */
public final class NullMiningListener implements MiningListener {
    public static final NullMiningListener INSTANCE = new NullMiningListener();

    @Override
    public void onEvent(MiningEvent event) {
        // intentionally empty
    }
}
