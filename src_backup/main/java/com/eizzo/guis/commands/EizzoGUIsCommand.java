package com.eizzo.guis.commands;
import com.eizzo.guis.EizzoGUIs;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
public class EizzoGUIsCommand implements CommandExecutor {
    private final EizzoGUIs plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public EizzoGUIsCommand(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "Command Help:");
            com.eizzo.guis.utils.ChatUtils.sendHelpMessage(sender, "open <menu> [player] [args]", "Open a GUI menu");
            com.eizzo.guis.utils.ChatUtils.sendHelpMessage(sender, "edit <menu>", "Edit a GUI menu");
            com.eizzo.guis.utils.ChatUtils.sendHelpMessage(sender, "create <name> <rows>", "Create a new menu");
            com.eizzo.guis.utils.ChatUtils.sendHelpMessage(sender, "delete <menu>", "Delete a menu");
            com.eizzo.guis.utils.ChatUtils.sendHelpMessage(sender, "help", "Show this help message");
            return true;
        }
        if (args[0].equalsIgnoreCase("open")) {
            // /eguis open <menu> [player]
            if (args.length < 2) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Usage: /eguis open <menu> [player]");
                return true;
            }
            String menuName = args[1];
            Player target;
            String argsString = null;
            if (args.length >= 3) {
                // Check if arg 2 is a player or args
                Player potentialTarget = Bukkit.getPlayer(args[2]);
                if (potentialTarget != null) {
                    // It's a player
                    if (!sender.hasPermission("eguis.open.others")) {
                        com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>You do not have permission to open menus for others.");
                        return true;
                    }
                    target = potentialTarget;
                    // If there is a 4th argument, it's the context
                    if (args.length >= 4) {
                        argsString = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                    }
                } else {
                    // It's probably args, assuming sender is player
                    if (!(sender instanceof Player)) {
                        com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Player not found: " + args[2]);
                        return true;
                    }
                    target = (Player) sender;
                    argsString = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                }
            }
            else {
                if (!(sender instanceof Player)) {
                    com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Console must specify a player.");
                    return true;
                }
                target = (Player) sender;
            }
            java.util.Map<String, String> context = new java.util.HashMap<>();
            if (argsString != null && argsString.startsWith("{") && argsString.endsWith("}")) {
                // Simple parser: remove braces, split by comma
                String content = argsString.substring(1, argsString.length() - 1);
                String[] pairs = content.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":");
                    if (kv.length == 2) {
                        context.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }
            plugin.getGuiManager().openMenu(target, menuName, context);
            return true;
        }
        if (args[0].equalsIgnoreCase("edit")) {
            // /eguis edit <menu>
            if (!(sender instanceof Player)) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Only players can edit menus.");
                return true;
            }
            if (!sender.hasPermission("eguis.edit")) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>You do not have permission to edit menus.");
                return true;
            }
            if (args.length < 2) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Usage: /eguis edit <menu>");
                return true;
            }
            plugin.getEditorManager().openEditor((Player) sender, args[1]);
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            // /eguis create <name> <rows>
            if (!(sender instanceof Player)) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Only players can create menus.");
                return true;
            }
            if (!sender.hasPermission("eguis.create")) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>You do not have permission to create menus.");
                return true;
            }
            if (args.length < 3) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Usage: /eguis create <name> <rows>");
                return true;
            }
            try {
                int rows = Integer.parseInt(args[2]);
                plugin.getEditorManager().createMenu((Player) sender, args[1], rows);
            } catch (NumberFormatException e) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Rows must be a number.");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("delete")) {
            // /eguis delete <menu>
            if (!sender.hasPermission("eguis.delete")) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>You do not have permission to delete menus.");
                return true;
            }
            if (args.length < 2) {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Usage: /eguis delete <menu>");
                return true;
            }
            String menuName = args[1];
            java.io.File file = new java.io.File(plugin.getDataFolder(), "menus/" + menuName + ".yml");
            if (file.exists()) {
                if (file.delete()) {
                    plugin.getConfigManager().loadMenus(); // Reload cache
                    com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<green>Menu '" + menuName + "' deleted successfully.");
                } else {
                    com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Failed to delete menu file.");
                }
            } else {
                com.eizzo.guis.utils.ChatUtils.sendMessage(sender, "<red>Menu not found.");
            }
            return true;
        }
        return true;
    }

}

