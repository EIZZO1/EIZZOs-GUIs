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
public class ActionEditorManager {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, ActionEditSession> sessions = new HashMap<>();
    public static class ActionEditSession {
        public final String menuName;
        public final int slot;
        public ActionEditSession(String menuName, int slot) {
            this.menuName = menuName;
            this.slot = slot;
        }
    }

    public ActionEditorManager(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    public void openActionEditor(Player player, String menuName, int slot) {
        sessions.put(player.getUniqueId(), new ActionEditSession(menuName, slot));
        plugin.getEditorManager().getRefreshing().add(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, mm.deserialize("<black>Actions (Slot " + slot + ")"));
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        List<String> actions = config.getStringList("items.slot_" + slot + ".actions");
        // List Actions
        for (int i = 0; i < actions.size() && i < 45; i++) {
            String action = actions.get(i);
            inv.setItem(i, createActionItem(action, i));
        }
        // Controls
        inv.setItem(49, createItem(Material.ARROW, "<red>Back", "<gray>Return to Slot Editor"));
        inv.setItem(51, createItem(Material.EMERALD, "<green>Add Action", "<gray>Click to add a new action"));
        player.openInventory(inv);
        plugin.getEditorManager().getRefreshing().remove(player.getUniqueId());
    }

    public void openAddActionSelector(Player player) {
        plugin.getEditorManager().getRefreshing().add(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 27, mm.deserialize("<black>Select Action Type"));
        inv.setItem(10, createItem(Material.BARRIER, "<red>[close]", "<gray>Close the menu"));
        inv.setItem(11, createItem(Material.PAPER, "<yellow>[message]", "<gray>Send a message to player"));
        inv.setItem(12, createItem(Material.COMMAND_BLOCK, "<green>[player]", "<gray>Execute player command"));
        inv.setItem(13, createItem(Material.REPEATING_COMMAND_BLOCK, "<blue>[console]", "<gray>Execute console command"));
        inv.setItem(14, createItem(Material.REDSTONE_BLOCK, "<red>[op]", "<gray>Execute command as OP"));
        inv.setItem(15, createItem(Material.CHEST, "<gold>[opengui]", "<gray>Open another menu"));
        inv.setItem(22, createItem(Material.ARROW, "<red>Cancel", "<gray>Return to Action List"));
        player.openInventory(inv);
        plugin.getEditorManager().getRefreshing().remove(player.getUniqueId());
    }

    private ItemStack createActionItem(String action, int index) {
        Material mat = Material.PAPER;
        if (action.startsWith("[close]")) mat = Material.BARRIER;
        else if (action.startsWith("[player]")) mat = Material.COMMAND_BLOCK;
        else if (action.startsWith("[console]")) mat = Material.REPEATING_COMMAND_BLOCK;
        else if (action.startsWith("[op]")) mat = Material.REDSTONE_BLOCK;
        else if (action.startsWith("[opengui]")) mat = Material.CHEST;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<!i><white>" + action));
        meta.lore(Arrays.asList(
            mm.deserialize("<!i><gray>Index: " + index),
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

    public ActionEditSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void addAction(Player player, String action) {
        ActionEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        List<String> actions = config.getStringList("items.slot_" + session.slot + ".actions");
        actions.add(action);
        config.set("items.slot_" + session.slot + ".actions", actions);
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openActionEditor(player, session.menuName, session.slot);
        ChatUtils.sendMessage(player, "<green>Action added!");
    }

    public void removeAction(Player player, int index) {
        ActionEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        List<String> actions = config.getStringList("items.slot_" + session.slot + ".actions");
        if (index >= 0 && index < actions.size()) {
            actions.remove(index);
            config.set("items.slot_" + session.slot + ".actions", actions);
            plugin.getConfigManager().saveMenu(session.menuName, config);
            ChatUtils.sendMessage(player, "<green>Action removed.");
        }
        openActionEditor(player, session.menuName, session.slot);
    }

}

