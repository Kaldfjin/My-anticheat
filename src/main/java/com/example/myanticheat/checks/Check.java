package com.example.myanticheat.checks;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.data.PlayerData;
import com.example.myanticheat.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public abstract class Check {
    
    protected final MyAntiCheat plugin;
    protected final String checkName;
    
    public Check(MyAntiCheat plugin, String checkName) {
        this.plugin = plugin;
        this.checkName = checkName;
    }
    
    public String getCheckName() {
        return checkName;
    }
    
    public boolean isEnabled() {
        return plugin.isCheckEnabled(checkName);
    }
    
    protected void flag(Player player, int violationAmount) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player);
        
        playerData.incrementViolationLevel(checkName, violationAmount);
        int currentVL = playerData.getViolationLevel(checkName);
        
        // Log the violation in console (if enabled in config, though not added as an option yet)
        if (plugin.getConfig().getBoolean("debug.log-violations", true)) {
            plugin.getLogger().info(
                "Player " + player.getName() + " failed " + checkName +
                " check with VL: " + currentVL);
        }

        // Send in-game chat notification
        if (plugin.getConfig().getBoolean("notifications.chat.enabled", true)) {
            String messageFormat = plugin.getConfig().getString("notifications.chat.format", "&8[&cAC&8] &f%player% &7failed &c%check% &7(VL: &f%vl%&7)");
            String notificationMessage = ChatColor.translateAlternateColorCodes('&', messageFormat
                .replace("%player%", player.getName())
                .replace("%check%", checkName)
                .replace("%vl%", String.valueOf(currentVL)));

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("myanticheat.notify")) {
                    staff.sendMessage(notificationMessage);
                }
            }
        }
        
        // Check if punishment is needed
        playerDataManager.punishPlayer(player, checkName);
    }
    
    protected boolean isExemptBasic(Player player) {
        // Exempt players in creative or spectator mode
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }
        
        // Exempt flying players
        if (player.isFlying() || player.isGliding()) {
            return true;
        }
        
        // Exempt players with relevant status effects
        if (player.hasPotionEffect(PotionEffectType.LEVITATION) || 
            player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            return true;
        }
        
        // Get player data
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Exempt recently teleported players
        if (playerData.wasRecentlyTeleported()) {
            return true;
        }
        
        // Exempt players that recently had their velocity changed
        if (playerData.wasRecentlyVelocityChanged()) {
            return true;
        }
        
        return false;
    }
    
    // Get potion effect level or 0 if not present
    protected int getPotionEffectLevel(Player player, PotionEffectType type) {
        return player.hasPotionEffect(type) 
                ? player.getPotionEffect(type).getAmplifier() + 1 
                : 0;
    }
} 