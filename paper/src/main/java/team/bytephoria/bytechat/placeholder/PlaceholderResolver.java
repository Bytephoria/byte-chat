package team.bytephoria.bytechat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.util.StringUtil;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

import java.util.function.Function;

/**
 * Handles placeholder replacement for chat elements.
 * <p>
 * This resolver supports:
 * <ul>
 *   <li>Internal placeholders: <code>{player}</code> and <code>{message}</code></li>
 *   <li>External placeholders via PlaceholderAPI (if installed)</li>
 * </ul>
 * <p>
 * PlaceholderAPI replacements are applied <strong>only</strong> to the format template
 * (server-controlled input), not to player messages — ensuring that user input cannot
 * execute arbitrary PAPI placeholders.
 */
public final class PlaceholderResolver {

    /**
     * Whether PlaceholderAPI is present and enabled on the server.
     * <p>
     * Checked once at class load time to avoid repeated lookups.
     */
    private static final boolean PAPI_ENABLED =
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    /**
     * Private constructor to prevent instantiation.
     */
    private PlaceholderResolver() {
        throw new NonInstantiableClassException();
    }

    /**
     * Creates a placeholder replacement function for a specific player and message context.
     * <p>
     * The returned {@link Function} takes a chat format string and replaces:
     * <ul>
     *     <li>PAPI placeholders (if PlaceholderAPI is available)</li>
     *     <li>Internal placeholders:
     *         <ul>
     *             <li><code>{player}</code> → player's name</li>
     *             <li><code>{message}</code> → player's chat message</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param player  the player whose placeholders will be resolved
     * @param message the raw message sent by the player
     * @return a function that replaces placeholders in any given input string
     */
    public static @NotNull Function<String, String> create(
            final @NotNull Player player,
            final @NotNull String message
    ) {
        return input -> {
            // Apply PlaceholderAPI only to the format string (safe, server-controlled)
            final String inputParsed = PAPI_ENABLED
                    ? PlaceholderAPI.setPlaceholders(player, input)
                    : input;

            // Replace internal placeholders with player-specific data
            return StringUtil.replace(inputParsed,
                    "{player}", player.getName(),
                    "{message}", message
            );
        };
    }
}
