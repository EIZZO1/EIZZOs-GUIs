package com.eizzo.guis.managers;
import com.eizzo.guis.EizzoGUIs;
import com.eizzo.tokens.EizzoTokens;
import com.eizzo.tokens.managers.TokenManager;
import com.eizzo.tokens.models.TokenType;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.List;
public class RequirementManager {
    private final EizzoGUIs plugin;
    private Economy econ = null;
    public RequirementManager(EizzoGUIs plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
            }
        }
    }

    public boolean checkViewRequirements(Player player, ConfigurationSection itemSection) {
        if (!itemSection.contains("view_requirement")) return true;
        return checkRequirements(player, itemSection.getConfigurationSection("view_requirement"));
    }

    public boolean checkClickRequirements(Player player, ConfigurationSection itemSection) {
        if (!itemSection.contains("click_requirement")) return true;
        return checkRequirements(player, itemSection.getConfigurationSection("click_requirement"));
    }

    private boolean checkRequirements(Player player, ConfigurationSection section) {
        if (section == null) return true;
        // Permission Check
        if (section.contains("permission")) {
            String perm = section.getString("permission");
            if (!player.hasPermission(perm)) return false;
        }
        // Global Economy Check (Money or Tokens override)
        if (section.contains("money")) {
            double amount = section.getDouble("money");
            boolean useTokens = plugin.getConfig().getBoolean("use-tokens", false);
            // Check for local token-id override
            String tokenId = section.getString("token-id");
            if (tokenId != null) {
                // If local token-id is set, force token usage regardless of global config
                useTokens = true;
            } else {
                tokenId = plugin.getConfig().getString("token-id", "tokens");
            }
            if (useTokens && Bukkit.getPluginManager().isPluginEnabled("EIZZOs-Tokens")) {
                TokenManager tm = EizzoTokens.get().getTokenManager();
                if (tm.getBalanceSync(player.getUniqueId(), tokenId) < amount) {
                    return false;
                }
            } else {
                if (econ != null && !econ.has(player, amount)) return false;
            }
        }
        // EIZZOs-Tokens Specific Check
        if (section.contains("tokens")) {
            if (!Bukkit.getPluginManager().isPluginEnabled("EIZZOs-Tokens")) return false;
            ConfigurationSection tokenSection = section.getConfigurationSection("tokens");
            if (tokenSection != null) {
                for (String tokenId : tokenSection.getKeys(false)) {
                    double amount = tokenSection.getDouble(tokenId);
                    TokenManager tm = EizzoTokens.get().getTokenManager();
                    if (tm.getBalanceSync(player.getUniqueId(), tokenId) < amount) {
                        return false;
                    }
                }
            }
        }
        // PlaceholderAPI Logic Checks
        if (section.contains("string_equals")) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                String input = section.getString("string_equals.input", "");
                String output = section.getString("string_equals.output", "");
                input = PlaceholderAPI.setPlaceholders(player, input);
                if (!input.equals(output)) return false;
            }
        }
        return true;
    }

    public void takeRequirements(Player player, ConfigurationSection section) {
        if (section == null) return;
        ConfigurationSection reqs = section.getConfigurationSection("click_requirement");
        if (reqs == null) return;
        // Take Money (or Tokens if override enabled)
        if (reqs.contains("money")) {
            double amount = reqs.getDouble("money");
            boolean useTokens = plugin.getConfig().getBoolean("use-tokens", false);
            // Check for local token-id override
            String tokenId = reqs.getString("token-id");
            if (tokenId != null) {
                useTokens = true;
            } else {
                tokenId = plugin.getConfig().getString("token-id", "tokens");
            }
            if (useTokens && Bukkit.getPluginManager().isPluginEnabled("EIZZOs-Tokens")) {
                TokenManager tm = EizzoTokens.get().getTokenManager();
                tm.removeBalance(player.getUniqueId(), tokenId, amount);
            } else {
                if (econ != null) {
                    econ.withdrawPlayer(player, amount);
                }
            }
        }
        // Take Tokens (Specific)
        if (reqs.contains("tokens") && Bukkit.getPluginManager().isPluginEnabled("EIZZOs-Tokens")) {
            ConfigurationSection tokenSection = reqs.getConfigurationSection("tokens");
            if (tokenSection != null) {
                for (String tokenId : tokenSection.getKeys(false)) {
                    double amount = tokenSection.getDouble(tokenId);
                    TokenManager tm = EizzoTokens.get().getTokenManager();
                    tm.removeBalance(player.getUniqueId(), tokenId, amount);
                }
            }
        }
    }

}

