package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.utils.ChatUtils;
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
import java.util.*;
public class SlotEditorManager {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, SlotEditSession> sessions = new HashMap<>();
    public static class SlotEditSession {
        public final String menuName;
        public final int slot;
        public SlotEditSession(String menuName, int slot) {
            this.menuName = menuName;
            this.slot = slot;
        }
    }

    public SlotEditorManager(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    public void openSlotEditor(Player player, String menuName, int slot) {
        clearSubSessions(player);
        sessions.put(player.getUniqueId(), new SlotEditSession(menuName, slot));
        plugin.getEditorManager().getRefreshing().add(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 54, mm.deserialize("<black>Editing Slot " + slot));
        // Get Config
        FileConfiguration config = plugin.getConfigManager().getMenu(menuName);
        String path = "items.slot_" + slot;
        ConfigurationSection section = config.getConfigurationSection(path);
        // Items
        if (section != null) {
            // Preview
            // We use GUIManager logic roughly, but we need to fetch the item manually since GUIManager opens menus
            ItemStack preview = createPreviewItem(section);
            inv.setItem(13, preview);
        } else {
             inv.setItem(13, new ItemStack(Material.BARRIER)); // No item yet
        }
        // Controls
        boolean enchanted = section != null && section.getBoolean("enchanted", false);
        inv.setItem(27, createControlItem(enchanted ? Material.ENCHANTED_BOOK : Material.BOOK, 
            "<yellow>Toggle Enchanted: " + (enchanted ? "<green>ON" : "<red>OFF"), 
            "<gray>Click to toggle enchantment glow"));
        inv.setItem(29, createControlItem(Material.NAME_TAG, "<yellow>Edit Name", "<gray>Click to rename item"));
        inv.setItem(31, createControlItem(Material.WRITABLE_BOOK, "<yellow>Edit Lore", "<gray>Click to edit lore"));
        inv.setItem(33, createControlItem(Material.COMMAND_BLOCK, "<yellow>Edit Actions", "<gray>Click to edit actions"));
        inv.setItem(35, createControlItem(Material.EMERALD, "<yellow>Edit Requirements", "<gray>Click to edit view/click requirements"));
        inv.setItem(49, createControlItem(Material.ARROW, "<red>Back", "<gray>Return to Menu Editor"));
        player.openInventory(inv);
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                plugin.getEditorManager().getRefreshing().remove(player.getUniqueId());
            }
        }.runTask(plugin);
    }

    private ItemStack createPreviewItem(ConfigurationSection section) {
        String matName = section.getString("material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        String name = section.getString("name");
        if (name != null) meta.displayName(mm.deserialize(name));
        List<String> lore = section.getStringList("lore");
        List<Component> loreComp = new ArrayList<>();
        for (String l : lore) loreComp.add(mm.deserialize(l));
        meta.lore(loreComp);
        if (section.getBoolean("enchanted", false)) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createControlItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize("<!i>" + name));
        meta.lore(Collections.singletonList(mm.deserialize("<!i>" + lore)));
        item.setItemMeta(meta);
        return item;
    }

    public SlotEditSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void endSession(Player player) {
        sessions.remove(player.getUniqueId());
        clearSubSessions(player);
    }

    private void clearSubSessions(Player player) {
        plugin.getLoreEditorManager().endSession(player);
        plugin.getActionEditorManager().endSession(player);
        plugin.getRequirementEditorManager().endSession(player);
    }

    public void toggleEnchanted(Player player) {
        SlotEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        String path = "items.slot_" + session.slot + ".enchanted";
        boolean current = config.getBoolean(path, false);
        config.set(path, !current);
        plugin.getConfigManager().saveMenu(session.menuName, config);
        ChatUtils.sendMessage(player, "<green>Enchantment glow toggled " + (!current ? "ON" : "OFF") + ".");
        openSlotEditor(player, session.menuName, session.slot);
    }

    public void handleRename(Player player, String newName) {
        SlotEditSession session = getSession(player);
        if (session == null) return;
        FileConfiguration config = plugin.getConfigManager().getMenu(session.menuName);
        if (newName.equalsIgnoreCase("<none>")) {
            config.set("items.slot_" + session.slot + ".name", null);
            ChatUtils.sendMessage(player, "<green>Name removed.");
        } else {
            config.set("items.slot_" + session.slot + ".name", newName);
            ChatUtils.sendMessage(player, "<green>Name updated!");
        }
        plugin.getConfigManager().saveMenu(session.menuName, config);
        openSlotEditor(player, session.menuName, session.slot);
    }

}

