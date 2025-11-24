package team.bytephoria.bytechat.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class EquipmentPreviewMenu extends AbstractPreviewMenu {

    private EquipmentPreviewMenu(final @NotNull Player player, final @NotNull String title) {
        super(InventoryType.HOPPER, Component.text(title));
    }

    public static @NotNull EquipmentPreviewMenu create(final @NotNull Player player, final @NotNull String title) {
        final EquipmentPreviewMenu menu = new EquipmentPreviewMenu(player, title);
        menu.populateInventory(player);
        return menu;
    }

    @Override
    public void populateInventory(final @NotNull Player player) {
        final ItemStack[] armorContents = player.getInventory().getArmorContents();
        final ItemStack offHand = player.getInventory().getItemInOffHand();

        final ItemStack[] contents = new ItemStack[5];
        System.arraycopy(armorContents, 0, contents, 0, armorContents.length);
        contents[armorContents.length] = offHand;

        this.getInventory().setContents(contents);
    }

}
