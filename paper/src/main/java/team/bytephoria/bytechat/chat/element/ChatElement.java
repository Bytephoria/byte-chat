package team.bytephoria.bytechat.chat.element;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;
import team.bytephoria.bytechat.util.ComponentUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a single visual section of a chat message.
 * <p>
 * Each {@link ChatElement} defines one distinct part of the chat layout —
 * for example, the player's name, separator, or message text.
 * <p>
 * A ChatElement can contain:
 * <ul>
 *     <li>Base text (supports placeholders and color formatting)</li>
 *     <li>Hover text (multi-line tooltips)</li>
 *     <li>Click actions (e.g. {@code RUN_COMMAND}, {@code SUGGEST_COMMAND}, {@code OPEN_URL})</li>
 * </ul>
 * <p>
 * Elements are deserialized via {@link ComponentSerializerAdapter}
 * and later combined by {@link team.bytephoria.bytechat.chat.format.ChatFormat}.
 */
public final class ChatElement {

    private final @NotNull String text;
    private final @Nullable List<String> hoverLines;
    private final @Nullable ClickEvent.Action clickAction;
    private final @Nullable String clickValue;

    public ChatElement(
            final @NotNull String text,
            final @Nullable List<String> hoverLines,
            final @Nullable ClickEvent.Action clickAction,
            final @Nullable String clickValue
    ) {
        this.text = text;
        this.hoverLines = hoverLines; // may be null
        this.clickAction = clickAction;
        this.clickValue = clickValue;
    }

    public @NotNull String text() {
        return this.text;
    }

    public @Nullable List<String> hoverLines() {
        return this.hoverLines;
    }

    public @Nullable ClickEvent.Action clickAction() {
        return this.clickAction;
    }

    public @Nullable String clickValue() {
        return this.clickValue;
    }

    /**
     * Converts this element into a fully rendered Adventure {@link Component}.
     * <p>
     * Applies placeholder replacements and attaches hover/click events if defined.
     *
     * @param adapter      serializer adapter for MiniMessage or Legacy formats
     * @param replacements function to replace placeholders dynamically (may be {@code null})
     * @return a rendered {@link Component} ready for display
     */
    public @NotNull Component toComponent(
            final @NotNull ComponentSerializerAdapter adapter,
            final @Nullable Function<String, String> replacements
    ) {
        final String processedText = this.apply(this.text, replacements);
        Component component = adapter.deserialize(processedText);

        final HoverEvent<Component> hover = this.createHoverEvent(adapter, replacements);
        if (hover != null) {
            component = component.hoverEvent(hover);
        }

        final ClickEvent click = this.createClickEvent(replacements);
        if (click != null) {
            component = component.clickEvent(click);
        }

        return component;
    }

    /**
     * Creates a hover event by joining multiple hover lines into one multiline {@link Component}.
     *
     * @return a hover event, or {@code null} if none are defined
     */
    private @Nullable HoverEvent<Component> createHoverEvent(
            final @NotNull ComponentSerializerAdapter adapter,
            final @Nullable Function<String, String> replacements
    ) {
        if (this.hoverLines == null || this.hoverLines.isEmpty()) {
            return null;
        }

        final List<Component> lines = new ArrayList<>(this.hoverLines.size());
        for (final String line : this.hoverLines) {
            if (line == null || line.isEmpty()) {
                lines.add(Component.empty());
                continue;
            }

            final String processed = this.apply(line, replacements);
            lines.add(adapter.deserialize(processed));
        }

        return HoverEvent.showText(ComponentUtil.joinLines(lines));
    }

    /**
     * Creates a click event, resolving placeholders in the target value if present.
     */
    private @Nullable ClickEvent createClickEvent(final @Nullable Function<String, String> replacements) {
        if (this.clickAction == null || this.clickValue == null || this.clickValue.isEmpty()) {
            return null;
        }

        final String processedValue = this.apply(this.clickValue, replacements);
        return ClickEvent.clickEvent(this.clickAction, processedValue);
    }

    /**
     * Applies the given replacements to the input string safely.
     * <p>
     * Returns the original text if {@code replacements} is {@code null}
     * or if the replacement function returns {@code null}.
     */
    private @NotNull String apply(final @NotNull String input, final @Nullable Function<String, String> replacements) {
        if (replacements == null) {
            return input;
        }

        final String result = replacements.apply(input);
        return result != null ? result : input;
    }

}
