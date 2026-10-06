package tfus.event;

import java.util.*;

/**
 * Records all mining events for later inspection (e.g., verification).
 * Null Object for when listeners aren't needed: use NullMiningListener.
 */
public final class TraceRecorder implements MiningListener {
    private final List<MiningEvent> events = new ArrayList<>();

    @Override
    public void onEvent(MiningEvent event) {
        events.add(event);
    }

    public List<MiningEvent> events() {
        return Collections.unmodifiableList(events);
    }

    public void clear() { events.clear(); }

    /** Find first event of a given type */
    @SuppressWarnings("unchecked")
    public <T extends MiningEvent> Optional<T> first(Class<T> type) {
        return events.stream()
            .filter(type::isInstance)
            .map(e -> (T) e)
            .findFirst();
    }

    /** Find all events of a given type */
    @SuppressWarnings("unchecked")
    public <T extends MiningEvent> List<T> all(Class<T> type) {
        return events.stream()
            .filter(type::isInstance)
            .map(e -> (T) e)
            .toList();
    }
}
