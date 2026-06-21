package team.bytephoria.bytechat.api.access;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.api.tag.TagRegistry;

/**
 * Public entry point for interacting with ByteChat from other plugins.
 * <p>
 * Obtain an instance either through the static accessor — {@link #getAPI()} or
 * {@link ByteChatProvider#getInstance()} — or through the Bukkit {@code ServicesManager}:
 * <pre>{@code
 * ByteChat api = ByteChat.getAPI();
 * api.tagRegistry().register(myTag);
 * }</pre>
 *
 * @see ByteChatProvider
 * @see TagRegistry
 */
public interface ByteChat {

    /**
     * Returns the active ByteChat API instance.
     *
     * @return the API instance
     * @throws IllegalStateException if ByteChat is not currently enabled
     * @see ByteChatProvider#getInstance()
     */
    @Contract(pure = true)
    static @NotNull ByteChat getAPI() {
        return ByteChatProvider.getInstance();
    }


    /**
     * The registry of inline chat tags. Register, remove, or inspect tags here.
     *
     * @return the live tag registry
     */
    @NotNull TagRegistry tagRegistry();

    /**
     * The running ByteChat plugin version (e.g. {@code 2.0.0}).
     *
     * @return the plugin version string
     */
    @NotNull String version();
}
