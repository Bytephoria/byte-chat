package team.bytephoria.bytechat.chat.renderer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import team.bytephoria.bytechat.util.exception.NonInstantiableClassException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Inserts standalone components into an already-rendered component at specific
 * visible-character positions, leaving the surrounding formatting untouched.
 * <p>
 * This is what lets an inline chat tag behave as if it were not part of a gradient:
 * the player's message is rendered with the tag removed (so the gradient is computed
 * over uninterrupted text), then the tag's own component is spliced back in at the
 * position it occupied. The source is flattened to per-character units with fully
 * resolved styles, the components are inserted at their indices, and adjacent
 * characters that share a style are coalesced back into single text components.
 */
public final class ComponentSplicer {

    private ComponentSplicer() {
        throw new NonInstantiableClassException();
    }

    /**
     * Splices each insertion's component into {@code source} at its visible index.
     *
     * @param source     the rendered component to splice into
     * @param insertions the components to insert, each at a visible-character index
     *                   in {@code [0, visibleLength]}; processed in ascending order
     * @return the resulting component, or {@code source} unchanged if there is
     *         nothing to insert
     */
    public static @NotNull Component splice(
            final @NotNull Component source,
            final @NotNull List<Insertion> insertions
    ) {
        if (insertions.isEmpty()) {
            return source;
        }

        final List<Insertion> ordered = new ArrayList<>(insertions);
        ordered.sort(Comparator.comparingInt(Insertion::index));

        final List<StyledChar> units = flatten(source);
        final List<Component> parts = new ArrayList<>();
        final StringBuilder buffer = new StringBuilder();
        Style bufferStyle = null;
        int next = 0;

        for (int position = 0; position <= units.size(); position++) {
            while (next < ordered.size() && ordered.get(next).index() == position) {
                bufferStyle = flushBuffer(parts, buffer, bufferStyle);
                parts.add(ordered.get(next).component());
                next = next + 1;
            }

            if (position == units.size()) {
                break;
            }

            final StyledChar unit = units.get(position);
            if (bufferStyle != null && !bufferStyle.equals(unit.style())) {
                flushBuffer(parts, buffer, bufferStyle);
            }

            bufferStyle = unit.style();
            buffer.append(unit.character());
        }

        flushBuffer(parts, buffer, bufferStyle);
        return Component.empty().children(parts);
    }

    /**
     * Appends the buffered run (if any) to {@code parts} as a single text component
     * and clears the buffer.
     *
     * @return {@code null}, so callers can reset their current-style tracker
     */
    private static @Nullable Style flushBuffer(
            final @NotNull List<Component> parts,
            final @NotNull StringBuilder buffer,
            final Style style
    ) {
        if (!buffer.isEmpty()) {
            parts.add(Component.text(buffer.toString(), style));
            buffer.setLength(0);
        }

        return null;
    }

    /** Flattens a component tree into per-character units with fully resolved styles. */
    private static @NotNull List<StyledChar> flatten(final @NotNull Component component) {
        final List<StyledChar> units = new ArrayList<>();
        flattenInto(component, Style.empty(), units);
        return units;
    }

    private static void flattenInto(
            final @NotNull Component component,
            final @NotNull Style inherited,
            final @NotNull List<StyledChar> out
    ) {
        final Style resolved = inherited.merge(component.style(), Style.Merge.Strategy.ALWAYS);

        if (component instanceof TextComponent text) {
            final String content = text.content();
            for (int i = 0; i < content.length(); i++) {
                out.add(new StyledChar(content.charAt(i), resolved));
            }
        }

        for (final Component child : component.children()) {
            flattenInto(child, resolved, out);
        }
    }

    /**
     * A component to splice in at a visible-character {@code index} of the source.
     *
     * @param index     the visible-character position to insert at
     * @param component the component to insert
     */
    public record Insertion(int index, @NotNull Component component) {}

    private record StyledChar(char character, @NotNull Style style) {}
}
