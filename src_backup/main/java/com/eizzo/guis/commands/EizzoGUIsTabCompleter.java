package com.eizzo.guis.commands;
import com.eizzo.guis.EizzoGUIs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
public class EizzoGUIsTabCompleter implements TabCompleter {
    private final EizzoGUIs plugin;
    public EizzoGUIsTabCompleter(EizzoGUIs plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("open", "edit", "create", "delete", "help");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete"))) {
            Set<String> menus = plugin.getConfigManager().getMenuNames();
            return new ArrayList<>(menus);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("open")) {
            return null; // Return null to default to online players
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
             return Arrays.asList("1", "2", "3", "4", "5", "6");
        }
        return Collections.emptyList();
    }

}

