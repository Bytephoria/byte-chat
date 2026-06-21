package team.bytephoria.bytechat.tags.builtin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.api.tag.TagContext;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.tags.AbstractTags;
import team.bytephoria.bytechat.ui.EquipmentPreviewMenu;

import java.time.Duration;

/**
 * Built-in {@code [armor]} tag — a clickable link that opens a preview of the
 * sender's equipped armor, limited by configured use count and lifetime.
 */
public final class ArmorTag extends AbstractTags {

    private final ChatConfiguration.Tags.ArmorTag configuration;
    public ArmorTag(final @NotNull ChatConfiguration.Tags.ArmorTag configuration) {
        super("armor", FeaturePermission.Format.TAG_ARMOR);
        this.configuration = configuration;
    }

    @Override
    public @NotNull Component render(final @NotNull TagContext context) {
        final Player player = context.sender();
        final String title = this.configuration.previewTitle().replace("{player_name}", player.getName());

        final EquipmentPreviewMenu previewMenu = EquipmentPreviewMenu.create(player, title);
        final TextColor color = parseColor(this.configuration.displayColor());

        return Component.text(this.configuration.displayText(), color)
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
