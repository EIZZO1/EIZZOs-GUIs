package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;
public class GUIManager {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, String> openMenus = new HashMap<>();
    private final Map<UUID, Map<String, String>> playerContext = new HashMap<>();
    public GUIManager(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, String menuName) {
        openMenu(player, menuName, new HashMap<>());
    }

    public void openMenu(Player player, String menuName, Map<String, String> args) {
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        if (config == null) {
            player.sendMessage(mm.deserialize("<red>Menu not found: " + menuName));
            return;
        }
        playerContext.put(player.getUniqueId(), args);
        String title = config.getString("title", "GUI");
        title = replacePlaceholders(player, title);
        int size = config.getInt("size", 27);
        Inventory inv = Bukkit.createInventory(null, size, mm.deserialize(title));
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;
                if (!plugin.getRequirementManager().checkViewRequirements(player, itemSection)) {
                    continue;
                }
                ItemStack item = createItem(player, itemSection);
                if (item != null) {
                    List<Integer> slots = itemSection.getIntegerList("slots");
                    if (slots.isEmpty()) {
                        int slot = itemSection.getInt("slot", -1);
                        if (slot >= 0 && slot < size) {
                            inv.setItem(slot, item);
                        }
                    } else {
                        for (int slot : slots) {
                            if (slot >= 0 && slot < size) {
                                inv.setItem(slot, item);
                            }
                        }
                    }
                }
            }
        }
        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), menuName);
        // Play Open Sound
        String sound = plugin.getConfig().getString("open-sound");
        if (sound != null && !sound.isEmpty()) {
            try {
                player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(sound), 1f, 1f);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private ItemStack createItem(Player player, ConfigurationSection section) {
        String matName = section.getString("material", "STONE");
        ItemStack item;
        // Handle Head Support
        if (matName.startsWith("[PLAYER] ")) {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            String playerName = replacePlaceholders(player, matName.substring(9));
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            item.setItemMeta(meta);
        } else if (matName.startsWith("[BASE64] ")) {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            String base64 = matName.substring(9);
            applyBase64Texture(meta, base64);
            item.setItemMeta(meta);
        } else {
            Material mat = Material.matchMaterial(matName);
            if (mat == null) mat = Material.STONE;
            item = new ItemStack(mat);
        }
        ItemMeta meta = item.getItemMeta();
        String displayName = section.getString("name");
        if (displayName != null) {
            meta.displayName(mm.deserialize(replacePlaceholders(player, displayName)));
        }
        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(mm.deserialize(replacePlaceholders(player, line)));
            }
            meta.lore(loreComponents);
        }
        if (section.contains("model-data")) {
            meta.setCustomModelData(section.getInt("model-data"));
        }
        // Enchantments/Flags could be added here
        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(Player player, String text) {
        // Replace args first: %arg_key%
        Map<String, String> args = playerContext.get(player.getUniqueId());
        if (args != null) {
            for (Map.Entry<String, String> entry : args.entrySet()) {
                text = text.replace("%arg_" + entry.getKey() + "%", entry.getValue());
            }
        }
        // Then PAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    private void applyBase64Texture(SkullMeta meta, String base64) {
        try {
            UUID uuid = UUID.randomUUID();
            // Get GameProfile class
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            // Create GameProfile instance
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, "");
            // Get PropertyMap
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            // Create Property instance ("textures", base64)
            Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", base64);
            // Add property to map (it's a ForwardingMultimap usually, put method works)
            // But usually we can just call put(key, value)
            java.lang.reflect.Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
            putMethod.invoke(properties, "textures", property);
            // Set profile field in SkullMeta
            java.lang.reflect.Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply Base64 texture: " + e.getMessage());
        }
    }

    public String getOpenedMenu(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        openMenus.remove(player.getUniqueId());
        playerContext.remove(player.getUniqueId());
        // Play Close Sound (only if they are truly closing, not switching menus? 
        // Logic is tricky here because openMenu calls closeInventory implicitly.
        // We'll leave it to InventoryListener or handle it simply.)
    }

    public void closeAllMenus() {
        for (UUID uuid : openMenus.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.closeInventory();
        }
        openMenus.clear();
        playerContext.clear();
    }

    public Map<String, String> getContext(Player player) {
        return playerContext.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

}
