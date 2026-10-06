package tfus.event;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Publisher for mining events. Maintains a list of listeners and
 * broadcasts events to all registered listeners.
 * Implements the Observable side of the Observer pattern.
 */
public final class EventPublisher {
    private final List<MiningListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(MiningListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(MiningListener listener) {
        listeners.remove(listener);
    }

    public void publish(MiningEvent event) {
        for (MiningListener l : listeners) {
            l.onEvent(event);
        }
    }

    public void clearListeners() {
        listeners.clear();
    }
}
