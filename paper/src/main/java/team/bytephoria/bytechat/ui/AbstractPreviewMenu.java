package team.bytephoria.bytechat.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPreviewMenu implements InventoryHolder {

    private final Inventory inventory;
    protected AbstractPreviewMenu(final @NotNull Component title) {
        this.inventory = Bukkit.createInventory(this, 54, title);
    }

    protected AbstractPreviewMenu(final @NotNull InventoryType inventoryType, final @NotNull Component title) {
        this.inventory = Bukkit.createInventory(this, inventoryType, title);
    }

    public abstract void populateInventory(
            final @NotNull Player player
    );

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
