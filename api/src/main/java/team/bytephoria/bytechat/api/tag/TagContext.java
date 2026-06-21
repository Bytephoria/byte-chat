package team.bytephoria.bytechat.api.tag;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Context handed to {@link Tag#render(TagContext)} for a single tag occurrence.
 *
 * @param sender the player who wrote the message containing the tag
 * @param key    the exact key matched inside the brackets (e.g. {@code item})
 */
public record TagContext(
        @NotNull Player sender,
        @NotNull String key
) {

    public TagContext {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(key);
    }

}
