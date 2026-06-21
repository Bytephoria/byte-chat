package team.bytephoria.bytechat.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.chat.Format;

/**
 * Fired by ByteChat when a player sends a chat message, after the applicable
 * {@link Format} has been resolved but before the message is rendered and
 * dispatched.
 * <p>
 * This event lets addons observe or veto chat messages — for example a chat
 * cooldown plugin can {@link #setCancelled(boolean) cancel} the event, while a
 * Discord bridge can read the sender, format, and raw message to forward it.
 * <p>
 * <b>Threading:</b> this is an asynchronous event fired from the chat thread.
 * Listeners must be thread-safe and must not call un-synchronized Bukkit API.
 * Cancelling the event prevents the message from being broadcast.
 */
public final class ByteChatMessageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Format format;
    private final String message;
    private boolean cancelled;

    public ByteChatMessageEvent(
            final @NotNull Player player,
            final @Nullable Format format,
            final @NotNull String message
    ) {
        super(true);
        this.player = player;
        this.format = format;
        this.message = message;
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
     * The format resolved for the sender, or {@code null} when no format applied
     * (in which case ByteChat leaves rendering to the server default).
     *
     * @return the resolved format, or {@code null}
     */
    public @Nullable Format format() {
        return this.format;
    }

    /**
     * The raw message text the player sent, before any ByteChat processing.
     *
     * @return the raw message
     */
    public @NotNull String message() {
        return this.message;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}
