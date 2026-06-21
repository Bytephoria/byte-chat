package team.bytephoria.bytechat.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses color strings from configuration into Adventure {@link TextColor}s.
 * <p>
 * Supported formats:
 * <ul>
 *     <li>Named colors — {@code GRAY}, {@code aqua}, {@code light_purple} … (case-insensitive)</li>
 *     <li>Hex — {@code #RRGGBB} or {@code RRGGBB}</li>
 *     <li>RGB — {@code rgb(r, g, b)} with each channel in {@code 0-255}</li>
 * </ul>
 * Unrecognised values fall back to the provided default.
 */
public final class ColorParser {

    private static final Pattern RGB_PATTERN = Pattern.compile(
            "rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)",
            Pattern.CASE_INSENSITIVE
    );

    private ColorParser() {
        throw new NonInstantiableClassException();
    }

    /**
     * Parses the given color string, returning {@code fallback} when it cannot be resolved.
     *
     * @param input the color string (named, hex, or rgb)
     * @param fallback the color to use when {@code input} is not recognized
     * @return the parsed color, never {@code null}
     */
    public static @NotNull TextColor parse(final @NotNull String input, final @NotNull TextColor fallback) {
        final String value = input.trim();
        if (value.isEmpty()) {
            return fallback;
        }

        // Hex: #RRGGBB or RRGGBB
        if (value.charAt(0) == '#') {
            final TextColor hex = TextColor.fromHexString(value);
            return hex != null ? hex : fallback;
        }

        // rgb(r, g, b)
        final Matcher rgbMatcher = RGB_PATTERN.matcher(value);
        if (rgbMatcher.matches()) {
            final int red = clamp(Integer.parseInt(rgbMatcher.group(1)));
            final int green = clamp(Integer.parseInt(rgbMatcher.group(2)));
            final int blue = clamp(Integer.parseInt(rgbMatcher.group(3)));
            return TextColor.color(red, green, blue);
        }

        // Bare 6-digit hex without leading '#'
        if (value.length() == 6 && value.chars().allMatch(ColorParser::isHexDigit)) {
            final TextColor hex = TextColor.fromHexString("#" + value);
            if (hex != null) {
                return hex;
            }
        }

        // Named color
        final NamedTextColor named = NamedTextColor.NAMES.value(value.toLowerCase());
        return named != null ? named : fallback;
    }

    /**
     * Convenience overload using {@link NamedTextColor#WHITE} as the fallback.
     */
    public static @NotNull TextColor parse(final @NotNull String input) {
        return parse(input, NamedTextColor.WHITE);
    }

    private static int clamp(final int channel) {
        return Math.clamp(channel, 0, 255);
    }

    private static boolean isHexDigit(final int codePoint) {
        return (codePoint >= '0' && codePoint <= '9') || (codePoint >= 'a' && codePoint <= 'f') || (codePoint >= 'A' && codePoint <= 'F');
    }

}
