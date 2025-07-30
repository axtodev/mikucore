package com.mikucore;

import com.mikucore.commands.*;
import com.mikucore.listeners.ChatListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class MikuCore extends JavaPlugin {
    private static MikuCore instance;
    private final HashMap<UUID, Long> playtime = new HashMap<>();
    private final HashMap<UUID, UUID> lastMessenger = new HashMap<>();
    private StaffChatCommand staffChatCommand;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Register commands
        getCommand("playtime").setExecutor(new PlaytimeCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("msg").setExecutor(new MessageCommand(this));
        getCommand("r").setExecutor(new ReplyCommand(this));
        this.staffChatCommand = new StaffChatCommand(this);
        getCommand("staffchat").setExecutor(staffChatCommand);
        getCommand("sc").setExecutor(staffChatCommand);
        getCommand("announce").setExecutor(new AnnounceCommand());
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // Start playtime tracking
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (var entry : playtime.entrySet()) {
                playtime.put(entry.getKey(), entry.getValue() + 1);
            }
        }, 20L, 20L);
        
        getLogger().info("MikuCore has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MikuCore has been disabled!");
    }

    public static MikuCore getInstance() {
        return instance;
    }

    public HashMap<UUID, Long> getPlaytime() {
        return playtime;
    }

    public HashMap<UUID, UUID> getLastMessenger() {
        return lastMessenger;
    }
    
    public StaffChatCommand getStaffChatCommand() {
        return staffChatCommand;
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        // Notify any components that need to reload their config
        if (staffChatCommand != null) {
            // If StaffChatCommand needs to reload config, add a method there
        }
    }
}
