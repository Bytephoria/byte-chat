package team.bytephoria.bytechat.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class EnderChestPreviewMenu extends AbstractPreviewMenu {

    EnderChestPreviewMenu(final @NotNull InventoryType inventoryType, final @NotNull Component title) {
        super(inventoryType, title);
    }

    public static @NotNull EnderChestPreviewMenu create(final @NotNull Player player, final @NotNull Component title) {
        final EnderChestPreviewMenu menu = new EnderChestPreviewMenu(InventoryType.ENDER_CHEST, title);
        menu.populateInventory(player);
        return menu;
    }

    @Override
    public void populateInventory(final @NotNull Player player) {
        final ItemStack[] contents = player.getEnderChest().getContents();
        this.getInventory().setContents(contents);
    }
}
