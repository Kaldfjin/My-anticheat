package com.example.myanticheat.checks.movement;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.checks.Check;
import com.example.myanticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

public class NoFallCheck extends Check {

    private static final Set<Material> FALL_DAMAGE_NEGATING_MATERIALS = new HashSet<>();
    private static final float MIN_FALL_DISTANCE = 3.5f;
    
    static {
        // Initialize materials that negate fall damage
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.WATER);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.LAVA);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.COBWEB);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.HAY_BLOCK);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.SLIME_BLOCK);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.HONEY_BLOCK);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.POWDER_SNOW);
        
        // Add all bed blocks
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.WHITE_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.ORANGE_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.MAGENTA_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.LIGHT_BLUE_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.YELLOW_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.LIME_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.PINK_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.GRAY_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.LIGHT_GRAY_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.CYAN_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.PURPLE_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.BLUE_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.BROWN_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.GREEN_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.RED_BED);
        FALL_DAMAGE_NEGATING_MATERIALS.add(Material.BLACK_BED);
    }
    
    public NoFallCheck(MyAntiCheat plugin) {
        super(plugin, "nofall");
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
        
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        float currentFallDistance = player.getFallDistance();
        boolean wasOnGround = playerData.isLastOnGround();
        boolean isNowOnGround = player.isOnGround();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Store current values for next check
        playerData.setLastFallDistance(currentFallDistance);
        playerData.setLastOnGround(isNowOnGround);
        playerData.setLastLocation(to.clone());
        
        // Check if player is landing (was not on ground, now is on ground)
        if (!wasOnGround && isNowOnGround && to.getY() < from.getY()) {
            // Player has landed - check if they should have taken fall damage
            if (currentFallDistance > MIN_FALL_DISTANCE) {
                // Check if the landing block or block below negates fall damage
                Block landingBlock = to.getBlock();
                Block blockBelow = to.clone().subtract(0, 0.1, 0).getBlock();
                
                if (!isLandingSafeBlock(landingBlock) && !isLandingSafeBlock(blockBelow)) {
                    // Player landed on a non-safe block with significant fall distance
                    // but didn't take damage - this is suspicious
                    flag(player, 1);
                }
            }
        }
    }
    
    private boolean isLandingSafeBlock(Block block) {
        return FALL_DAMAGE_NEGATING_MATERIALS.contains(block.getType());
    }
} 