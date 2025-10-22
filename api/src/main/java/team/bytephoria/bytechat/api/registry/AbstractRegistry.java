package team.bytephoria.bytechat.api.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class providing a simple implementation for {@link Registry}.
 * Subclasses can extend this to define domain-specific registries.
 */
public abstract class AbstractRegistry<K, V> implements Registry<K, V> {

    private final Map<K, V> entries = new HashMap<>();

    @Override
    public void register(final K key, final V value) {
        this.entries.put(key, value);
    }

    @Override
    public V get(final K key) {
        return this.entries.get(key);
    }

    @Override
    public boolean contains(final K key) {
        return this.entries.containsKey(key);
    }

    @Override
    public Map<K, V> all() {
        return Collections.unmodifiableMap(this.entries);
    }

    @Override
    public void clearAll() {
        this.entries.clear();
    }

    /**
     * Returns the number of entries currently registered.
     */
    public int size() {
        return this.entries.size();
    }
}