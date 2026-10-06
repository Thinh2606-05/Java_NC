package tfus.algorithm.structure;

import tfus.domain.FuzzyItem;
import java.util.*;

/**
 * Global store of all TFUS-lists, indexed by FuzzyItem.
 * Maintains insertion order for determinism.
 */
public final class GlobalListStore {
    private final Map<FuzzyItem, TFUSList> lists = new LinkedHashMap<>();

    public TFUSList getOrCreate(FuzzyItem fi) {
        return lists.computeIfAbsent(fi, TFUSList::new);
    }

    public TFUSList get(FuzzyItem fi) {
        return lists.get(fi);
    }

    public boolean contains(FuzzyItem fi) {
        return lists.containsKey(fi);
    }

    public Collection<TFUSList> all() {
        return Collections.unmodifiableCollection(lists.values());
    }

    public Set<FuzzyItem> keys() {
        return Collections.unmodifiableSet(lists.keySet());
    }

    /** Remove lists that have no entries (after batch removal). */
    public List<FuzzyItem> removeEmptyLists() {
        List<FuzzyItem> removed = new ArrayList<>();
        Iterator<Map.Entry<FuzzyItem, TFUSList>> it = lists.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<FuzzyItem, TFUSList> e = it.next();
            if (e.getValue().isEmpty()) {
                removed.add(e.getKey());
                it.remove();
            }
        }
        return removed;
    }

    public int size() { return lists.size(); }

    public void clear() { lists.clear(); }
}
