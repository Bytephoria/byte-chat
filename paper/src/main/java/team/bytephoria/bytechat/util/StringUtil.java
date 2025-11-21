package team.bytephoria.bytechat.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

/**
 * Utility class for efficient string replacement operations.
 * <p>
 * Provides optimized methods for replacing multiple placeholder-value pairs
 * without using regular expressions or creating unnecessary intermediate objects.
 */
public final class StringUtil {

    private StringUtil() {
        throw new NonInstantiableClassException();
    }

    /**
     * Splits a string by a single character without using regex.
     * <p>
     * This method performs two linear passes:
     * one to count parts and one to extract them.
     * It avoids intermediate allocations and guarantees
     * minimal overhead compared to {@link String#split(String)}.
     *
     * @param message   the input text to split
     * @param splitChar the delimiter character
     * @return an array containing the split segments,
     *         or {@code null} if the input string is empty
     */
    public static @NotNull String @Nullable [] split(
            final @NotNull String message,
            final char splitChar
    ) {
        final int length = message.length();
        if (length == 0) {
            return null;
        }

        int parts = 0;
        for (int index = 0; index < length; index++) {
            final char character = message.charAt(index);
            if (character == splitChar) {
                parts++;
            }
        }

        final String[] sections = new String[parts + 1];
        int sectionIndex = 0;
        int startIndex = 0;

        for (int index = 0; index < length; index++) {
            final char character = message.charAt(index);
            if (character == splitChar) {
                sections[sectionIndex] = message.substring(startIndex, index);
                sectionIndex++;
                startIndex = index + 1;
            }
        }

        sections[sectionIndex] = message.substring(startIndex);
        return sections;
    }


    /**
     * Replaces multiple placeholders in a string with their corresponding values.
     * <p>
     * The arguments must come in pairs: {@code placeholder, value}.
     * For example:
     * <pre>{@code
     * StringUtil.replace("Hello {name}, your rank is {rank}",
     *                    "{name}", "Steve",
     *                    "{rank}", "Admin");
     * }</pre>
     *
     * @param input        the base input string
     * @param replacements placeholder-value pairs (must be even length)
     * @return the string with all replacements applied
     * @throws IllegalArgumentException if an odd number of replacement arguments is provided
     */
    public static @NotNull String replace(
            final @NotNull String input,
            final @NotNull String @NotNull ... replacements
    ) {
        if (replacements.length == 0) {
            return input;
        }

        if ((replacements.length & 1) != 0) {
            throw new IllegalArgumentException("Replacements must be provided in pairs: placeholder, value");
        }

        String result = input;
        for (int i = 0; i < replacements.length; i += 2) {
            final String placeholder = replacements[i];
            final String value = replacements[i + 1];

            result = replaceSingle(result, placeholder, value);
        }

        return result;
    }

    /**
     * Replaces all occurrences of a specific placeholder in the given string.
     * <p>
     * This method avoids regex and uses a manual search for maximum performance.
     *
     * @param input       the input text
     * @param placeholder the substring to replace
     * @param replacement the replacement text
     * @return the resulting string with replacements applied
     */
    public static @NotNull String replaceSingle(
            final @NotNull String input,
            final @NotNull String placeholder,
            final @NotNull String replacement
    ) {
        final int placeholderLength = placeholder.length();
        if (placeholderLength == 0 || input.isEmpty()) {
            return input;
        }

        final StringBuilder stringBuilder = new StringBuilder(input.length());
        int start = 0;
        int index;

        while ((index = input.indexOf(placeholder, start)) != -1) {
            stringBuilder
                    .append(input, start, index)
                    .append(replacement);

            start = index + placeholderLength;
        }

        // Append remaining part
        if (start < input.length()) {
            stringBuilder.append(input, start, input.length());
        }

        return stringBuilder.toString();
    }
}