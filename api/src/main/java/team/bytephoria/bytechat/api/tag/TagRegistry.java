package team.bytephoria.bytechat.api.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

/**
 * Registry of inline chat {@link Tag tags}.
 * <p>
 * Tags are keyed by {@link Tag#key()} and matched case-insensitively, so
 * {@code [Item]} and {@code [item]} resolve to the same tag. Implementations are
 * expected to normalise keys accordingly.
 * <p>
 * Obtain the live registry from {@code ByteChat.getAPI().tagRegistry()} and
 * register your own tags during your plugin's startup.
 */
public interface TagRegistry {

    /**
     * An unmodifiable view of all registered tags, keyed by their (normalised) key.
     *
     * @return the registered tags
     */
    @NotNull
    @UnmodifiableView
    Map<String, Tag> tags();

    /**
     * Returns the tag registered under the given key, if any.
     *
     * @param key the tag key (case-insensitive)
     * @return the matching tag, or {@code null} if none is registered
     */
    @Nullable Tag tag(final @NotNull String key);

    /**
     * Returns whether a tag is registered under the given key.
     *
     * @param key the tag key (case-insensitive)
     * @return {@code true} if a tag is registered for the key
     */
    boolean contains(final @NotNull String key);

    /**
     * Registers a tag, keyed by its {@link Tag#key()}.
     *
     * @param tag the tag to register
     * @throws IllegalStateException if a tag is already registered under the same key
     */
    void register(final @NotNull Tag tag);

    /**
     * Removes the tag registered under the given key.
     *
     * @param key the tag key (case-insensitive)
     * @return the removed tag, or {@code null} if none was registered
     */
    @Nullable Tag unregister(final @NotNull String key);
}
