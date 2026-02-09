package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.managers.RequirementEditorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
public class RequirementEditorListener implements Listener {
    private final EizzoGUIs plugin;
    public RequirementEditorListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        RequirementEditorManager.RequirementEditSession session = plugin.getRequirementEditorManager().getSession(player);
        if (session == null) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainComponentSerializer.plain().serialize(event.getView().title());
        if (!title.contains("Requirements (Slot")) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        if (slot == 11) { // Money
            plugin.getChatInputManager().requestInput(player, "<yellow>Enter cost (0 to remove):", (input) -> {
                try {
                    double amount = Double.parseDouble(input);
                    plugin.getRequirementEditorManager().setMoney(player, amount);
                } catch (NumberFormatException e) {
                    com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<red>Invalid number.");
                    plugin.getRequirementEditorManager().openRequirementEditor(player, session.menuName, session.slot);
                }
            }, () -> plugin.getRequirementEditorManager().openRequirementEditor(player, session.menuName, session.slot));
        } else if (slot == 13) { // Permission
            plugin.getChatInputManager().requestInput(player, "<yellow>Enter permission node ('none' to remove):", (input) -> {
                plugin.getRequirementEditorManager().setPermission(player, input);
            }, () -> plugin.getRequirementEditorManager().openRequirementEditor(player, session.menuName, session.slot));
        } else if (slot == 15) { // Token ID
            if (plugin.getConfig().getBoolean("use-tokens")) {
                plugin.getChatInputManager().requestInput(player, "<yellow>Enter Token ID ('default' to use config):", (input) -> {
                    plugin.getRequirementEditorManager().setTokenId(player, input);
                }, () -> plugin.getRequirementEditorManager().openRequirementEditor(player, session.menuName, session.slot));
            }
        } else if (slot == 31) { // Back
            plugin.getSlotEditorManager().openSlotEditor(player, session.menuName, session.slot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (plugin.getEditorManager().isRefreshing(player)) return;
            plugin.getRequirementEditorManager().endSession(player);
        }
    }

}

