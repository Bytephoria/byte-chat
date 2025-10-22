package team.bytephoria.bytechat.manager;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.Prioritizable;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.registry.ChatFormatRegistry;

import java.util.Comparator;
import java.util.Optional;

public final class ChatManager {

    private final ChatFormatRegistry chatFormatRegistry;
    private final ChatConfiguration chatConfiguration;

    public ChatManager(
            final @NotNull ChatFormatRegistry chatFormatRegistry,
            final @NotNull ChatConfiguration chatConfiguration
    ) {
        this.chatFormatRegistry = chatFormatRegistry;
        this.chatConfiguration = chatConfiguration;
    }

    /**
     * Returns the default chat format configured in chat.yml, if it exists.
     */
    public @NotNull Optional<ChatFormat> defaultChatFormat() {
        final String defaultId = this.chatConfiguration.chat().defaultFormat();
        return Optional.ofNullable(this.chatFormatRegistry.get(defaultId));
    }

    /**
     * Finds the most appropriate chat format for the given permissible.
     * <p>
     * Fast-path optimization:
     * If there is only one format registered, it performs a direct permission check
     * instead of streaming through the registry.
     * <p>
     * Normal flow:
     * - Returns the highest priority format the permissible has access to.
     * - Falls back to the default format if none match.
     *
     * @param permissible the permissible entity (usually a Player)
     * @return the most suitable ChatFormat or null if none are applicable
     */
    public @Nullable ChatFormat search(final @NotNull Permissible permissible) {
        // Fast path: only one chat format registered
        if (this.chatFormatRegistry.size() == 1) {
            final ChatFormat singleFormat = this.chatFormatRegistry.all().values().iterator().next();
            final String permission = singleFormat.permission();

            // If the format requires no permission or the permissible has it, return it
            if (permission == null || permission.isEmpty() || permissible.hasPermission(permission)) {
                return singleFormat;
            }

            // If the single format is the configured default, also return it
            final String defaultFormatId = this.chatConfiguration.chat().defaultFormat();
            if (defaultFormatId.equalsIgnoreCase(singleFormat.id())) {
                return singleFormat;
            }

            // Otherwise, no accessible format
            return null;
        }

        // Normal path: search by permission, fallback to default
        return this.searchFromPermission(permissible)
                .or(this::defaultChatFormat)
                .orElse(null);
    }

    /**
     * Searches for the highest priority format that the permissible has access to.
     * Formats without a permission node are considered public.
     *
     * @param permissible the permissible entity to check
     * @return an Optional containing the format if found
     */
    public @NotNull Optional<ChatFormat> searchFromPermission(final @NotNull Permissible permissible) {
        return this.chatFormatRegistry.all().values().stream()
                .filter(format -> {
                    final String permission = format.permission();
                    return permission == null || permission.isEmpty() || permissible.hasPermission(permission);
                })
                .max(Comparator.comparingInt(Prioritizable::priority));
    }

}
