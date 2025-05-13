package com.example.myanticheat.checks.movement;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.checks.Check;
import com.example.myanticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class SpeedCheck extends Check {

    // Base movement speeds (blocks per tick)
    private static final double BASE_WALK_SPEED = 0.21;
    private static final double BASE_SPRINT_SPEED = 0.28;
    private static final double ICE_MULTIPLIER = 1.3;
    private static final double SOUL_SAND_MULTIPLIER = 0.4;
    private static final double SPEED_EFFECT_MULTIPLIER = 0.2;
    
    // Allow a small buffer for server lag/calculation errors
    private static final double THRESHOLD_BUFFER = 0.05;
    
    // Consecutive violations needed before flagging
    private static final int CONSECUTIVE_VIOLATIONS_REQUIRED = 3;
    
    private static final String SPEED_BUFFER_KEY = "speed_consecutive_violations";
    
    public SpeedCheck(MyAntiCheat plugin) {
        super(plugin, "speed");
    }
    
    public void check(PlayerMoveEvent event) {
        if (!isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Skip if player is exempt from basic checks
        if (isExemptBasic(player)) {
            return;
        }
        
        // Skip if player is in a vehicle
        if (player.isInsideVehicle()) {
            return;
        }
        
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Only check horizontal movement
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        
        // Skip if player didn't move horizontally
        if (horizontalDistance < 0.01) {
            return;
        }
        
        // Calculate maximum allowed speed
        double maxSpeed = calculateMaxAllowedSpeed(player, from);
        
        // Add buffer for server lag
        maxSpeed += THRESHOLD_BUFFER;
        
        // If player is moving faster than allowed
        if (horizontalDistance > maxSpeed) {
            // Get consecutive violations count
            int consecutiveViolations = playerData.getViolationLevel(SPEED_BUFFER_KEY);
            playerData.incrementViolationLevel(SPEED_BUFFER_KEY, 1);
            
            // Only flag after consecutive violations to reduce false positives
            if (consecutiveViolations >= CONSECUTIVE_VIOLATIONS_REQUIRED) {
                flag(player, 1);
                playerData.resetViolationLevel(SPEED_BUFFER_KEY); // Reset consecutive counter
            }
        } else {
            // Reset consecutive violations if player is not speeding
            playerData.resetViolationLevel(SPEED_BUFFER_KEY);
        }
        
        // Store current location for next check
        playerData.setLastLocation(to.clone());
    }
    
    private double calculateMaxAllowedSpeed(Player player, Location location) {
        double baseSpeed = player.isSprinting() ? BASE_SPRINT_SPEED : BASE_WALK_SPEED;
        
        // Reduce speed if sneaking
        if (player.isSneaking()) {
            baseSpeed *= 0.3;
        }
        
        // Check for speed effect
        int speedEffectLevel = getPotionEffectLevel(player, PotionEffectType.SPEED);
        if (speedEffectLevel > 0) {
            baseSpeed += (SPEED_EFFECT_MULTIPLIER * speedEffectLevel);
        }
        
        // Check for slowness effect
        int slownessEffectLevel = getPotionEffectLevel(player, PotionEffectType.SLOW);
        if (slownessEffectLevel > 0) {
            baseSpeed *= (1.0 - (0.15 * slownessEffectLevel));
        }
        
        // Check the block the player is walking on
        Block blockBelow = location.clone().subtract(0, 0.1, 0).getBlock();
        Material blockType = blockBelow.getType();
        
        // Speed modifier for ice blocks
        if (blockType == Material.ICE || blockType == Material.PACKED_ICE || 
            blockType == Material.BLUE_ICE || blockType == Material.FROSTED_ICE) {
            baseSpeed *= ICE_MULTIPLIER;
        }
        
        // Speed modifier for soul sand
        if (blockType == Material.SOUL_SAND || blockType == Material.SOUL_SOIL) {
            baseSpeed *= SOUL_SAND_MULTIPLIER;
        }
        
        return baseSpeed;
    }
} 