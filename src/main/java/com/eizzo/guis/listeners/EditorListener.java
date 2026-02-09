package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
public class EditorListener implements Listener {
    private final EizzoGUIs plugin;
    public EditorListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!plugin.getEditorManager().isEditing(player)) return;
        // Prevent conflict: If in Slot/Action/Lore/Requirement editor, ignore Main Editor logic
        if (plugin.getSlotEditorManager().getSession(player) != null || 
            plugin.getActionEditorManager().getSession(player) != null ||
            plugin.getLoreEditorManager().getSession(player) != null ||
            plugin.getRequirementEditorManager().getSession(player) != null) {
            return;
        }
        // Ensure we are clicking top inventory
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();
        if (event.getClick() == ClickType.SHIFT_RIGHT) {
            // Set from hand
            plugin.getEditorManager().setItemFromHand(player, slot, player.getInventory().getItemInMainHand());
        } else if (event.getClick() == ClickType.MIDDLE) {
            // Delete
            plugin.getEditorManager().deleteItem(player, slot);
        } else if (event.getClick() == ClickType.LEFT) {
            // Open Slot Editor
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<red>Cannot edit an empty slot. Shift+Right Click with an item first.");
                return;
            }
            plugin.getSlotEditorManager().openSlotEditor(player, plugin.getEditorManager().getEditingMenu(player), slot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (plugin.getEditorManager().isEditing(player)) {
                if (plugin.getEditorManager().isRefreshing(player)) {
                    // Do nothing, we are just refreshing
                    return;
                }
                plugin.getEditorManager().stopEditing(player);
                com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<gray>Exited Editor Mode.");
            }
        }
    }

}

