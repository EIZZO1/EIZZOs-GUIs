package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
public class EditorManager {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, String> editingPlayers = new HashMap<>();
    private final Set<UUID> refreshing = new HashSet<>();
    public EditorManager(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    public void openEditor(Player player, String menuName) {
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        if (config == null) {
            com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<red>Menu not found. Use /eguis create <name> <rows> first.");
            return;
        }
        refreshing.add(player.getUniqueId());
        editingPlayers.put(player.getUniqueId(), menuName);
        // Reuse the GUIManager to open the menu
        plugin.getGuiManager().openMenu(player, menuName);
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                refreshing.remove(player.getUniqueId());
            }
        }.runTask(plugin);
    }

    public boolean isEditing(Player player) {
        return editingPlayers.containsKey(player.getUniqueId());
    }

    public boolean isRefreshing(Player player) {
        return refreshing.contains(player.getUniqueId());
    }

    public Set<UUID> getRefreshing() {
        return refreshing;
    }

    public String getEditingMenu(Player player) {
        return editingPlayers.get(player.getUniqueId());
    }

    public void stopEditing(Player player) {
        editingPlayers.remove(player.getUniqueId());
    }

    public Set<UUID> getEditingPlayers() {
        return editingPlayers.keySet();
    }

    public void setItemFromHand(Player player, int slot, ItemStack handItem) {
        if (handItem == null || handItem.getType() == Material.AIR) return;
        String menuName = getEditingMenu(player);
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        String path = "items.slot_" + slot;
        config.set(path + ".slot", slot);
        config.set(path + ".material", handItem.getType().name());
        if (handItem.hasItemMeta()) {
            ItemMeta meta = handItem.getItemMeta();
            if (meta.hasDisplayName()) {
                config.set(path + ".name", net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(meta.displayName()));
            }
            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                if (meta.lore() != null) {
                    for (net.kyori.adventure.text.Component line : meta.lore()) {
                        lore.add(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(line));
                    }
                }
                config.set(path + ".lore", lore);
            }
            if (meta.hasCustomModelData()) {
                config.set(path + ".model-data", meta.getCustomModelData());
            }
        }
        if (!config.contains(path + ".actions")) {
             config.set(path + ".actions", Collections.singletonList("[close]"));
        }
        plugin.getConfigManager().saveMenu(menuName, config);
        openEditor(player, menuName); // Refresh
        com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<green>Item set at slot " + slot);
    }

    public void deleteItem(Player player, int slot) {
        String menuName = getEditingMenu(player);
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        ConfigurationSection items = config.getConfigurationSection("items");
        if (items != null) {
            String keyToDelete = null;
            for (String key : items.getKeys(false)) {
                ConfigurationSection itemSection = items.getConfigurationSection(key);
                if (itemSection == null) continue;
                if (itemSection.getInt("slot") == slot) {
                    keyToDelete = key;
                    break;
                }
                List<Integer> slots = itemSection.getIntegerList("slots");
                if (slots.contains(slot)) {
                    com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<red>Cannot delete multi-slot items via click yet.");
                    return;
                }
            }
            if (keyToDelete != null) {
                config.set("items." + keyToDelete, null);
                plugin.getConfigManager().saveMenu(menuName, config);
                openEditor(player, menuName);
                com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<green>Item deleted.");
            }
        }
    }

    public void createMenu(Player player, String name, int rows) {
        if (rows < 1 || rows > 6) {
            com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<red>Rows must be between 1 and 6.");
            return;
        }
        if (plugin.getConfigManager().getMenu(name) != null) {
            com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<red>Menu '" + name + "' already exists.");
            return;
        }
        FileConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        config.set("config-version", 1.0);
        config.set("title", "<black>" + name);
        config.set("size", rows * 9);
        plugin.getConfigManager().saveMenu(name, config);
        com.eizzo.guis.utils.ChatUtils.sendMessage(player, "<green>Menu '" + name + "' created!");
        openEditor(player, name);
    }

}
