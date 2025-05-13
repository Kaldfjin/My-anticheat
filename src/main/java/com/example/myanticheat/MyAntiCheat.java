package com.example.myanticheat;

import com.example.myanticheat.commands.ReloadCommand;
import com.example.myanticheat.listeners.MovementListener;
import com.example.myanticheat.managers.PlayerDataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class MyAntiCheat extends JavaPlugin {
    
    private static MyAntiCheat instance;
    private PlayerDataManager playerDataManager;
    private FileConfiguration config;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        config = getConfig();
        
        // Initialize managers
        playerDataManager = new PlayerDataManager();
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        
        // Register commands
        Objects.requireNonNull(getCommand("myanticheat")).setExecutor(new ReloadCommand(this));
        
        // Start violation decay task
        startViolationDecayTask();
        
        getLogger().info("MyAntiCheat has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MyAntiCheat has been disabled!");
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        getLogger().info("MyAntiCheat configuration reloaded!");
    }
    
    private void startViolationDecayTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerDataManager.decayAllViolationLevels();
            }
        }.runTaskTimer(this, 20 * 60, 20 * 60); // Run every minute (20 ticks * 60 seconds)
    }
    
    public static MyAntiCheat getInstance() {
        return instance;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public boolean isCheckEnabled(String checkName) {
        return config.getBoolean("checks." + checkName + ".enabled", true);
    }
    
    public int getCheckThreshold(String checkName) {
        return config.getInt("checks." + checkName + ".threshold", 10);
    }
    
    public String getPunishmentCommand() {
        return config.getString("punishment-command", "kick %player% [AntiCheat] Unfair Advantage");
    }
} 