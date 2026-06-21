package team.bytephoria.bytechat.api.tag;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A chat tag — an inline token a player can write in chat to inject a rich
 * {@link Component} into their message.
 * <p>
 * A tag is activated by writing {@code [key]} in chat, where {@code key} is the
 * value returned by {@link #key()}. When the renderer encounters such a token it
 * looks the tag up in the {@link TagRegistry}, checks {@link #permission()}, and
 * calls {@link #render(TagContext)} to obtain the component to splice into the
 * message.
 * <p>
 * Because {@link #render(TagContext)} returns a full Adventure {@link Component},
 * a tag has complete control over its output: it may attach hover tooltips
 * (including {@code show_item}), click callbacks with use-limits, gradients, and
 * any other Adventure feature. Built-in tags ({@code [item]}, {@code [inv]},
 * {@code [armor]}, {@code [ec]}) are themselves implemented against this contract.
 * <p>
 * Third-party plugins register their own tags through
 * {@code ByteChat.get().tagRegistry().register(tag)}.
 */
public interface Tag {

    /**
     * The identifier that activates this tag in chat as {@code [key]}.
     * <p>Keys are matched case-insensitively and must be unique within a registry.
     *
     * @return the tag key
     */
    @NotNull String key();

    /**
     * The permission required to use this tag, or {@code null} / empty to allow
     * everyone. When set, players lacking the permission have the token left
     * untouched (rendered as literal text) instead of expanded.
     *
     * @return the required permission, or {@code null} for none
     */
    default @Nullable String permission() {
        return null;
    }

    /**
     * Builds the component to inject into chat for a single occurrence of this tag.
     *
     * @param context the render context (sender and the raw key that was matched)
     * @return the component to splice into the message
     */
    @NotNull Component render(final @NotNull TagContext context);
}
