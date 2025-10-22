package team.bytephoria.bytechat.util;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

import java.util.Collection;

/**
 * Utility class for working with {@link Component} objects.
 * <p>
 * Provides helper methods for safe concatenation and composition of multiple components.
 */
public final class ComponentUtil {

    private ComponentUtil() {
        throw new NonInstantiableClassException();
    }

    /**
     * Joins a collection of {@link Component}s with newline separators.
     * <p>
     * The resulting {@link Component} will include all elements in the given order,
     * with a newline between each of them. Empty collections return {@link Component#empty()}.
     *
     * @param components the components to join
     * @return a single {@link Component} containing all given components separated by newlines
     */
    public static @NotNull Component joinLines(final @NotNull Collection<? extends Component> components) {
        final var iterator = components.iterator();
        if (!iterator.hasNext()) {
            return Component.empty();
        }

        Component result = iterator.next();
        while (iterator.hasNext()) {
            result = result
                    .appendNewline()
                    .append(iterator.next());
        }

        return result;
    }
}
