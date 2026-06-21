package team.bytephoria.bytechat.chat.renderer;

import io.papermc.paper.chat.ChatRenderer;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.api.event.ByteChatPostRenderEvent;
import team.bytephoria.bytechat.chat.component.ChatComponent;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.placeholder.PlaceholderResolver;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;
import team.bytephoria.bytechat.service.MentionResolverService;
import team.bytephoria.bytechat.service.TagResolverService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * A {@link ChatRenderer.ViewerUnaware} implementation that renders chat messages
 * using a predefined {@link ChatFormat}, composing the format's ordered
 * {@link ChatComponent}s around the player's (sanitized) message.
 * <p>
 * Player input is always parsed with a restricted serializer that strips interactive
 * events (click, hover, insertion) to prevent chat exploits, and inline tags
 * ({@code [inv]}, {@code [item]}, …) are expanded into their rich components. Two
 * strategies build the message body:
 * <ul>
 *     <li><b>Inline</b> ({@link #renderInline}) — used when {@code inline-formatting}
 *         is enabled and the player may use colors. Tags are stripped to invisible
 *         sentinels so surrounding gradients are computed over uninterrupted text,
 *         then their components are spliced back in via {@link ComponentSplicer},
 *         keeping each tag's own color.</li>
 *     <li><b>Segmented</b> ({@link #preparePlayerMessage}) — the fallback. Tags become
 *         placeholder markers; the text is sanitized segment-by-segment and the tag
 *         components are injected at the marker boundaries.</li>
 * </ul>
 */
public final class ViewerUnawareChatRenderer implements ChatRenderer.ViewerUnaware {

    private final ChatFormat chatFormat;
    private final SignedMessage signedMessage;
    private final ChatConfiguration chatConfiguration;
    private final MentionResolverService mentionResolverService;
    private final TagResolverService tagResolverService;

    /** Used to deserialize format components defined by the server (name, separator, etc.). */
    private final ComponentSerializerAdapter componentSerializerAdapter;

    /**
     * Used exclusively for player input. Configured to strip interactive tags
     * (click, hover, insertion) regardless of player permissions, preventing
     * chat exploit vectors such as {@code <click:run_command:/op ...>}.
     */
    private final ComponentSerializerAdapter playerInputSerializerAdapter;

    public ViewerUnawareChatRenderer(
            final @NotNull ChatFormat chatFormat,
            final @NotNull SignedMessage signedMessage,
            final @NotNull ChatConfiguration chatConfiguration,
            final @NotNull MentionResolverService mentionResolverService,
            final @NotNull TagResolverService tagResolverService,
            final @NotNull ComponentSerializerAdapter componentSerializerAdapter,
            final @NotNull ComponentSerializerAdapter playerInputSerializerAdapter
    ) {
        this.chatFormat = chatFormat;
        this.signedMessage = signedMessage;
        this.chatConfiguration = chatConfiguration;
        this.mentionResolverService = mentionResolverService;
        this.tagResolverService = tagResolverService;
        this.componentSerializerAdapter = componentSerializerAdapter;
        this.playerInputSerializerAdapter = playerInputSerializerAdapter;
    }

    @Override
    public @NotNull Component render(
            final @NotNull Player source,
            final @NotNull Component sourceDisplayName,
            final @NotNull Component message
    ) {
        final Component rendered = this.shouldRenderInline(source)
                ? this.renderInline(source)
                : this.renderSegmented(source);

        return this.firePostRender(source, rendered);
    }

    /**
     * Fires {@link ByteChatPostRenderEvent} with the final chat line, allowing addons
     * to observe or transform it, and returns the (possibly replaced) component.
     */
    private @NotNull Component firePostRender(final @NotNull Player source, final @NotNull Component rendered) {
        final ByteChatPostRenderEvent event = new ByteChatPostRenderEvent(source, this.chatFormat, rendered);
        event.callEvent();
        return event.component();
    }

    /**
     * The segmented render path: tags become placeholder markers, player text is
     * sanitized segment-by-segment, and the tag components are injected at the marker
     * boundaries. Used whenever the inline path does not apply.
     */
    private @NotNull Component renderSegmented(final @NotNull Player source) {
        final Pair<String, TagResolverService.TagResolutionResult> preparedMessage = this.preparePlayerMessage(source);
        final String preparedPlayerMessage = preparedMessage.left();
        final TagResolverService.TagResolutionResult tagResolutionResult = preparedMessage.right();

        // Build the message component before composing the full chat line so that
        // tag placeholders are injected directly as components rather than being
        // passed through the format deserializer as raw strings.
        final Component messageComponent = this.buildMessageComponent(preparedPlayerMessage, tagResolutionResult);
        final Function<String, String> replacements = PlaceholderResolver.create(source, preparedPlayerMessage);

        return this.composeMessage(this.chatFormat.allComponents(), messageComponent, replacements);
    }

    /**
     * Whether the inline (splice) render path applies for this message.
     * <p>
     * It requires the {@code inline-formatting} option, an active tag system, and a
     * player permitted to use colors — otherwise formatting is stripped anyway and the
     * continuity behavior is moot, so the segmented path is used.
     */
    private boolean shouldRenderInline(final @NotNull Player source) {
        final ChatConfiguration.Tags tags = this.chatConfiguration.chat().tags();
        return tags.enabled() && tags.inlineFormatting() && this.allowsFormatting(source);
    }

    /**
     * Renders the chat line so that surrounding formatting (gradients, rainbow) flows
     * across inline tags as if the tags were not present.
     * <p>
     * The player message is deserialized with each tag replaced by a sentinel that is
     * stripped before parsing — so the gradient is computed over the uninterrupted
     * text — and the rendered tag components are spliced back into the colored result
     * at their original positions. This keeps each tag's own color and contributes
     * nothing to the gradient's progression.
     */
    private @NotNull Component renderInline(final @NotNull Player source) {
        String resolvedMessage = this.signedMessage.message();
        List<Component> tagComponents = List.of();

        if (source.hasPermission(FeaturePermission.Format.TAG)) {
            final TagResolverService.InlineSplit split = this.tagResolverService.splitInlineTags(source, resolvedMessage);
            resolvedMessage = split.processedMessage();
            tagComponents = split.components();
        }

        final ChatConfiguration.PreProcessor preProcessor = this.chatConfiguration.chat().preProcessor();
        if (preProcessor.enabled() && !preProcessor.playerInput().isBlank()) {
            final String template = preProcessor.playerInput();
            final String placeholders = PlaceholderResolver.resolvePlaceholders(source, template);

            resolvedMessage = placeholders.replace("{message}", resolvedMessage);
        }

        if (this.chatConfiguration.chat().mentions().enabled() && source.hasPermission(FeaturePermission.Format.MENTION)) {
            resolvedMessage = this.mentionResolverService.resolveMentions(source, resolvedMessage);
        }

        final Component messageComponent = this.buildInlineMessage(resolvedMessage, tagComponents);
        final Function<String, String> replacements = PlaceholderResolver.create(source, resolvedMessage);

        return this.composeMessage(this.chatFormat.allComponents(), messageComponent, replacements);
    }

    /**
     * Deserializes {@code processed} with the tag sentinels removed, then splices the
     * resolved tag components back at the visible positions the sentinels occupied.
     */
    private @NotNull Component buildInlineMessage(
            final @NotNull String processed,
            final @NotNull List<Component> components
    ) {
        if (components.isEmpty()) {
            return this.playerInputSerializerAdapter.deserialize(processed);
        }

        final StringBuilder sourceText = new StringBuilder(processed.length());
        final List<ComponentSplicer.Insertion> insertions = new ArrayList<>(components.size());

        for (int i = 0; i < processed.length(); i++) {
            final char c = processed.charAt(i);
            final int sentinelIndex = c - TagResolverService.INLINE_SENTINEL_BASE;

            if (sentinelIndex >= 0 && sentinelIndex < components.size()) {
                insertions.add(new ComponentSplicer.Insertion(this.visibleLength(sourceText.toString()), components.get(sentinelIndex)));
            } else {
                sourceText.append(c);
            }
        }

        final Component rendered = this.playerInputSerializerAdapter.deserialize(sourceText.toString());
        return ComponentSplicer.splice(rendered, insertions);
    }

    /** Number of visible (plain-text) characters produced by deserializing {@code source}. */
    private int visibleLength(final @NotNull String source) {
        if (source.isEmpty()) {
            return 0;
        }

        final Component component = this.playerInputSerializerAdapter.deserialize(source);
        return PlainTextComponentSerializer.plainText().serialize(component).length();
    }

    /**
     * Returns whether the player may use colors and decorations in their message,
     * i.e. {@code text-formatting} is enabled and the player has the colour permission.
     */
    private boolean allowsFormatting(final @NotNull Player player) {
        return this.chatConfiguration.chat().textFormatting() && player.hasPermission(FeaturePermission.Format.COLOR);
    }

    /**
     * Prepares the raw signed message for rendering. The steps are:
     * <ol>
     *     <li>If the player may not use formatting, escape their input up front so
     *         their markup renders literally instead of being consumed — this keeps a
     *         pure-formatting message like {@code <red>} from collapsing to nothing,
     *         while leaving server-controlled content (pre-processor template,
     *         mentions) added later free to parse.</li>
     *     <li>Resolve ByteChat inline tags (e.g. {@code [inv]}) into unique
     *         placeholder markers.</li>
     *     <li>When formatting is allowed, sanitize the player text through the
     *         restricted {@link #playerInputSerializerAdapter}, removing interactive
     *         events while preserving colors and decorations.</li>
     *     <li>Resolve {@code @mentions} against online players if the feature
     *         is enabled and the player has the required permission.</li>
     * </ol>
     *
     * @param player the player who sent the message
     * @return a pair of the prepared message string and the resolved tag components
     */
    private @NotNull Pair<String, TagResolverService.TagResolutionResult> preparePlayerMessage(
            final @NotNull Player player
    ) {
        final boolean allowFormatting = this.allowsFormatting(player);
        String resolvedMessage = this.signedMessage.message();

        if (!allowFormatting) {
            resolvedMessage = this.playerInputSerializerAdapter.escape(resolvedMessage);
        }

        TagResolverService.TagResolutionResult tagResult = null;
        if (this.chatConfiguration.chat().tags().enabled() && player.hasPermission(FeaturePermission.Format.TAG)) {
            tagResult = this.tagResolverService.resolveTags(player, resolvedMessage);
            resolvedMessage = tagResult.processedMessage();
        }

        final ChatConfiguration.PreProcessor preProcessor = this.chatConfiguration.chat().preProcessor();
        if (preProcessor.enabled() && !preProcessor.playerInput().isBlank()) {
            final String template = preProcessor.playerInput();
            final String placeholders = PlaceholderResolver.resolvePlaceholders(player, template);

            resolvedMessage = placeholders.replace("{message}", resolvedMessage);
        }

        if (allowFormatting) {
            resolvedMessage = this.sanitizePlayerInput(resolvedMessage);
        }

        if (this.chatConfiguration.chat().mentions().enabled() && player.hasPermission(FeaturePermission.Format.MENTION)) {
            resolvedMessage = this.mentionResolverService.resolveMentions(player, resolvedMessage);
        }

        return Pair.of(resolvedMessage, tagResult);
    }

    /**
     * Strips interactive events (click, hover, insertion) from player-written text
     * while preserving its colors and decorations, and leaving ByteChat placeholder
     * markers untouched.
     * <p>
     * The input is split on placeholder boundaries so that each text segment is
     * round-tripped in isolation through the restricted serializer — which never
     * emits interactive events. Placeholder markers ({@code <<<BYTECHAT_TAG_N>>>})
     * are passed through and later replaced by their components in
     * {@link #buildMessageComponent}, so tag components (which may carry click events
     * for inventory previews) are never stripped.
     * <p>
     * Only invoked when the player is allowed to use formatting; otherwise the input
     * is escaped up front in {@link #preparePlayerMessage}.
     *
     * @param message the message string, potentially containing placeholder markers
     * @return the sanitized message string with placeholders intact
     */
    private @NotNull String sanitizePlayerInput(final @NotNull String message) {
        final String[] parts = TagResolverService.PLACEHOLDER_SPLIT_PATTERN.split(message);
        final StringBuilder result = new StringBuilder(message.length());

        for (final String part : parts) {
            if (part.startsWith(TagResolverService.PLACEHOLDER_PREFIX)) {
                result.append(part);
                continue;
            }

            final Component sanitized = this.playerInputSerializerAdapter.deserialize(part);
            result.append(this.playerInputSerializerAdapter.serialize(sanitized));
        }

        return result.toString();
    }

    /**
     * Assembles the player's message into a single {@link Component} by processing
     * each segment independently.
     * <p>
     * Segments that match a ByteChat placeholder marker are replaced directly with
     * the pre-built {@link TagResolverService.TagComponent} for that marker. All
     * other segments are deserialized through the restricted
     * {@link #playerInputSerializerAdapter}, which at this point only contains
     * safe formatting (colors, decorations) — interactive events were already
     * stripped in {@link #sanitizePlayerInput}.
     * <p>
     * Building the message component here, rather than deferring to
     * {@link ChatComponent#toComponent}, avoids feeding placeholder markers into the
     * full-featured format deserializer where they could be rendered as visible text.
     *
     * @param preparedMessage    the sanitized message string, may contain placeholder markers
     * @param tagResolutionResult resolved tag components keyed by their placeholder strings,
     *                            or {@code null} if the tag system was not active for this message
     * @return the assembled message component ready to be appended to the chat line
     */
    private @NotNull Component buildMessageComponent(
            final @NotNull String preparedMessage,
            final @Nullable TagResolverService.TagResolutionResult tagResolutionResult
    ) {
        final String[] parts = TagResolverService.PLACEHOLDER_SPLIT_PATTERN.split(preparedMessage);
        Component result = Component.empty();

        for (final String part : parts) {
            if (part.startsWith(TagResolverService.PLACEHOLDER_PREFIX) && tagResolutionResult != null) {
                final Component tagComponent = tagResolutionResult.tagComponents().stream()
                        .filter(tc -> tc.placeholder().equals(part))
                        .map(TagResolverService.TagComponent::component)
                        .findFirst()
                        .orElse(Component.empty());

                result = result.append(tagComponent);
                continue;
            }

            if (!part.isEmpty()) {
                result = result.append(this.playerInputSerializerAdapter.deserialize(part));
            }
        }

        return result;
    }

    /**
     * Composes the final chat line by rendering each {@link ChatComponent} in order.
     * <p>
     * Components that represent the message body ({@link ChatComponent#isMessageComponent()})
     * receive the pre-assembled {@code messageComponent} directly, bypassing
     * placeholder substitution and deserialization entirely.
     * All other components (name, separator, prefix, etc.) are rendered normally
     * using the full-featured {@link #componentSerializerAdapter}.
     *
     * @param chatComponents   the ordered components that define the chat format
     * @param messageComponent the pre-assembled, sanitized player message component
     * @param replacements     placeholder resolver for dynamic values such as {@code {player}}
     * @return the fully composed chat line as a single {@link Component}
     */
    private @NotNull Component composeMessage(
            final @NotNull Collection<ChatComponent> chatComponents,
            final @NotNull Component messageComponent,
            final @NotNull Function<String, String> replacements
    ) {
        Component result = Component.empty();

        for (final ChatComponent component : chatComponents) {
            if (component.isMessageComponent()) {
                result = result.append(new ChatComponent(messageComponent)
                        .toComponent(this.componentSerializerAdapter, null));
            } else {
                result = result.append(component.toComponent(this.componentSerializerAdapter, replacements));
            }
        }

        return result;
    }
}