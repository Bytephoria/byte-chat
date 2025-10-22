package team.bytephoria.bytechat.manager;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.Prioritizable;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.registry.ChatFormatRegistry;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class ChatManager {

    private static final Comparator<ChatFormat> PRIORITY_COMPARATOR =
            Comparator.comparingInt(Prioritizable::priority);

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
     * Returns the default chat format configured in config.yml, if it exists.
     */
    public @NotNull Optional<ChatFormat> defaultChatFormat() {
        final String defaultFormatId = this.chatConfiguration.chat().defaultFormat();
        return Optional.ofNullable(this.chatFormatRegistry.get(defaultFormatId));
    }

    /**
     * Resolves the most appropriate chat format for the given permissible.
     * <p>Fast path: if only one format exists, it performs a direct permission check.</p>
     * <p>Otherwise, it finds all accessible formats, selects the one with the highest
     * priority, and falls back to the default format if none apply.</p>
     *
     * @param permissible the permissible entity (usually a Player)
     * @return the resolved chat format, or {@code null} if none are applicable
     */
    public @Nullable ChatFormat search(final @NotNull Permissible permissible) {
        // Fast path: only one format
        if (this.chatFormatRegistry.size() == 1) {
            final ChatFormat singleFormat = this.chatFormatRegistry.all().values().iterator().next();
            final String permission = singleFormat.permission();

            if (permission == null || permission.isEmpty() || permissible.hasPermission(permission)) {
                return singleFormat;
            }

            final String defaultFormatId = this.chatConfiguration.chat().defaultFormat();
            return defaultFormatId.equalsIgnoreCase(singleFormat.id()) ? singleFormat : null;
        }

        // Normal path
        return this.resolveBestFor(permissible)
                .or(this::defaultChatFormat)
                .orElse(null);
    }

    /**
     * Returns a sequential Stream of all formats the permissible can use.
     * This stream is read-only and should not be reused once consumed.
     */
    public @NotNull Stream<ChatFormat> matchingFormats(final @NotNull Permissible permissible) {
        return this.chatFormatRegistry.all().values().stream()
                .filter(format -> {
                    final String permission = format.permission();
                    return permission == null || permission.isEmpty() || permissible.hasPermission(permission);
                });
    }

    /**
     * Returns the format with the highest priority from the provided stream.
     */
    public @NotNull Optional<ChatFormat> highestPriority(final @NotNull Stream<ChatFormat> formats) {
        return formats.max(PRIORITY_COMPARATOR);
    }

    /**
     * Finds all accessible formats for the permissible and returns the one
     * with the highest priority. Does not apply any fallback.
     */
    public @NotNull Optional<ChatFormat> resolveBestFor(final @NotNull Permissible permissible) {
        return this.highestPriority(this.matchingFormats(permissible));
    }
}
