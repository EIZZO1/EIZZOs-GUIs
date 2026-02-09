package com.eizzo.guis.commands;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.guis.utils.ChatUtils;
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
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getGuiManager().openMenuList((Player) sender);
            } else {
                sendMenuList(sender);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            if (sender instanceof Player) {
                plugin.getGuiManager().openMenuList((Player) sender);
            } else {
                sendMenuList(sender);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("removebinding")) {
            if (!sender.hasPermission("eguis.admin")) {
                ChatUtils.sendMessage(sender, "<red>No permission.");
                return true;
            }
            if (args.length < 2) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis removebinding <menu>");
                return true;
            }
            String menuName = args[1];
            org.bukkit.configuration.file.FileConfiguration menuConfig = plugin.getConfigManager().getMenu(menuName);
            if (menuConfig == null) {
                ChatUtils.sendMessage(sender, "<red>Menu not found.");
                return true;
            }
            menuConfig.set("item-binding", null);
            plugin.getConfigManager().saveMenu(menuName, menuConfig);
            ChatUtils.sendMessage(sender, "<green>Removed binding for '" + menuName + "'.");
            return true;
        }
        if (args[0].equalsIgnoreCase("setbinding")) {
            if (!(sender instanceof Player)) {
                ChatUtils.sendMessage(sender, "<red>Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("eguis.admin")) {
                ChatUtils.sendMessage(sender, "<red>No permission.");
                return true;
            }
            if (args.length < 2) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis setbinding <menu>");
                return true;
            }
            String menuName = args[1];
            org.bukkit.configuration.file.FileConfiguration menuConfig = plugin.getConfigManager().getMenu(menuName);
            if (menuConfig == null) {
                ChatUtils.sendMessage(sender, "<red>Menu not found.");
                return true;
            }
            org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                ChatUtils.sendMessage(sender, "<red>You must be holding an item.");
                return true;
            }
            // Apply PDC to the item in hand so it works immediately
            org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(plugin.getBindingKey(), org.bukkit.persistence.PersistentDataType.STRING, menuName);
            item.setItemMeta(meta);
            // Update Config
            menuConfig.set("item-binding.material", item.getType().name());
            if (meta.hasDisplayName()) {
                menuConfig.set("item-binding.name", net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(meta.displayName()));
            }
            if (meta.hasLore()) {
                java.util.List<String> lore = new java.util.ArrayList<>();
                for (net.kyori.adventure.text.Component line : meta.lore()) {
                    lore.add(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().serialize(line));
                }
                menuConfig.set("item-binding.lore", lore);
            }
            plugin.getConfigManager().saveMenu(menuName, menuConfig);
            ChatUtils.sendMessage(sender, "<green>Item in hand set as binding for '" + menuName + "' and tagged with ID.");
            return true;
        }
        if (args[0].equalsIgnoreCase("giveitem")) {
            if (!sender.hasPermission("eguis.giveitem")) {
                ChatUtils.sendMessage(sender, "<red>You do not have permission to use this command.");
                return true;
            }
            if (args.length < 2) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis giveitem <menu> [player]");
                return true;
            }
            String menuName = args[1];
            Player target;
            if (args.length >= 3) {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    ChatUtils.sendMessage(sender, "<red>Player not found: " + args[2]);
                    return true;
                }
            } else {
                if (!(sender instanceof Player)) {
                    ChatUtils.sendMessage(sender, "<red>Console must specify a player.");
                    return true;
                }
                target = (Player) sender;
            }
            org.bukkit.inventory.ItemStack item = plugin.getGuiManager().getBindingItem(menuName);
            if (item == null) {
                ChatUtils.sendMessage(sender, "<red>Menu '" + menuName + "' does not have an item-binding.");
                return true;
            }
            target.getInventory().addItem(item);
            ChatUtils.sendMessage(sender, "<green>Gave binding item for '" + menuName + "' to " + target.getName() + ".");
            return true;
        }
        if (args[0].equalsIgnoreCase("open")) {
            // /eguis open <menu> [player]
            if (args.length < 2) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis open <menu> [player]");
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
                        ChatUtils.sendMessage(sender, "<red>You do not have permission to open menus for others.");
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
                        ChatUtils.sendMessage(sender, "<red>Player not found: " + args[2]);
                        return true;
                    }
                    target = (Player) sender;
                    argsString = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                }
            }
            else {
                if (!(sender instanceof Player)) {
                    ChatUtils.sendMessage(sender, "<red>Console must specify a player.");
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
                ChatUtils.sendMessage(sender, "<red>Only players can edit menus.");
                return true;
            }
            if (!sender.hasPermission("eguis.edit")) {
                ChatUtils.sendMessage(sender, "<red>You do not have permission to edit menus.");
                return true;
            }
            if (args.length < 2) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis edit <menu>");
                return true;
            }
            plugin.getEditorManager().openEditor((Player) sender, args[1]);
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            // /eguis create <name> <rows>
            if (!(sender instanceof Player)) {
                ChatUtils.sendMessage(sender, "<red>Only players can create menus.");
                return true;
            }
            if (!sender.hasPermission("eguis.create")) {
                ChatUtils.sendMessage(sender, "<red>You do not have permission to create menus.");
                return true;
            }
            if (args.length < 3) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis create <name> <rows>");
                return true;
            }
            try {
                int rows = Integer.parseInt(args[2]);
                plugin.getEditorManager().createMenu((Player) sender, args[1], rows);
            } catch (NumberFormatException e) {
                ChatUtils.sendMessage(sender, "<red>Rows must be a number.");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("delete")) {
            // /eguis delete <menu>
            if (!sender.hasPermission("eguis.delete")) {
                ChatUtils.sendMessage(sender, "<red>You do not have permission to delete menus.");
                return true;
            }
            if (args.length < 2) {
                ChatUtils.sendMessage(sender, "<red>Usage: /eguis delete <menu>");
                return true;
            }
            String menuName = args[1];
            java.io.File file = new java.io.File(plugin.getDataFolder(), "menus/" + menuName + ".yml");
            if (file.exists()) {
                if (file.delete()) {
                    plugin.getConfigManager().loadMenus(); // Reload cache
                    ChatUtils.sendMessage(sender, "<green>Menu '" + menuName + "' deleted successfully.");
                } else {
                    ChatUtils.sendMessage(sender, "<red>Failed to delete menu file.");
                }
            } else {
                ChatUtils.sendMessage(sender, "<red>Menu not found.");
            }
            return true;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        ChatUtils.sendHelpHeader(sender);
        if (sender.hasPermission("eguis.admin")) {
            ChatUtils.sendHelpMessage(sender, "list", "List all available menus");
            ChatUtils.sendHelpMessage(sender, "open <menu> [player] [args]", "Open a GUI menu");
            ChatUtils.sendHelpMessage(sender, "edit <menu>", "Edit a GUI menu");
            ChatUtils.sendHelpMessage(sender, "create <name> <rows>", "Create a new menu");
            ChatUtils.sendHelpMessage(sender, "delete <menu>", "Delete a menu");
            ChatUtils.sendHelpMessage(sender, "giveitem <menu> [player]", "Give the binding item for a menu");
            ChatUtils.sendHelpMessage(sender, "setbinding <menu>", "Set the item in hand as the binding for a menu");
            ChatUtils.sendHelpMessage(sender, "removebinding <menu>", "Remove the item binding for a menu");
        } else if (sender.hasPermission("eguis.giveitem")) {
            ChatUtils.sendHelpMessage(sender, "giveitem <menu> [player]", "Give the binding item for a menu");
        }
        ChatUtils.sendHelpMessage(sender, "help", "Show this help message");
    }

    private void sendMenuList(CommandSender sender) {
        java.util.Set<String> menuNames = plugin.getConfigManager().getMenuNames();

        ChatUtils.sendMessage(sender, "<gradient:#00D4FF:#0099FF><bold>--- EIZZOs-GUIs Menu List ---</bold></gradient>");

        if (menuNames.isEmpty()) {
            ChatUtils.sendMessage(sender, "<gray>No menus available.");
        } else {
            for (String menuName : menuNames) {
                org.bukkit.configuration.file.FileConfiguration menuConfig = plugin.getConfigManager().getMenu(menuName);
                String title = menuConfig != null ? menuConfig.getString("title", "N/A") : "N/A";
                ChatUtils.sendMessage(sender, "  <yellow>» <white>" + menuName + " <gray>- " + mm.stripTags(title));
            }
        }

        ChatUtils.sendMessage(sender, "");
        if (sender.hasPermission("eguis.admin")) {
            ChatUtils.sendMessage(sender, "<gray>Use <yellow>/eguis open <menu> <gray>to open a menu.");
            ChatUtils.sendMessage(sender, "<gray>Use <yellow>/eguis edit <menu> <gray>to edit a menu.");
        } else {
            ChatUtils.sendMessage(sender, "<gray>Use <yellow>/eguis open <menu> <gray>to open a menu.");
        }
        ChatUtils.sendMessage(sender, "<gray>Type <yellow>/eguis help <gray>for more commands.");
    }

}
