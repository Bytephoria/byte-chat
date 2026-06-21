package team.bytephoria.bytechat.tags.builtin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.FeaturePermission;
import team.bytephoria.bytechat.api.tag.TagContext;
import team.bytephoria.bytechat.configuration.ChatConfiguration;
import team.bytephoria.bytechat.tags.AbstractTags;

/**
 * Built-in {@code [item]} tag — renders the player's main-hand item with a full
 * item tooltip, or a fallback text when the hand is empty.
 */
public final class ItemTag extends AbstractTags {

    private final ChatConfiguration.Tags.ItemTag configuration;
    public ItemTag(final @NotNull ChatConfiguration.Tags.ItemTag configuration) {
        super("item", FeaturePermission.Format.TAG_ITEM);
        this.configuration = configuration;
    }

    @Override
    public @NotNull Component render(final @NotNull TagContext context) {
        final Player player = context.sender();
        final ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            final TextColor color = parseColor(this.configuration.emptyHandColor());
            return Component.text(this.configuration.emptyHandText(), color);
        }

        return item.displayName()
                .hoverEvent(HoverEvent.showItem(item.asHoverEvent().value()));
    }
}
