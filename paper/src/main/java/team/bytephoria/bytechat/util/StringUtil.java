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

}