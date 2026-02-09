package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.managers.ActionEditorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
public class ActionEditorListener implements Listener {
    private final EizzoGUIs plugin;
    public ActionEditorListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ActionEditorManager.ActionEditSession session = plugin.getActionEditorManager().getSession(player);
        if (session == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainComponentSerializer.plain().serialize(event.getView().title());
        if (!title.contains("Actions (Slot") && !title.contains("Select Action Type")) return;
        event.setCancelled(true);
        // Check if we are in the main list or selector
        if (title.contains("Actions (Slot")) {
            handleMainList(event, player, session);
        } else if (title.contains("Select Action Type")) {
            handleSelector(event, player);
        }
    }

    private void handleMainList(InventoryClickEvent event, Player player, ActionEditorManager.ActionEditSession session) {
        int slot = event.getSlot();
        if (slot < 45) {
            // Action item
            if (event.getCurrentItem() != null && event.getClick().isRightClick()) {
                plugin.getActionEditorManager().removeAction(player, slot);
            }
        } else if (slot == 49) { // Back
            plugin.getSlotEditorManager().openSlotEditor(player, session.menuName, session.slot);
        } else if (slot == 51) { // Add
            plugin.getActionEditorManager().openAddActionSelector(player);
        }
    }

    private void handleSelector(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();
        if (slot == 22) { // Cancel
             ActionEditorManager.ActionEditSession session = plugin.getActionEditorManager().getSession(player);
             if (session != null) {
                 plugin.getActionEditorManager().openActionEditor(player, session.menuName, session.slot);
             }
             return;
        }
        String prefix = null;
        if (slot == 10) prefix = "[close]";
        else if (slot == 11) prefix = "[message] ";
        else if (slot == 12) prefix = "[player] ";
        else if (slot == 13) prefix = "[console] ";
        else if (slot == 14) prefix = "[op] ";
        else if (slot == 15) prefix = "[opengui] ";
        if (prefix != null) {
            if (prefix.equals("[close]")) {
                plugin.getActionEditorManager().addAction(player, "[close]");
            } else {
                final String p = prefix;
                plugin.getChatInputManager().requestInput(player, "<yellow>Enter value for " + p.trim(), (input) -> {
                    plugin.getActionEditorManager().addAction(player, p + input);
                });
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (plugin.getEditorManager().isRefreshing(player)) return;
            plugin.getActionEditorManager().endSession(player);
        }
    }

}

