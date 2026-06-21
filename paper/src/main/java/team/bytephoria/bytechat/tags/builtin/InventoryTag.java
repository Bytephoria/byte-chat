package team.bytephoria.bytechat.tags.builtin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.api.tag.TagContext;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.placeholder.PlaceholderResolver;
import team.bytephoria.bytechat.serializer.component.ComponentSerializerAdapter;
import team.bytephoria.bytechat.tags.AbstractTags;
import team.bytephoria.bytechat.ui.CompleteInventoryPreviewMenu;
import team.bytephoria.bytechat.util.ComponentUtil;

import java.time.Duration;
import java.util.List;

/**
 * Built-in {@code [inv]} tag — a clickable link that opens a preview of the
 * sender's full inventory, limited by configured use count and lifetime.
 */
public final class InventoryTag extends AbstractTags {

    private final ChatConfiguration.Tags.InventoryTag configuration;
    private final ComponentSerializerAdapter serializerAdapter;

    public InventoryTag(
            final @NotNull ComponentSerializerAdapter serializerAdapter,
            final @NotNull ChatConfiguration.Tags.InventoryTag configuration
    ) {
        super("inv", FeaturePermission.Format.TAG_INVENTORY);
        this.configuration = configuration;
        this.serializerAdapter = serializerAdapter;
    }

    @Override
    public @NotNull Component render(final @NotNull TagContext context) {
        final Player player = context.sender();
        final String title = this.configuration.previewTitle().replace("{player_name}", player.getName());

        final CompleteInventoryPreviewMenu previewMenu = CompleteInventoryPreviewMenu.create(player, title);
        final TextColor color = parseColor(this.configuration.displayColor());
        final List<String> hoverString = this.configuration.hover();

        hoverString.replaceAll(s -> PlaceholderResolver.resolvePlaceholders(player, s));

        final List<Component> hover = ComponentUtil.parseStringCollection(hoverString, this.serializerAdapter);

        return Component.text(this.configuration.displayText(), color)
                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtil.joinLines(hover)))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player clickedPlayer) {
                        clickedPlayer.openInventory(previewMenu.getInventory());
                    }
                }, ClickCallback.Options.builder()
                        .uses(this.configuration.maxClicks())
                        .lifetime(Duration.ofSeconds(this.configuration.expirationSeconds()))
                        .build()));
    }
}
