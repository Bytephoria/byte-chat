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
 * format and applies placeholder replacements and tag injection without
 * considering viewer-specific context.
 * <p>
 * Tag resolution, formatting cleanup, and mention handling occur before the
 * placeholder system and component deserialization are applied.
 */
public final class ViewerUnawareChatRenderer implements ChatRenderer.ViewerUnaware {

    private final ChatFormat chatFormat;
    private final SignedMessage signedMessage;
    private final ChatConfiguration chatConfiguration;
    private final MentionResolverService mentionResolverService;
    private final TagResolverService tagResolverService;
    private final ComponentSerializerAdapter componentSerializerAdapter;

    public ViewerUnawareChatRenderer(
            final @NotNull ChatFormat chatFormat,
            final @NotNull SignedMessage signedMessage,
            final @NotNull ChatConfiguration chatConfiguration,
            final @NotNull MentionResolverService mentionResolverService,
            final @NotNull TagResolverService tagResolverService,
            final @NotNull ComponentSerializerAdapter componentSerializerAdapter
    ) {
        this.chatFormat = chatFormat;
        this.signedMessage = signedMessage;
        this.chatConfiguration = chatConfiguration;
        this.mentionResolverService = mentionResolverService;
        this.tagResolverService = tagResolverService;
        this.componentSerializerAdapter = componentSerializerAdapter;
    }

    @Override
    public @NotNull Component render(
            final @NotNull Player source,
            final @NotNull Component sourceDisplayName,
            final @NotNull Component message
    ) {
        // Resolve tags, mentions, and formatting rules according to player permissions.
        final @NotNull Pair<String, TagResolverService.TagResolutionResult> preparedMessage = this.preparePlayerMessage(source);
        final String preparedPlayerMessage = preparedMessage.left();
        final TagResolverService.TagResolutionResult tagResolutionResult = preparedMessage.right();

        // Create placeholder replacements for the current player/message context.
        final Function<String, String> replacements = PlaceholderResolver.create(source, preparedPlayerMessage);

        // Render all chat elements defined in the format.
        final Collection<ChatElement> chatElements = this.chatFormat.allElements();
        return this.composeMessage(chatElements, tagResolutionResult, replacements);
    }

    /**
     * Prepares the raw message sent by the player. This includes:
     * <ul>
     *     <li>Resolving custom ByteChat tags before any formatting is removed.</li>
     *     <li>Stripping formatting codes if the player lacks permission or if formatting is disabled.</li>
     *     <li>Resolving mentions if the feature is enabled in configuration.</li>
     * </ul>
     * <p>
     * The output is a plain-text or formatted string depending on permissions,
     * ready for placeholder evaluation and component deserialization.
     *
     * @param player the player who sent the message
     * @return a pair containing the sanitized message string and the tag resolution results
     */
    private @NotNull Pair<String, TagResolverService.TagResolutionResult> preparePlayerMessage(final @NotNull Player player) {
        String resolvedMessage = this.signedMessage.message(); // Raw signed message text

        TagResolverService.TagResolutionResult tagResult = null;

        // Resolve custom tags before stripping formatting.
        if (this.chatConfiguration.chat().tags().enabled() && player.hasPermission(FeaturePermission.Format.TAG)) {
            tagResult = this.tagResolverService.resolveTags(player, resolvedMessage);
            resolvedMessage = tagResult.processedMessage();
        }

        // Strip formatting if disallowed for the player or globally disabled.
        if (!this.chatConfiguration.chat().textFormatting() || !player.hasPermission(FeaturePermission.Format.COLOR)) {
            resolvedMessage = PlainTextComponentSerializer.plainText().serialize(
                    this.componentSerializerAdapter.deserialize(resolvedMessage)
            );
        }

        // Resolve mentions if enabled by configuration.
        if (this.chatConfiguration.chat().mentions().enabled()
                && player.hasPermission(FeaturePermission.Format.MENTION)) {
            resolvedMessage = this.mentionResolverService.resolveMentions(player, resolvedMessage);
        }

        return Pair.of(resolvedMessage, tagResult);
    }

    /**
     * Sequentially composes the final chat output by appending each
     * {@link ChatElement}, applying placeholder replacements, and then injecting
     * the resolved tag components.
     *
     * @param chatElements the ordered chat elements that form the chat format
     * @param tagResolutionResult resolved tag components to be injected
     * @param replacements placeholder resolver for dynamic data
     * @return the composed chat component
     */
    private @NotNull Component composeMessage(
            final @NotNull Collection<ChatElement> chatElements,
            final @Nullable TagResolverService.TagResolutionResult tagResolutionResult,
            final @NotNull Function<String, String> replacements
    ) {
        Component result = Component.empty();

        // Compose static and placeholder-based chat elements.
        for (final ChatElement element : chatElements) {
            result = result.append(element.toComponent(this.componentSerializerAdapter, replacements));
        }

        // Inject tag components in place of placeholder markers.
        if (tagResolutionResult != null && !tagResolutionResult.tagComponents().isEmpty()) {
            result = this.injectTagComponents(result, tagResolutionResult);
        }

        return result;
    }

    /**
     * Injects interactive tag components into the already composed message.
     * Tags were previously replaced with unique placeholder markers during
     * resolution; this method replaces those markers with actual components.
     *
     * @param message the message containing placeholder text markers
     * @param tagResolutionResult the resolved tag components
     * @return the message with all tag components injected
     */
    private @NotNull Component injectTagComponents(
            final @NotNull Component message,
            final @NotNull TagResolverService.TagResolutionResult tagResolutionResult
    ) {
        Component result = message;

        // Replace each placeholder marker with its corresponding component.
        for (final TagResolverService.TagComponent tagComponent : tagResolutionResult.tagComponents()) {
            final String placeholder = tagComponent.placeholder();
            final Component component = tagComponent.component();

            result = result.replaceText(builder -> builder
                    .once()
                    .matchLiteral(placeholder)
                    .replacement(component)
            );
        }

        return result;
    }
}
