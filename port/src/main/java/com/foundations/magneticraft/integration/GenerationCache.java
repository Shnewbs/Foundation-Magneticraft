package com.foundations.magneticraft.integration;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/** Resource managers are replaced on reload; script revisions invalidate their owner-layer snapshot. */
public final class GenerationCache<K, V> {
    private record Entry<V>(long revision, V value) {}
    private final Map<K, Entry<V>> entries = new WeakHashMap<>();
    public synchronized V get(K manager, long revision, Supplier<V> loader) {
        Entry<V> entry = entries.get(manager);
        if (entry == null || entry.revision() != revision) {
            entry = new Entry<>(revision, loader.get());
            entries.put(manager, entry);
        }
        return entry.value();
    }
    public synchronized void clear() { entries.clear(); }
}
