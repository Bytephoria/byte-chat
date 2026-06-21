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
import team.bytephoria.bytechat.ui.EnderChestPreviewMenu;

import java.time.Duration;

/**
 * Built-in ender chest tag — a clickable link that opens a preview of the
 * sender's ender chest. Registered under several aliases ({@code [ec]},
 * {@code [ender]}, {@code [enderchest]}).
 */
public final class EnderChestTag extends AbstractTags {

    private final ChatConfiguration.EnderChestTag configuration;
    public EnderChestTag(final @NotNull String key, final @NotNull ChatConfiguration.EnderChestTag configuration) {
        super(key, FeaturePermission.Format.TAG_ENDERCHEST);
        this.configuration = configuration;
    }

    @Override
    public @NotNull Component render(final @NotNull TagContext context) {
        final Player player = context.sender();
        final String title = this.configuration.previewTitle().replace("{player_name}", player.getName());

        final EnderChestPreviewMenu previewMenu = EnderChestPreviewMenu.create(player, Component.text(title));
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
