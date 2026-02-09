package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.utils.ChatUtils;
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
public class RequirementEditorManager {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, RequirementEditSession> sessions = new HashMap<>();
    public static class RequirementEditSession {
        public final String menuName;
        public final int slot;
        public RequirementEditSession(String menuName, int slot) {
            this.menuName = menuName;
            this.slot = slot;
        }
    }

    public RequirementEditorManager(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    public void openRequirementEditor(Player player, String menuName, int slot) {
        sessions.put(player.getUniqueId(), new RequirementEditSession(menuName, slot));
        plugin.getEditorManager().getRefreshing().add(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 36, mm.deserialize("<black>Requirements (Slot " + slot + ")"));
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        String path = "items.slot_" + slot;
        // Helper to get value or "None"
        String moneyReq = String.valueOf(config.getDouble(path + ".click_requirement.money", 0));
        String permReq = config.getString(path + ".view_requirement.permission", "None");
        String tokenId = config.getString(path + ".click_requirement.token-id", "Default");
        inv.setItem(11, createItem(Material.GOLD_INGOT, "<yellow>Money Cost", 
            Arrays.asList("<gray>Current: " + moneyReq, "<gray>Click to set cost")));
        inv.setItem(13, createItem(Material.PAPER, "<yellow>View Permission", 
            Arrays.asList("<gray>Current: " + permReq, "<gray>Click to set permission")));
        if (plugin.getConfig().getBoolean("use-tokens")) {
            inv.setItem(15, createItem(Material.SUNFLOWER, "<yellow>Token ID", 
                Arrays.asList("<gray>Current: " + tokenId, "<gray>Click to set Token ID for this item")));
        }
        inv.setItem(31, createItem(Material.ARROW, "<red>Back", Collections.singletonList("<gray>Return to Slot Editor")));
        player.openInventory(inv);
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                plugin.getEditorManager().getRefreshing().remove(player.getUniqueId());
            }
        }.runTask(plugin);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<!i>" + name));
        if (lore != null) {
            List<net.kyori.adventure.text.Component> l = new ArrayList<>();
            for (String s : lore) l.add(mm.deserialize("<!i>" + s));
            meta.lore(l);
        }
        item.setItemMeta(meta);
        return item;
    }

    public RequirementEditSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void endSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public void setMoney(Player player, double amount) {
        RequirementEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        if (amount <= 0) {
            config.set("items.slot_" + session.slot + ".click_requirement.money", null);
        } else {
            config.set("items.slot_" + session.slot + ".click_requirement.money", amount);
        }
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openRequirementEditor(player, session.menuName, session.slot);
    }

    public void setPermission(Player player, String perm) {
        RequirementEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        if (perm.equalsIgnoreCase("none")) {
            config.set("items.slot_" + session.slot + ".view_requirement.permission", null);
        } else {
            config.set("items.slot_" + session.slot + ".view_requirement.permission", perm);
        }
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openRequirementEditor(player, session.menuName, session.slot);
    }

    public void setTokenId(Player player, String tokenId) {
        RequirementEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        if (tokenId.equalsIgnoreCase("default") || tokenId.equalsIgnoreCase("none")) {
            config.set("items.slot_" + session.slot + ".click_requirement.token-id", null);
        } else {
            config.set("items.slot_" + session.slot + ".click_requirement.token-id", tokenId);
        }
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openRequirementEditor(player, session.menuName, session.slot);
    }

}

