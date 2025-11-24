package team.bytephoria.bytechat.ui.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import team.bytephoria.bytechat.ui.AbstractPreviewMenu;

public final class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClickEvent(final @NotNull InventoryClickEvent clickEvent) {
        final Inventory inventory = clickEvent.getInventory();
        if (inventory.getHolder() instanceof AbstractPreviewMenu) {
            clickEvent.setCancelled(true);
        }
    }

}
