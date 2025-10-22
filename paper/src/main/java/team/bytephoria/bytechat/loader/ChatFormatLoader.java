package team.bytephoria.bytechat.loader;

import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.bytephoria.bytechat.api.Loader;
import team.bytephoria.bytechat.chat.element.ChatElement;
import team.bytephoria.bytechat.chat.format.ChatFormat;
import team.bytephoria.bytechat.configuration.FormatConfiguration;
import team.bytephoria.bytechat.registry.ChatFormatRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and registers all chat formats defined in the configuration.
 * <p>
 * This loader reads {@link FormatConfiguration} data, constructs
 * corresponding {@link ChatFormat} and {@link ChatElement} objects,
 * and registers them in the {@link ChatFormatRegistry}.
 */
public final class ChatFormatLoader implements Loader {

    private final ChatFormatRegistry registry;
    private final FormatConfiguration configuration;

    public ChatFormatLoader(
            final @NotNull ChatFormatRegistry registry,
            final @NotNull FormatConfiguration configuration
    ) {
        this.registry = registry;
        this.configuration = configuration;
    }

    @Override
    public void load() {
        this.configuration.formats().forEach(this::loadFormat);
    }

    private void loadFormat(final @NotNull String id, final @NotNull FormatConfiguration.ChatFormat chatFormatConfiguration) {
        final Map<String, ChatElement> elements = new LinkedHashMap<>(chatFormatConfiguration.elements().size());

        chatFormatConfiguration.elements().forEach((elementId, elementConfig) -> {
            final ChatElement element = this.createElement(elementConfig);
            elements.put(elementId, element);
        });

        final ChatFormat chatFormat = new ChatFormat(
                id,
                chatFormatConfiguration.permission(),
                chatFormatConfiguration.priority(),
                elements
        );

        this.registry.register(id, chatFormat);
    }

    private @NotNull ChatElement createElement(final @NotNull FormatConfiguration.ChatElement chatElement) {
        final List<String> hoverLines = chatElement.hover();
        final FormatConfiguration.ClickAction clickConfig = chatElement.click();

        final ClickEvent.Action action = this.parseClickAction(clickConfig.action());
        final String value = clickConfig.value();

        return new ChatElement(chatElement.text(), hoverLines, action, value);
    }

    private @Nullable ClickEvent.Action parseClickAction(final @NotNull String actionName) {
        try {
            return ClickEvent.Action.valueOf(actionName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void unload() {
        this.registry.clearAll();
    }
}