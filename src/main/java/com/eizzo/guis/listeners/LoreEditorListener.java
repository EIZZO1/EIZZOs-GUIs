package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.managers.LoreEditorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
public class LoreEditorListener implements Listener {
    private final EizzoGUIs plugin;
    public LoreEditorListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        LoreEditorManager.LoreEditSession session = plugin.getLoreEditorManager().getSession(player);
        if (session == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainComponentSerializer.plain().serialize(event.getView().title());
        if (!title.contains("Lore Editor")) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot < 45) {
            // Lore line
            if (event.getCurrentItem() != null) {
                if (event.getClick().isLeftClick()) {
                    // Edit
                    plugin.getChatInputManager().requestInput(player, "<yellow>Enter new text for line:", (input) -> {
                        plugin.getLoreEditorManager().editLine(player, slot, input);
                    });
                } else if (event.getClick().isRightClick()) {
                    // Remove
                    plugin.getLoreEditorManager().removeLine(player, slot);
                }
            }
        } else if (slot == 49) { // Back
            plugin.getSlotEditorManager().openSlotEditor(player, session.menuName, session.slot);
        } else if (slot == 51) { // Add
            plugin.getChatInputManager().requestInput(player, "<yellow>Enter new lore line:", (input) -> {
                plugin.getLoreEditorManager().addLine(player, input);
            });
        } else if (slot == 47) { // Clear
            plugin.getLoreEditorManager().clearLore(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (plugin.getEditorManager().isRefreshing(player)) return;
            plugin.getLoreEditorManager().endSession(player);
        }
    }

}

