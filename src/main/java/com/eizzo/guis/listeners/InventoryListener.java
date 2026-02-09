package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import java.util.List;
import java.util.Map;
public class InventoryListener implements Listener {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public InventoryListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        // If editing, let EditorListener handle it
        if (plugin.getEditorManager().isEditing(player)) return;
        String menuName = plugin.getGuiManager().getOpenedMenu(player);
        if (menuName == null) return;
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        // Handle list GUI clicks
        if ("__LIST__".equals(menuName)) {
            handleListClick(player, event);
            return;
        }

        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        if (config == null) return;
        int slot = event.getSlot();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;
        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;
            boolean isSlot = false;
            if (itemSection.contains("slots")) {
                isSlot = itemSection.getIntegerList("slots").contains(slot);
            } else {
                isSlot = itemSection.getInt("slot") == slot;
            }
            if (isSlot) {
                // Check Click Requirements
                if (!plugin.getRequirementManager().checkClickRequirements(player, itemSection)) {
                    playSound(player, "fail-sound", "BLOCK_ANVIL_LAND");
                    return;
                }
                // Take Requirements (Money/Tokens)
                plugin.getRequirementManager().takeRequirements(player, itemSection);
                playSound(player, "click-sound", "UI_BUTTON_CLICK");
                List<String> actions = itemSection.getStringList("actions");
                executeActions(player, actions);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // Only play close sound if they are actually leaving the GUI system
            // However, distinguishing between "switching menu" and "closing" is hard without custom events.
            // For now, we'll rely on the client or the fact that openMenu might play an "open" sound overriding this.
            // Or just play it.
            if (plugin.getGuiManager().getOpenedMenu(player) != null) {
                playSound(player, "close-sound", "BLOCK_CHEST_CLOSE");
                plugin.getGuiManager().removePlayer(player);
            }
        }
    }

    private void executeActions(Player player, List<String> actions) {
        Map<String, String> args = plugin.getGuiManager().getContext(player);
        for (String action : actions) {
            // Replace args
            for (Map.Entry<String, String> entry : args.entrySet()) {
                action = action.replace("%arg_" + entry.getKey() + "%", entry.getValue());
            }

            // Built-in player placeholders
            action = action.replace("%player_name%", player.getName());
            action = action.replace("%player_displayname%", net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(player.displayName()));
            action = action.replace("%player_uuid%", player.getUniqueId().toString());
            action = action.replace("%player_world%", player.getWorld().getName());
            action = action.replace("%player_ping%", String.valueOf(player.getPing()));
            action = action.replace("%player_level%", String.valueOf(player.getLevel()));
            action = action.replace("%player_gamemode%", player.getGameMode().name());

            // Replace PlaceholderAPI placeholders (will override built-ins)
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    action = PlaceholderAPI.setPlaceholders(player, action);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to parse PlaceholderAPI in action: " + e.getMessage());
                }
            }
            if (action.startsWith("[close]")) {
                player.closeInventory();
            } else if (action.startsWith("[message] ")) {
                player.sendMessage(mm.deserialize(action.substring(10)));
            } else if (action.startsWith("[player] ")) {
                player.performCommand(action.substring(9));
            } else if (action.startsWith("[console] ")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.substring(10));
            } else if (action.startsWith("[op] ")) {
                boolean wasOp = player.isOp();
                try {
                    player.setOp(true);
                    player.performCommand(action.substring(5));
                } finally {
                    player.setOp(wasOp);
                }
            } else if (action.startsWith("[opengui] ")) {
                String menu = action.substring(9).trim();
                // Pass current args to next menu?
                // Usually we might want to start fresh or merge.
                // For simplicity, we pass the SAME context.
                plugin.getGuiManager().openMenu(player, menu, args);
            }
        }
    }

    private void handleListClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();
        org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == org.bukkit.Material.AIR) return;

        // Close button (slot 53)
        if (slot == 53) {
            player.closeInventory();
            return;
        }

        // Menu items are in slots 0-44
        if (slot >= 45) return;

        net.kyori.adventure.text.Component displayNameComp = clickedItem.getItemMeta().displayName();
        if (displayNameComp == null) return;

        String displayName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayNameComp);
        String menuName = displayName; // Menu items are named after the menu

        // Verify this menu exists
        if (plugin.getConfigManager().getMenu(menuName) == null) return;

        boolean isRightClick = event.isRightClick();
        boolean isLeftClick = event.isLeftClick();

        // Right-click to edit (admin only)
        if (isRightClick && player.hasPermission("eguis.admin")) {
            player.closeInventory();
            plugin.getEditorManager().openEditor(player, menuName);
            playSound(player, "click-sound", "UI_BUTTON_CLICK");
            return;
        }

        // Left-click or any click to open
        if (isLeftClick || isRightClick) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getGuiManager().openMenu(player, menuName);
            }, 1L);
            playSound(player, "click-sound", "UI_BUTTON_CLICK");
        }
    }

    private void playSound(Player player, String configKey, String defaultSound) {
        String soundName = plugin.getConfig().getString(configKey, defaultSound);
        if (soundName != null && !soundName.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName), 1f, 1f);
            } catch (IllegalArgumentException ignored) {}
        }
    }

}
