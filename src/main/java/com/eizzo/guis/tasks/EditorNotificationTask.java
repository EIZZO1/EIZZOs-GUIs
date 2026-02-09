package com.eizzo.guis.tasks;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.UUID;
public class EditorNotificationTask extends BukkitRunnable {
    private final EizzoGUIs plugin;
    public EditorNotificationTask(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (UUID uuid : plugin.getEditorManager().getEditingPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // Verify they are not in the Slot Editor (which is a different view)
                // Although SlotEditorManager also uses refreshing/editing states, 
                // we might want to avoid spamming them if they are in the sub-menu.
                // But the user said "when the menu is open". 
                // The Slot Editor IS a menu.
                // However, the controls "Shift-Right to Set Icon" etc apply to the MAIN editor.
                // If they are in Slot Editor, they have buttons.
                if (plugin.getSlotEditorManager().getSession(player) != null) {
                    continue; // Skip if in Slot Editor
                }
                ChatUtils.sendMessage(player, "<gray>Editor Controls:");
                ChatUtils.sendMessage(player, "<gray>Left-Click <dark_gray>» <yellow>Edit Properties");
                ChatUtils.sendMessage(player, "<gray>Shift+Right-Click <dark_gray>» <yellow>Set Icon");
                ChatUtils.sendMessage(player, "<gray>Middle-Click <dark_gray>» <red>Delete Item");
            }
        }
    }

}

