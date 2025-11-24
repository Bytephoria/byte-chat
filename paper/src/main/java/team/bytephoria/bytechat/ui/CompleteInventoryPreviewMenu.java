package team.bytephoria.bytechat.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CompleteInventoryPreviewMenu extends AbstractPreviewMenu {

    private CompleteInventoryPreviewMenu(final @NotNull Player player, final @NotNull String title) {
        super(Component.text(title));
    }

    public static @NotNull CompleteInventoryPreviewMenu create(final @NotNull Player player, final @NotNull String title) {
        final CompleteInventoryPreviewMenu menu = new CompleteInventoryPreviewMenu(player, title);
        menu.populateInventory(player);
        return menu;
    }

    @Override
    public void populateInventory(final @NotNull Player player) {
        this.getInventory().setContents(player.getInventory().getContents());
    }
}
