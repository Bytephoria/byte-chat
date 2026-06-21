package team.bytephoria.bytechat.api.event;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.chat.Format;

/**
 * Fired by ByteChat after a player's chat message has been fully rendered, exposing
 * the final {@link Component} that will be shown to viewers.
 * <p>
 * Where {@link ByteChatMessageEvent} runs <em>before</em> rendering (and can veto the
 * message), this event runs <em>after</em> rendering: use it to observe the finished
 * chat line — for example to forward the formatted message to Discord or to log it —
 * or to apply a final transformation via {@link #setComponent(Component)}.
 * <p>
 * <b>Threading:</b> this is an asynchronous event fired from the chat render thread.
 * Listeners must be thread-safe and must not call un-synchronized Bukkit API.
 */
public final class ByteChatPostRenderEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Format format;
    private Component component;

    public ByteChatPostRenderEvent(
            final @NotNull Player player,
            final @Nullable Format format,
            final @NotNull Component component
    ) {
        super(true);
        this.player = player;
        this.format = format;
        this.component = component;
    }

    /**
     * The player who sent the message.
     *
     * @return the sender
     */
    public @NotNull Player player() {
        return this.player;
    }

    /**
     * The format that was used to render the message, or {@code null} if none applied.
     *
     * @return the format, or {@code null}
     */
    public @Nullable Format format() {
        return this.format;
    }

    /**
     * The final rendered chat line that will be sent to viewers.
     *
     * @return the rendered component
     */
    public @NotNull Component component() {
        return this.component;
    }

    /**
     * Replaces the final rendered chat line. The new component is what viewers will
     * actually see.
     *
     * @param component the component to display instead
     */
    public void component(final @NotNull Component component) {
        this.component = component;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
