package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.managers.SlotEditorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
public class SlotEditorListener implements Listener {
    private final EizzoGUIs plugin;
    public SlotEditorListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        SlotEditorManager.SlotEditSession session = plugin.getSlotEditorManager().getSession(player);
        if (session == null) return;
        // Prevent conflict: If in Action/Lore/Requirement editor, ignore Slot Editor logic
        if (plugin.getActionEditorManager().getSession(player) != null || 
            plugin.getLoreEditorManager().getSession(player) != null ||
            plugin.getRequirementEditorManager().getSession(player) != null) {
            return;
        }
        // Check if we are in the slot editor inventory (by title or just active session + cancellation)
        // Since session exists, we assume top inventory is the editor.
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot == 29) { // Edit Name
            plugin.getChatInputManager().requestInput(player, "<yellow>Enter new name:", (input) -> {
                plugin.getSlotEditorManager().handleRename(player, input);
            }, () -> {
                plugin.getSlotEditorManager().openSlotEditor(player, session.menuName, session.slot);
            });
        } else if (slot == 31) { // Edit Lore
            plugin.getLoreEditorManager().openLoreEditor(player, session.menuName, session.slot);
        } else if (slot == 33) { // Edit Actions
            plugin.getActionEditorManager().openActionEditor(player, session.menuName, session.slot);
        } else if (slot == 35) { // Edit Requirements
            plugin.getRequirementEditorManager().openRequirementEditor(player, session.menuName, session.slot);
        } else if (slot == 49) { // Back
            plugin.getSlotEditorManager().endSession(player);
            plugin.getEditorManager().openEditor(player, session.menuName);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // If just refreshing (e.g. chat input closed it), don't end session
            if (plugin.getEditorManager().isRefreshing(player)) return;
            // Otherwise, end session
            plugin.getSlotEditorManager().endSession(player);
        }
    }

}

