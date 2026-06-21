package team.bytephoria.bytechat.api.access;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Static accessor for the {@link ByteChat} API.
 * <p>
 * The implementation is bound by the plugin during {@code onEnable} and cleared
 * during {@code onDisable}. Callers must therefore only access it while ByteChat
 * is enabled (for example, from within their own {@code onEnable} after declaring
 * a dependency on ByteChat, or lazily at runtime).
 * <pre>{@code
 * ByteChatProvider.get().tagRegistry().register(myTag);
 * }</pre>
 */
public final class ByteChatProvider {

    private static ByteChat instance;

    private ByteChatProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Returns the active API instance.
     *
     * @return the API instance
     * @throws IllegalStateException if ByteChat is not enabled
     */
    public static @NotNull ByteChat getInstance() {
        return Objects.requireNonNull(instance, "ByteChat API is not available; is the plugin enabled?");
    }

    /**
     * Returns whether the API is currently bound and available.
     *
     * @return {@code true} if {@link #getInstance()} would succeed
     */
    public static boolean hasInstance() {
        return instance != null;
    }

    /**
     * Binds the API implementation. Intended for internal use by the plugin only.
     *
     * @param instance the implementation, or {@code null} to unbind
     */
    public static void setInstance(final @NotNull ByteChat instance) {
        final ByteChat notNullInstance = Objects.requireNonNull(instance, "ByteChatProvider instance must not be null.");
        if (ByteChatProvider.instance != null) {
            throw new IllegalStateException("ByteChatProvider instance is already set.");
        }

        ByteChatProvider.instance = notNullInstance;
    }

    /**
     * Resets the stored API instance.
     * <p>
     * Intended for internal/testing purposes only.
     */
    @ApiStatus.Internal
    public static void reset() {
        ByteChatProvider.instance = null;
    }

}
