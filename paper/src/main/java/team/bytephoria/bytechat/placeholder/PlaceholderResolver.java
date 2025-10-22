package team.bytephoria.bytechat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.util.StringUtil;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

import java.util.function.Function;

/**
 * Replaces all supported placeholders in a chat element string.
 * Includes built-in placeholders ({player}, {message})
 * and PlaceholderAPI placeholders (if installed).
 */
public final class PlaceholderResolver {

    private static final boolean PAPI_ENABLED =
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

    private PlaceholderResolver() {
        throw new NonInstantiableClassException();
    }

    public static @NotNull Function<String, String> create(
            final @NotNull Player player,
            final @NotNull String message
    ) {
        return input -> {
            String processed = StringUtil.replace(input,
                    "{player}", player.getName(),
                    "{message}", message
            );

            if (PAPI_ENABLED) {
                processed = PlaceholderAPI.setPlaceholders(player, processed);
            }

            return processed;
        };
    }
}