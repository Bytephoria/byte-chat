package team.bytephoria.bytechat.tags;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.tag.Tag;
import team.bytephoria.bytechat.util.ColorParser;

/**
 * Convenience base class for {@link Tag} implementations.
 * <p>
 * Stores the {@code key} and optional {@code permission}, leaving subclasses to
 * implement only {@link #render(team.bytephoria.bytechat.api.tag.TagContext)}.
 */
public abstract class AbstractTags implements Tag {

    private final @NotNull String key;
    private final @Nullable String permission;

    protected AbstractTags(final @NotNull String key) {
        this(key, null);
    }

    protected AbstractTags(final @NotNull String key, final @Nullable String permission) {
        this.key = key;
        this.permission = permission;
    }

    @Override
    public @NotNull String key() {
        return this.key;
    }

    @Override
    public @Nullable String permission() {
        return this.permission;
    }

    /**
     * Converts a configured color string — named, hex ({@code #RRGGBB}), or
     * {@code rgb(r, g, b)} — into a {@link TextColor}, defaulting to white if it
     * cannot be resolved.
     */
    protected static @NotNull TextColor parseColor(final @NotNull String color) {
        return ColorParser.parse(color, NamedTextColor.WHITE);
    }
}
