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

    public ItemStack getBindingItem(String menuName) {
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        if (config == null || !config.contains("item-binding")) return null;
        ConfigurationSection section = config.getConfigurationSection("item-binding");
        if (section == null) return null;
        return createItem(null, section, menuName);
    }

    private ItemStack createItem(Player player, ConfigurationSection section) {
        return createItem(player, section, null);
    }

    private ItemStack createItem(Player player, ConfigurationSection section, String menuId) {
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
        if (section.getBoolean("enchanted", false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        if (menuId != null) {
            meta.getPersistentDataContainer().set(plugin.getBindingKey(), org.bukkit.persistence.PersistentDataType.STRING, menuId);
        }
        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        if (player == null) return text;

        // Replace args first: %arg_key%
        Map<String, String> args = playerContext.get(player.getUniqueId());
        if (args != null) {
            for (Map.Entry<String, String> entry : args.entrySet()) {
                text = text.replace("%arg_" + entry.getKey() + "%", entry.getValue());
            }
        }

        // Built-in player placeholders (fallback if PAPI not available or Player expansion missing)
        text = text.replace("%player_name%", player.getName());
        text = text.replace("%player_displayname%", net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(player.displayName()));
        text = text.replace("%player_uuid%", player.getUniqueId().toString());
        text = text.replace("%player_world%", player.getWorld().getName());
        text = text.replace("%player_x%", String.valueOf((int) player.getLocation().getX()));
        text = text.replace("%player_y%", String.valueOf((int) player.getLocation().getY()));
        text = text.replace("%player_z%", String.valueOf((int) player.getLocation().getZ()));
        text = text.replace("%player_health%", String.valueOf((int) player.getHealth()));
        text = text.replace("%player_max_health%", String.valueOf((int) player.getMaxHealth()));
        text = text.replace("%player_level%", String.valueOf(player.getLevel()));
        text = text.replace("%player_exp%", String.valueOf((int) (player.getExp() * 100)));
        text = text.replace("%player_food%", String.valueOf(player.getFoodLevel()));
        text = text.replace("%player_ping%", String.valueOf(player.getPing()));
        text = text.replace("%player_gamemode%", player.getGameMode().name());
        text = text.replace("%player_ip%", player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown");

        // Then PAPI (will override built-ins if expansions are installed)
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse PlaceholderAPI placeholders: " + e.getMessage());
            }
        }
        return text;
    }

    private void applyBase64Texture(SkullMeta meta, String base64) {
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), "");
            Object properties = null;
            try {
                properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            } catch (NoSuchMethodException e) {
                properties = gameProfileClass.getMethod("properties").invoke(profile);
            }
            if (properties != null) {
                java.lang.reflect.Method putMethod = properties.getClass().getMethod("put", Object.class, Object.class);
                Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", base64);
                putMethod.invoke(properties, "textures", property);
            }
            java.lang.reflect.Field profileField = null;
            Class<?> metaClass = meta.getClass();
            while (metaClass != null && profileField == null) {
                try {
                    profileField = metaClass.getDeclaredField("profile");
                } catch (NoSuchFieldException e) {
                    metaClass = metaClass.getSuperclass();
                }
            }
            if (profileField != null) {
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            }
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

    public void openMenuList(Player player) {
        Set<String> menuNames = plugin.getConfigManager().getMenuNames();
        List<String> sortedMenus = new ArrayList<>(menuNames);
        sortedMenus.sort(String.CASE_INSENSITIVE_ORDER);

        Inventory inv = Bukkit.createInventory(null, 54, Component.text("EIZZOs-GUIs Menu List"));

        // Fill with glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Add menu items
        int slot = 0;
        for (String menuName : sortedMenus) {
            if (slot >= 45) break; // Reserve bottom row

            FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
            String title = config != null ? config.getString("title", "N/A") : "N/A";
            int size = config != null ? config.getInt("size", 27) : 27;
            int rows = size / 9;

            ItemStack menuItem = new ItemStack(Material.CHEST);
            ItemMeta meta = menuItem.getItemMeta();
            meta.displayName(mm.deserialize("<yellow>" + menuName));

            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Title: <white>" + mm.stripTags(title)));
            lore.add(mm.deserialize("<gray>Size: <white>" + size + " <gray>(<white>" + rows + " rows<gray>)"));
            lore.add(Component.empty());

            if (player.hasPermission("eguis.admin")) {
                lore.add(mm.deserialize("<yellow>Left-Click: <white>Open Menu"));
                lore.add(mm.deserialize("<aqua>Right-Click: <white>Edit Menu"));
            } else {
                lore.add(mm.deserialize("<yellow>Click: <white>Open Menu"));
            }

            meta.lore(lore);
            menuItem.setItemMeta(meta);

            inv.setItem(slot++, menuItem);
        }

        // Add close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.displayName(mm.deserialize("<red>Close"));
        closeButton.setItemMeta(closeMeta);
        inv.setItem(53, closeButton);

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), "__LIST__");
    }

}
