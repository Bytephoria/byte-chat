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
import team.bytephoria.bytechat.chat.element.ChatElement;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.placeholder.PlaceholderResolver;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;
import team.bytephoria.bytechat.service.MentionResolverService;
import team.bytephoria.bytechat.service.TagResolverService;

import java.util.Collection;
import java.util.function.Function;

/**
 * A {@link ChatRenderer.ViewerUnaware} implementation responsible for rendering
 * chat messages using a predefined {@link ChatFormat}.
 * <p>
 * This renderer aggregates all {@link ChatElement}s in the order defined by the
 * format and applies placeholder replacements without considering viewer-specific
 * context.
 * <p>
 * The render pipeline follows this order:
 * <ol>
 *     <li>ByteChat inline tags ({@code [inv]}, {@code [item]}, etc.) are resolved
 *         into unique placeholder markers before any sanitization occurs.</li>
 *     <li>The remaining player input is sanitized segment-by-segment using a
 *         restricted serializer that strips interactive events (click, hover,
 *         insertion) while preserving color and decoration if permitted.</li>
 *     <li>The sanitized string and resolved tag components are combined into a
 *         single {@link Component} before being handed to the format system,
 *         avoiding any round-trip through the full-featured deserializer.</li>
 * </ol>
 */
public final class ViewerUnawareChatRenderer implements ChatRenderer.ViewerUnaware {

    private final ChatFormat chatFormat;
    private final SignedMessage signedMessage;
    private final ChatConfiguration chatConfiguration;
    private final MentionResolverService mentionResolverService;
    private final TagResolverService tagResolverService;

    /** Used to deserialize format elements defined by the server (name, separator, etc.). */
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
        final Pair<String, TagResolverService.TagResolutionResult> preparedMessage = this.preparePlayerMessage(source);
        final String preparedPlayerMessage = preparedMessage.left();
        final TagResolverService.TagResolutionResult tagResolutionResult = preparedMessage.right();

        // Build the message component before composing the full chat line so that
        // tag placeholders are injected directly as components rather than being
        // passed through the format deserializer as raw strings.
        final Component messageComponent = this.buildMessageComponent(preparedPlayerMessage, tagResolutionResult);
        final Function<String, String> replacements = PlaceholderResolver.create(source, preparedPlayerMessage);

        return this.composeMessage(this.chatFormat.allElements(), messageComponent, replacements);
    }

    /**
     * Prepares the raw signed message for rendering. The steps are:
     * <ol>
     *     <li>Resolve ByteChat inline tags (e.g. {@code [inv]}) into unique
     *         placeholder markers before any formatting is altered.</li>
     *     <li>Sanitize the remaining player text through the restricted
     *         {@link #playerInputSerializerAdapter}, removing interactive events
     *         while respecting the player's color/formatting permission.</li>
     *     <li>Resolve {@code @mentions} against online players if the feature
     *         is enabled and the player has the required permission.</li>
     * </ol>
     *
     * @param player the player who sent the message
     * @return a pair of the sanitized message string and the resolved tag components
     */
    private @NotNull Pair<String, TagResolverService.TagResolutionResult> preparePlayerMessage(
            final @NotNull Player player
    ) {
        String resolvedMessage = this.signedMessage.message();
        TagResolverService.TagResolutionResult tagResult = null;

        if (this.chatConfiguration.chat().tags().enabled() && player.hasPermission(FeaturePermission.Format.TAG)) {
            tagResult = this.tagResolverService.resolveTags(player, resolvedMessage);
            resolvedMessage = tagResult.processedMessage();
        }

        resolvedMessage = this.sanitizePlayerInput(player, resolvedMessage);
        if (this.chatConfiguration.chat().mentions().enabled() && player.hasPermission(FeaturePermission.Format.MENTION)) {
            resolvedMessage = this.mentionResolverService.resolveMentions(player, resolvedMessage);
        }

        return Pair.of(resolvedMessage, tagResult);
    }

    /**
     * Sanitizes player-written text while preserving ByteChat placeholder markers.
     * <p>
     * The input is split on placeholder boundaries so that each plain-text segment
     * is processed in isolation through the restricted serializer. Placeholder
     * markers ({@code <<<BYTECHAT_TAG_N>>>}) are passed through untouched and are
     * later replaced by their actual components in {@link #buildMessageComponent}.
     * <p>
     * This approach ensures that tag components built with {@link TagResolverService}
     * (which may carry click events for inventory previews) are never stripped,
     * while all interactive events injected by the player are removed.
     *
     * @param player  the player whose permissions determine whether colors are kept
     * @param message the message string, potentially containing placeholder markers
     * @return the sanitized message string with placeholders intact
     */
    private @NotNull String sanitizePlayerInput(
            final @NotNull Player player,
            final @NotNull String message
    ) {
        final boolean allowFormatting = this.chatConfiguration.chat().textFormatting() && player.hasPermission(FeaturePermission.Format.COLOR);
        final String[] parts = TagResolverService.PLACEHOLDER_SPLIT_PATTERN.split(message);
        final StringBuilder result = new StringBuilder(message.length());

        for (final String part : parts) {
            if (part.startsWith(TagResolverService.PLACEHOLDER_PREFIX)) {
                result.append(part);
                continue;
            }

            final Component sanitized = this.playerInputSerializerAdapter.deserialize(part);
            if (allowFormatting) {
                result.append(this.playerInputSerializerAdapter.serialize(sanitized));
            } else {
                result.append(PlainTextComponentSerializer.plainText().serialize(sanitized));
            }
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
     * {@link ChatElement#toComponent}, avoids feeding placeholder markers into the
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
     * Composes the final chat line by rendering each {@link ChatElement} in order.
     * <p>
     * Elements that represent the message body ({@link ChatElement#isMessageElement()})
     * receive the pre-assembled {@code messageComponent} directly, bypassing
     * placeholder substitution and deserialization entirely.
     * All other elements (name, separator, prefix, etc.) are rendered normally
     * using the full-featured {@link #componentSerializerAdapter}.
     *
     * @param chatElements     the ordered elements that define the chat format
     * @param messageComponent the pre-assembled, sanitized player message component
     * @param replacements     placeholder resolver for dynamic values such as {@code {player}}
     * @return the fully composed chat line as a single {@link Component}
     */
    private @NotNull Component composeMessage(
            final @NotNull Collection<ChatElement> chatElements,
            final @NotNull Component messageComponent,
            final @NotNull Function<String, String> replacements
    ) {
        Component result = Component.empty();

        for (final ChatElement element : chatElements) {
            if (element.isMessageElement()) {
                result = result.append(new ChatElement(messageComponent)
                        .toComponent(this.componentSerializerAdapter, null));
            } else {
                result = result.append(element.toComponent(this.componentSerializerAdapter, replacements));
            }
        }

        return result;
    }
}