package tfus.event;

/**
 * Observer interface for mining events.
 */
public interface MiningListener {
    void onEvent(MiningEvent event);
}
