package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.utils.ChatUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
public class ChatInputManager implements Listener {
    private final EizzoGUIs plugin;
    private final Map<UUID, Consumer<String>> pendingInputs = new HashMap<>();
    private final Map<UUID, Runnable> pendingCancels = new HashMap<>();
    public ChatInputManager(EizzoGUIs plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void requestInput(Player player, String prompt, Consumer<String> callback) {
        requestInput(player, prompt, callback, null);
    }

    public void requestInput(Player player, String prompt, Consumer<String> callback, Runnable onCancel) {
        ChatUtils.sendMessage(player, prompt);
        ChatUtils.sendMessage(player, "<gray>Type 'cancel' to abort.");
        pendingInputs.put(player.getUniqueId(), callback);
        if (onCancel != null) pendingCancels.put(player.getUniqueId(), onCancel);
        player.closeInventory();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (pendingInputs.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String message = LegacyComponentSerializer.legacyAmpersand().serialize(event.message());
            Consumer<String> callback = pendingInputs.remove(player.getUniqueId());
            Runnable onCancel = pendingCancels.remove(player.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (message.equalsIgnoreCase("cancel")) {
                        ChatUtils.sendMessage(player, "<red>Input cancelled.");
                        if (onCancel != null) onCancel.run();
                        return;
                    }
                    callback.accept(message);
                }
            }.runTask(plugin);
        }
    }

}

