package com.example.myanticheat.listeners;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.checks.movement.NoFallCheck;
import com.example.myanticheat.checks.movement.SpeedCheck;
import com.example.myanticheat.checks.movement.FlyCheck;
import com.example.myanticheat.checks.movement.JesusCheck;
import com.example.myanticheat.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

public class MovementListener implements Listener {

    private final MyAntiCheat plugin;
    private final NoFallCheck noFallCheck;
    private final SpeedCheck speedCheck;
    private final FlyCheck flyCheck;
    private final JesusCheck jesusCheck;
    
    public MovementListener(MyAntiCheat plugin) {
        this.plugin = plugin;
        this.noFallCheck = new NoFallCheck(plugin);
        this.speedCheck = new SpeedCheck(plugin);
        this.flyCheck = new FlyCheck(plugin);
        this.jesusCheck = new JesusCheck(plugin);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize player data for new players
        plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up player data when they leave
        plugin.getPlayerDataManager().removePlayerData(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Skip if no movement or only head rotation
        if (event.getFrom().getX() == event.getTo().getX() && 
            event.getFrom().getY() == event.getTo().getY() && 
            event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }
        
        // Run all movement-related checks
        noFallCheck.check(event);
        speedCheck.check(event);
        flyCheck.check(event);
        jesusCheck.check(event);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Update player's teleport time in data
        playerData.setLastTeleportTime(System.currentTimeMillis());
        
        // Update location after teleport to prevent false positives
        playerData.setLastLocation(event.getTo().clone());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Update player's velocity time in data
        playerData.setLastVelocityTime(System.currentTimeMillis());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        // If player takes damage due to explosion, fire, attack, etc.
        // mark them as having velocity changes to prevent false positives
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL &&
            event.getCause() != EntityDamageEvent.DamageCause.DROWNING &&
            event.getCause() != EntityDamageEvent.DamageCause.STARVATION) {
            
            Player player = (Player) event.getEntity();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            playerData.setLastVelocityTime(System.currentTimeMillis());
        }
    }
} 