package team.bytephoria.bytechat.chat.component;

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
 * Each {@link ChatComponent} defines one distinct part of the chat layout —
 * for example, the player's name, a separator, or the message body.
 * <p>
 * A {@code ChatComponent} can contain:
 * <ul>
 *     <li>A format string with optional placeholders (e.g. {@code {player}})</li>
 *     <li>Hover lines rendered as a multi-line tooltip</li>
 *     <li>A click action and value (e.g. {@code SUGGEST_COMMAND})</li>
 * </ul>
 * <p>
 * Alternatively, a {@code ChatComponent} may wrap a pre-built {@link Component}
 * directly. In that case all text, hover, and click fields are ignored. This is
 * used by the renderer to inject the player's already-assembled message component
 * without re-deserializing it.
 * <p>
 * Components are deserialized via a {@link ComponentSerializerAdapter} and later
 * combined into the full chat line by
 * {@link team.bytephoria.bytechat.chat.format.ChatFormat}.
 */
public final class ChatComponent {

    private final @NotNull String text;
    private final @Nullable List<String> hoverLines;
    private final @Nullable ClickEvent.Action clickAction;
    private final @Nullable String clickValue;

    /**
     * A pre-built component to use instead of the text/hover/click fields.
     * When non-null, {@link #toComponent} returns it immediately.
     */
    private final @Nullable Component prebuiltComponent;

    /**
     * Creates a standard format-driven component with optional hover and click.
     *
     * @param text        the format string; may contain placeholders
     * @param hoverLines  lines to show on hover, or {@code null} / empty for none
     * @param clickAction the click action type, or {@code null} for none
     * @param clickValue  the click action value, or {@code null} / blank for none
     */
    public ChatComponent(
            final @NotNull String text,
            final @Nullable List<String> hoverLines,
            final @Nullable ClickEvent.Action clickAction,
            final @Nullable String clickValue
    ) {
        this.text = text;
        this.hoverLines = hoverLines;
        this.clickAction = clickAction;
        this.clickValue = clickValue;
        this.prebuiltComponent = null;
    }

    /**
     * Creates a component that wraps a pre-built {@link Component}.
     * <p>
     * Used by the renderer to inject the player's message component directly,
     * bypassing deserialization and placeholder substitution entirely.
     *
     * @param prebuiltComponent the component to return from {@link #toComponent}
     */
    public ChatComponent(final @NotNull Component prebuiltComponent) {
        this.text = "";
        this.hoverLines = null;
        this.clickAction = null;
        this.clickValue = null;
        this.prebuiltComponent = prebuiltComponent;
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
     * Returns {@code true} if this component represents the player's message body.
     * <p>
     * The renderer uses this to substitute the pre-assembled message component
     * instead of performing placeholder replacement and deserialization.
     */
    public boolean isMessageComponent() {
        return this.prebuiltComponent == null && this.text.contains("{message}");
    }

    /**
     * Converts this component into a fully rendered Adventure {@link Component}.
     * <p>
     * If a pre-built component was supplied at construction time, it is returned
     * immediately. Otherwise, placeholders are resolved, the text is deserialized
     * via the given {@code adapter}, and any hover or click events are attached.
     *
     * @param adapter      the serializer used to deserialize format strings
     * @param replacements placeholder resolver, or {@code null} to skip substitution
     * @return the rendered {@link Component}
     */
    public @NotNull Component toComponent(
            final @NotNull ComponentSerializerAdapter adapter,
            final @Nullable Function<String, String> replacements
    ) {
        if (this.prebuiltComponent != null) {
            return this.prebuiltComponent;
        }

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
     * Builds a hover event from the configured lines, resolving placeholders and
     * deserializing each line individually.
     *
     * @return the hover event, or {@code null} if no lines are configured
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

            lines.add(adapter.deserialize(this.apply(line, replacements)));
        }

        return HoverEvent.showText(ComponentUtil.joinLines(lines));
    }

    /**
     * Builds a click event from the configured action and value, resolving
     * placeholders in the value string first.
     *
     * @return the click event, or {@code null} if no action or value is configured
     */
    private @Nullable ClickEvent createClickEvent(final @Nullable Function<String, String> replacements) {
        if (this.clickAction == null || this.clickValue == null || this.clickValue.isEmpty()) {
            return null;
        }

        final String value = this.apply(this.clickValue, replacements);
        return switch (this.clickAction.toString()) {
            case "open_url" -> ClickEvent.openUrl(value);
            case "open_file" -> ClickEvent.openFile(value);
            case "run_command" -> ClickEvent.runCommand(value);
            case "suggest_command" -> ClickEvent.suggestCommand(value);
            case "copy_to_clipboard" -> ClickEvent.copyToClipboard(value);
            default -> null;
        };
    }

    /**
     * Applies placeholder replacements to the given input string.
     * Returns the original input if {@code replacements} is {@code null} or
     * if the function itself returns {@code null}.
     */
    private @NotNull String apply(
            final @NotNull String input,
            final @Nullable Function<String, String> replacements
    ) {
        if (replacements == null) {
            return input;
        }

        final String result = replacements.apply(input);
        return result != null ? result : input;
    }
}
