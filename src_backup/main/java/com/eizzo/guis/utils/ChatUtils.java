package com.eizzo.guis.utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
public class ChatUtils {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(mm.deserialize("<gradient:#ffaa00:#ffff55><b>[EizzoGUIs]</b></gradient> <gray>" + message));
    }

    public static void sendHelpMessage(CommandSender sender, String command, String description) {
         sender.sendMessage(mm.deserialize("<dark_gray>» <yellow>/eguis " + command + " <dark_gray>- <gray>" + description));
    }

    public static Component format(String message) {
        return mm.deserialize(message);
    }

}
