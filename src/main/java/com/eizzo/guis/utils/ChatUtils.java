package com.eizzo.guis.utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
public class ChatUtils {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String PREFIX = "<gradient:#55FF55:#00FF00><b>[EIZZOs-GUIs]</b></gradient> <gray>» </gray>";
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(mm.deserialize(PREFIX + "<white>" + message));
    }

    public static void sendHelpHeader(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gradient:#55FF55:#00FF00><b>--- EIZZOs-GUIs ---</b></gradient>"));
    }

    public static void sendHelpMessage(CommandSender sender, String command, String description) {
         sender.sendMessage(mm.deserialize("<gradient:#55FF55:#00FF00><b>»</b></gradient> <green>/eguis " + command + "</green> <gray>-- " + description));
    }

    public static Component format(String message) {
        return mm.deserialize(message);
    }

}
