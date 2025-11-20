package team.bytephoria.bytechat.chat.renderer;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.chat.element.ChatElement;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.placeholder.PlaceholderResolver;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;

import java.util.Collection;
import java.util.function.Function;

/**
 * A {@link ChatRenderer.ViewerUnaware} implementation that renders
 * chat messages using a predefined {@link ChatFormat}.
 * <p>
 * This renderer combines all {@link ChatElement}s in the order defined
 * by the format and applies placeholder replacements without considering
 * viewer-specific context.
 */
public final class ViewerUnawareChatRenderer implements ChatRenderer.ViewerUnaware {

    private final ChatFormat chatFormat;
    private final SignedMessage signedMessage;
    private final ChatConfiguration chatConfiguration;
    private final ComponentSerializerAdapter componentSerializerAdapter;

    public ViewerUnawareChatRenderer(
            final @NotNull ChatFormat chatFormat,
            final @NotNull SignedMessage signedMessage,
            final @NotNull ChatConfiguration chatConfiguration,
            final @NotNull ComponentSerializerAdapter componentSerializerAdapter
    ) {
        this.chatFormat = chatFormat;
        this.signedMessage = signedMessage;
        this.chatConfiguration = chatConfiguration;
        this.componentSerializerAdapter = componentSerializerAdapter;
    }

    @Override
    public @NotNull Component render(
            final @NotNull Player source,
            final @NotNull Component sourceDisplayName,
            final @NotNull Component message
    ) {
        // Prepare message text according to player permissions and configuration
        final String preparedMessage = this.preparePlayerMessage(source);

        // Build placeholder replacements for this player and message context
        final Function<String, String> replacements = PlaceholderResolver.create(source, preparedMessage);

        // Render all chat elements in the configured order
        final Collection<ChatElement> chatElements = this.chatFormat.allElements();
        return this.composeMessage(chatElements, replacements);
    }

    /**
     * Sanitizes and prepares the player's message before rendering,
     * based on configuration and player permissions.
     * <ul>
     *     <li>If text formatting is disabled or the player lacks permission,
     *         all formatting codes are stripped using {@link PlainTextComponentSerializer}.</li>
     *     <li>Otherwise, the message remains intact and will be deserialized
     *         by the current {@link ComponentSerializerAdapter} (e.g., MiniMessage or LegacyAmpersand).</li>
     * </ul>
     *
     * @param player the player who sent the message
     * @return a sanitized message string ready for deserialization
     */
    private @NotNull String preparePlayerMessage(final @NotNull Player player) {
        final boolean allowFormatting = this.chatConfiguration.chat().textFormatting();
        final String rawMessage = this.signedMessage.message();

        if (!allowFormatting || !player.hasPermission(FeaturePermission.Format.COLOR)) {
            // Remove all formatting -> ensure message is plain text
            return PlainTextComponentSerializer.plainText().serialize(
                    this.componentSerializerAdapter.deserialize(rawMessage)
            );
        }

        return rawMessage;
    }

    /**
     * Builds the final formatted chat message by combining all {@link ChatElement}s
     * defined in the {@link ChatFormat}, applying placeholder replacements sequentially.
     *
     * @param chatElements the ordered chat elements from the format
     * @param replacements the placeholder replacement function for this render context
     * @return the composed {@link Component} representing the full chat message
     */
    private @NotNull Component composeMessage(
            final @NotNull Collection<ChatElement> chatElements,
            final @NotNull Function<String, String> replacements
    ) {
        Component result = Component.empty();
        for (final ChatElement element : chatElements) {
            result = result.append(element.toComponent(this.componentSerializerAdapter, replacements));
        }

        return result;
    }
}
