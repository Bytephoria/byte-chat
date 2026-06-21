package team.bytephoria.bytechat.tags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import team.bytephoria.bytechat.api.tag.Tag;
import team.bytephoria.bytechat.api.tag.TagRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Default in-memory {@link TagRegistry}. Keys are normalised to lower case so that
 * lookups are case-insensitive, as documented by the interface.
 */
public final class DefaultTagRegistry implements TagRegistry {

    private final Map<String, Tag> tags = new HashMap<>();

    @Override
    public @NotNull @UnmodifiableView Map<String, Tag> tags() {
        return Collections.unmodifiableMap(this.tags);
    }

    @Override
    public @Nullable Tag tag(final @NotNull String key) {
        return this.tags.get(normalize(key));
    }

    @Override
    public boolean contains(final @NotNull String key) {
        return this.tags.containsKey(normalize(key));
    }

    @Override
    public void register(final @NotNull Tag tag) {
        final String key = normalize(tag.key());
        if (this.tags.containsKey(key)) {
            throw new IllegalStateException("A tag is already registered under the key: " + key);
        }

        this.tags.put(key, tag);
    }

    @Override
    public @Nullable Tag unregister(final @NotNull String key) {
        return this.tags.remove(normalize(key));
    }

    private static @NotNull String normalize(final @NotNull String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
