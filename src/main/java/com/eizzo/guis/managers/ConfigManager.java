package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
public class ConfigManager {
    private final EizzoGUIs plugin;
    private final Map<String, FileConfiguration> menuConfigs = new HashMap<>();
    private final File menusFolder;
    public ConfigManager(EizzoGUIs plugin) {
        this.plugin = plugin;
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        // Check main config version
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            double version = config.getDouble("config-version", 1.0);
            if (version < 1.1) {
                plugin.getLogger().info("Updating config.yml to version 1.1...");
                config.options().copyDefaults(true);
                config.set("config-version", 1.1);
                // Ensure new keys exist in memory
                if (!config.contains("fail-sound")) config.set("fail-sound", "BLOCK_ANVIL_LAND");
                if (!config.contains("use-tokens")) config.set("use-tokens", false);
                if (!config.contains("token-id")) config.set("token-id", "tokens");
                plugin.saveConfig();
                // Reorder and comment
                try {
                    List<String> lines = Files.readAllLines(configFile.toPath());
                    List<String> orderedLines = new ArrayList<>();
                    orderedLines.add("config-version: 1.1");
                    orderedLines.add("");
                    orderedLines.add("# EIZZOs-GUIs Configuration");
                    orderedLines.add("# Define sounds, default settings, and more here.");
                    orderedLines.add("");
                    orderedLines.add("# Default sound for opening a GUI");
                    orderedLines.add("open-sound: " + config.getString("open-sound", "BLOCK_CHEST_OPEN"));
                    orderedLines.add("");
                    orderedLines.add("# Default sound for closing a GUI");
                    orderedLines.add("close-sound: " + config.getString("close-sound", "BLOCK_CHEST_CLOSE"));
                    orderedLines.add("");
                    orderedLines.add("# Default sound for clicking an item");
                    orderedLines.add("click-sound: " + config.getString("click-sound", "UI_BUTTON_CLICK"));
                    orderedLines.add("");
                    orderedLines.add("# Default sound for failing a requirement (insufficient funds, no permission)");
                    orderedLines.add("fail-sound: " + config.getString("fail-sound", "BLOCK_ANVIL_LAND"));
                    orderedLines.add("");
                    orderedLines.add("# Economy Settings");
                    orderedLines.add("# Use EIZZOs-Tokens for all 'money' requirements instead of Vault");
                    orderedLines.add("use-tokens: " + config.getBoolean("use-tokens", false));
                    orderedLines.add("# The token ID to use if use-tokens is true");
                    orderedLines.add("token-id: \"" + config.getString("token-id", "tokens") + "\"");
                    Files.write(configFile.toPath(), orderedLines);
                } catch (Exception e) {
                    plugin.getLogger().severe("Could not reorder config.yml: " + e.getMessage());
                }
            }
        }
        plugin.reloadConfig();
    }

    public void loadMenus() {
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }
        createDefaultMenu("example.yml");
        createDefaultMenu("showcase.yml");
        createDefaultMenu("server_selector.yml");
        menuConfigs.clear();
        File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                checkConfigVersion(file, "config-version", 1.0);
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                menuConfigs.put(file.getName().replace(".yml", ""), config);
            }
        }
        plugin.getLogger().info("Loaded " + menuConfigs.size() + " menus.");
    }

    private void createDefaultMenu(String fileName) {
        File file = new File(menusFolder, fileName);
        if (!file.exists()) {
            try {
                plugin.saveResource("menus/" + fileName, false);
            } catch (IllegalArgumentException e) {
                // Resource might not exist in jar yet, create a basic one
                try {
                    file.createNewFile();
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    config.set("config-version", 1.0);
                    if (fileName.equals("showcase.yml")) {
                        config.set("title", "<gradient:#ff0000:#0000ff>EIZZOs-GUIs Showcase</gradient>");
                        config.set("size", 45);
                        config.set("items.slot_4.slot", 4);
                        config.set("items.slot_4.material", "PAPER");
                        config.set("items.slot_4.name", "<gold>Welcome!");
                        config.set("items.slot_4.lore", Arrays.asList("<gray>This is a showcase menu.", "<gray>Explore the features below."));
                        config.set("items.slot_10.slot", 10);
                        config.set("items.slot_10.material", "DIAMOND_SWORD");
                        config.set("items.slot_10.name", "<red>OP Command Test");
                        config.set("items.slot_10.lore", Arrays.asList("<gray>Click to give yourself", "<gray>Creative Mode (as OP)."));
                        config.set("items.slot_10.actions", Arrays.asList("[op] gamemode creative %player_name%", "[message] <green>You are now in Creative Mode!"));
                        config.set("items.slot_12.slot", 12);
                        config.set("items.slot_12.material", "EMERALD");
                        config.set("items.slot_12.name", "<green>Economy Check");
                        config.set("items.slot_12.lore", Arrays.asList("<gray>Cost: $100", "<gray>Click to buy a diamond."));
                        config.set("items.slot_12.click_requirement.money", 100);
                        config.set("items.slot_12.actions", Arrays.asList("[console] give %player_name% diamond 1", "[message] <green>Purchased a diamond!"));
                        config.set("items.slot_14.slot", 14);
                        config.set("items.slot_14.material", "[PLAYER] EIZZO");
                        config.set("items.slot_14.name", "<aqua>Head Support");
                        config.set("items.slot_14.lore", Arrays.asList("<gray>This is EIZZO's head.", "<gray>Using [PLAYER] prefix."));
                        config.set("items.slot_16.slot", 16);
                        config.set("items.slot_16.material", "[BASE64] eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTIyODRlMTMyYmZkNjVjY2JkY2UwZmRkY2czOTc3NzllODYxODQ3NmU3YjRlYjRjZDAzZjExNTRmYmI3In19fQ==");
                        config.set("items.slot_16.name", "<light_purple>Base64 Head");
                        config.set("items.slot_16.lore", Arrays.asList("<gray>Custom texture via Base64.", "<gray>Click to say Hello."));
                        config.set("items.slot_16.actions", Collections.singletonList("[message] <light_purple>Hello World!"));
                        config.set("items.slot_22.slot", 22);
                        config.set("items.slot_22.material", "COMMAND_BLOCK");
                        config.set("items.slot_22.name", "<yellow>Sub-Menu");
                        config.set("items.slot_22.lore", Collections.singletonList("<gray>Click to open another menu."));
                        config.set("items.slot_22.actions", Collections.singletonList("[opengui] example"));
                        config.set("items.slot_30.slot", 30);
                        config.set("items.slot_30.material", "BARRIER");
                        config.set("items.slot_30.name", "<red>Restricted Item");
                        config.set("items.slot_30.lore", Arrays.asList("<gray>You need permission", "<gray>'eguis.admin' to see this."));
                        config.set("items.slot_30.view_requirement.permission", "eguis.admin");
                        config.set("items.slot_32.slot", 32);
                        config.set("items.slot_32.material", "BOOK");
                        config.set("items.slot_32.name", "<blue>PlaceholderAPI");
                        config.set("items.slot_32.lore", Arrays.asList("<gray>Your Name: %player_name%", "<gray>Your Ping: %player_ping%"));
                        config.set("items.close_button.slot", 40);
                        config.set("items.close_button.material", "BARRIER");
                        config.set("items.close_button.name", "<red>Close");
                        config.set("items.close_button.actions", Collections.singletonList("[close]"));
                    } else if (fileName.equals("server_selector.yml")) {
                        config.set("title", "<gradient:#55ff55:#ffff55>Server Selector</gradient>");
                        config.set("size", 27);
                        config.set("item-binding.material", "NETHER_STAR");
                        config.set("item-binding.name", "<yellow>Server Selector");
                        config.set("item-binding.lore", Collections.singletonList("<gray>Lore Server Selector"));
                        config.set("items.survival.slot", 13);
                        config.set("items.survival.material", "GRASS_BLOCK");
                        config.set("items.survival.name", "<green>Survival Server");
                        config.set("items.survival.lore", Collections.singletonList("<gray>Click to join the Survival server!"));
                        config.set("items.survival.actions", Arrays.asList("[op] server survival", "[message] <green>Sending you to Survival..."));
                        config.set("items.info.slot", 4);
                        config.set("items.info.material", "NETHER_STAR");
                        config.set("items.info.name", "<yellow>Server Selector");
                        config.set("items.info.lore", Collections.singletonList("<gray>This is the server selector."));
                        config.set("items.info.actions", Collections.singletonList("[message] <yellow>You are already in the selector!"));
                    } else {
                        config.set("title", "<blue>Example Menu");
                        config.set("size", 27);
                        config.set("items.close_button.slot", 13);
                        config.set("items.close_button.material", "BARRIER");
                        config.set("items.close_button.name", "<red>Close");
                        config.set("items.close_button.lore", Collections.singletonList("<gray>Click to close"));
                        config.set("items.close_button.actions", Collections.singletonList("[close]"));
                    }
                    config.save(file);
                } catch (IOException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Could not create default menu " + fileName, ex);
                }
            }
        }
    }

    private void checkConfigVersion(File file, String versionKey, double currentVersion) {
        // Simple check to ensure the file exists and has the version key at the top
        // For a full implementation, we would parse the lines and rewrite if needed
        // similar to EIZZOs-Spawners.
        // For now, we assume the user maintains it or we overwrite if missing entirely.
        if (!file.exists()) return;
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty() || !lines.get(0).startsWith(versionKey + ":")) {
                // In a real scenario, we would rewrite the file to put version at top
                // keeping comments and structure. For this prototype, we'll just warn.
                // Or we can prepend it.
                // Let's prepend it if it's completely missing from the file content check.
                boolean hasVersion = lines.stream().anyMatch(l -> l.trim().startsWith(versionKey + ":"));
                if (!hasVersion) {
                    List<String> newLines = new ArrayList<>();
                    newLines.add(versionKey + ": " + currentVersion);
                    newLines.addAll(lines);
                    Files.write(file.toPath(), newLines);
                    plugin.getLogger().info("Added version key to " + file.getName());
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to check version for " + file.getName());
        }
    }

    public FileConfiguration getMenu(String name) {
        return menuConfigs.get(name);
    }

    public Set<String> getMenuNames() {
        return menuConfigs.keySet();
    }

    public void reload() {
        loadConfig();
        loadMenus();
    }

    public void saveMenu(String name, FileConfiguration config) {
        File file = new File(menusFolder, name + ".yml");
        try {
            config.save(file);
            menuConfigs.put(name, config);
        } catch (IOException e) {
             plugin.getLogger().severe("Could not save menu " + name + ": " + e.getMessage());
        }
    }

}

