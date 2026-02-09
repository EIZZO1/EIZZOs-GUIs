package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.utils.ChatUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
public class LoreEditorManager {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, LoreEditSession> sessions = new HashMap<>();
    public static class LoreEditSession {
        public final String menuName;
        public final int slot;
        public LoreEditSession(String menuName, int slot) {
            this.menuName = menuName;
            this.slot = slot;
        }
    }

    public LoreEditorManager(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    public void openLoreEditor(Player player, String menuName, int slot) {
        sessions.put(player.getUniqueId(), new LoreEditSession(menuName, slot));
        plugin.getEditorManager().getRefreshing().add(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, mm.deserialize("<black>Lore Editor (Slot " + slot + ")"));
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        List<String> lore = config.getStringList("items.slot_" + slot + ".lore");
        // List Lore Lines
        for (int i = 0; i < lore.size() && i < 45; i++) {
            String line = lore.get(i);
            inv.setItem(i, createLoreItem(line, i));
        }
        // Controls
        inv.setItem(49, createItem(Material.ARROW, "<red>Back", "<gray>Return to Slot Editor"));
        inv.setItem(51, createItem(Material.EMERALD, "<green>Add Line", "<gray>Click to add a new line"));
        inv.setItem(47, createItem(Material.BARRIER, "<red>Clear Lore", "<gray>Click to remove all lore"));
        player.openInventory(inv);
        plugin.getEditorManager().getRefreshing().remove(player.getUniqueId());
    }

    private ItemStack createLoreItem(String line, int index) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<!i>" + line));
        meta.lore(Arrays.asList(
            mm.deserialize("<!i><gray>Line: " + (index + 1)),
            mm.deserialize("<!i><yellow>Left-Click to Edit"),
            mm.deserialize("<!i><yellow>Right-Click to Remove")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<!i>" + name));
        if (lore != null) meta.lore(Collections.singletonList(mm.deserialize("<!i>" + lore)));
        item.setItemMeta(meta);
        return item;
    }

    public LoreEditSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void addLine(Player player, String line) {
        LoreEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        List<String> lore = config.getStringList("items.slot_" + session.slot + ".lore");
        if (line.equalsIgnoreCase("<none>")) {
             // Maybe add empty line? Or nothing?
             // User said "<none> for empty". Could mean empty string or clear.
             // If they type <none> in add, we probably add nothing.
        } else {
             lore.add(line);
        }
        config.set("items.slot_" + session.slot + ".lore", lore);
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openLoreEditor(player, session.menuName, session.slot);
    }

    public void editLine(Player player, int index, String newLine) {
        LoreEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        List<String> lore = config.getStringList("items.slot_" + session.slot + ".lore");
        if (index >= 0 && index < lore.size()) {
            if (newLine.equalsIgnoreCase("<none>")) {
                lore.remove(index);
            } else {
                lore.set(index, newLine);
            }
            config.set("items.slot_" + session.slot + ".lore", lore);
            plugin.getConfigManager().saveMenu(session.menuName, config);
        }
        openLoreEditor(player, session.menuName, session.slot);
    }

    public void removeLine(Player player, int index) {
        editLine(player, index, "<none>");
    }

    public void clearLore(Player player) {
        LoreEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        config.set("items.slot_" + session.slot + ".lore", null);
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openLoreEditor(player, session.menuName, session.slot);
    }

}

