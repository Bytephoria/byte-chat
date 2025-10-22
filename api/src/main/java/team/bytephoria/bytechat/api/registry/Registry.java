package team.bytephoria.bytechat.api.registry;

import java.util.Map;

/**
 * Represents a generic registry for managing key-value pairs.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface Registry<K, V> {

    /**
     * Registers a new entry in the registry.
     *
     * @param key   the unique key
     * @param value the associated value
     */
    void register(final K key, final V value);

    /**
     * Retrieves an entry from the registry.
     *
     * @param key the key to look up
     * @return the value, or {@code null} if not found
     */
    V get(final K key);

    /**
     * Checks if an entry exists for the given key.
     *
     * @param key the key to check
     * @return true if the key exists
     */
    boolean contains(final K key);

    /**
     * Returns an unmodifiable view of all registered entries.
     *
     * @return all registry entries
     */
    Map<K, V> all();

    /**
     * Clears all registered entries.
     */
    void clearAll();
}