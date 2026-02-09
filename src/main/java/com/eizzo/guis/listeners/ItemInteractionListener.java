package com.eizzo.guis.listeners;
import com.eizzo.guis.EizzoGUIs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
public class ItemInteractionListener implements Listener {
    private final EizzoGUIs plugin;
    public ItemInteractionListener(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        if (!item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        // Check PDC first (Secure ID)
        String menuId = meta.getPersistentDataContainer().get(plugin.getBindingKey(), org.bukkit.persistence.PersistentDataType.STRING);
        if (menuId != null) {
            if (plugin.getConfigManager().getMenu(menuId) != null) {
                event.setCancelled(true);
                plugin.getGuiManager().openMenu(event.getPlayer(), menuId);
                return;
            }
        }
        // We remove the Name/Lore check to satisfy "cannot be recreated in anvil" 
        // as anvils can only change name/lore, not PDC.
    }

}

