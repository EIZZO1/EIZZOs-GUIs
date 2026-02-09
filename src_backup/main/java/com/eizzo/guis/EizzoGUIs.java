package com.eizzo.guis;
import com.eizzo.guis.managers.ConfigManager;
import com.eizzo.guis.managers.GUIManager;
import com.eizzo.guis.commands.EizzoGUIsCommand;
import com.eizzo.guis.commands.EizzoGUIsTabCompleter;
import com.eizzo.guis.listeners.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;
public class EizzoGUIs extends JavaPlugin {
    private static EizzoGUIs instance;
    private ConfigManager configManager;
    private GUIManager guiManager;
    private com.eizzo.guis.managers.EditorManager editorManager;
    private com.eizzo.guis.managers.RequirementManager requirementManager;
    private com.eizzo.guis.managers.ChatInputManager chatInputManager;
    private com.eizzo.guis.managers.SlotEditorManager slotEditorManager;
    private com.eizzo.guis.managers.ActionEditorManager actionEditorManager;
    private com.eizzo.guis.managers.LoreEditorManager loreEditorManager;
    private com.eizzo.guis.managers.RequirementEditorManager requirementEditorManager;
    @Override
    public void onEnable() {
        instance = this;
        // Initialize Managers
        this.configManager = new ConfigManager(this);
        this.requirementManager = new com.eizzo.guis.managers.RequirementManager(this);
        this.guiManager = new GUIManager(this);
        this.editorManager = new com.eizzo.guis.managers.EditorManager(this);
        this.chatInputManager = new com.eizzo.guis.managers.ChatInputManager(this);
        this.slotEditorManager = new com.eizzo.guis.managers.SlotEditorManager(this);
        this.actionEditorManager = new com.eizzo.guis.managers.ActionEditorManager(this);
        this.loreEditorManager = new com.eizzo.guis.managers.LoreEditorManager(this);
        this.requirementEditorManager = new com.eizzo.guis.managers.RequirementEditorManager(this);
        // Load Configuration
        this.configManager.loadConfig();
        this.configManager.loadMenus();
        // Register Commands
        getCommand("eguis").setExecutor(new EizzoGUIsCommand(this));
        getCommand("eguis").setTabCompleter(new EizzoGUIsTabCompleter(this));
        // Register Listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new com.eizzo.guis.listeners.EditorListener(this), this);
        getServer().getPluginManager().registerEvents(new com.eizzo.guis.listeners.SlotEditorListener(this), this);
        getServer().getPluginManager().registerEvents(new com.eizzo.guis.listeners.ActionEditorListener(this), this);
        getServer().getPluginManager().registerEvents(new com.eizzo.guis.listeners.LoreEditorListener(this), this);
        getServer().getPluginManager().registerEvents(new com.eizzo.guis.listeners.RequirementEditorListener(this), this);
        // Start Tasks
        new com.eizzo.guis.tasks.EditorNotificationTask(this).runTaskTimer(this, 100L, 100L);
        getLogger().info("EIZZOs-GUIs has been enabled!");
    }

    @Override
    public void onDisable() {
        // Close all open GUIs to prevent item theft/dupes on reload
        if (guiManager != null) {
            guiManager.closeAllMenus();
        }
        getLogger().info("EIZZOs-GUIs has been disabled!");
    }

    public static EizzoGUIs get() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public com.eizzo.guis.managers.EditorManager getEditorManager() {
        return editorManager;
    }

    public com.eizzo.guis.managers.RequirementManager getRequirementManager() {
        return requirementManager;
    }

    public com.eizzo.guis.managers.ChatInputManager getChatInputManager() {
        return chatInputManager;
    }

    public com.eizzo.guis.managers.SlotEditorManager getSlotEditorManager() {
        return slotEditorManager;
    }

    public com.eizzo.guis.managers.ActionEditorManager getActionEditorManager() {
        return actionEditorManager;
    }

    public com.eizzo.guis.managers.LoreEditorManager getLoreEditorManager() {
        return loreEditorManager;
    }

    public com.eizzo.guis.managers.RequirementEditorManager getRequirementEditorManager() {
        return requirementEditorManager;
    }

}

