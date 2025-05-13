package com.example.myanticheat.checks.movement;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.checks.Check;
import com.example.myanticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class JesusCheck extends Check {

    private static final String WATER_WALK_BUFFER_KEY = "jesus_consecutive_ticks";
    private static final int CONSECUTIVE_TICKS_THRESHOLD = 3; // Player needs to be on water for this many ticks
    private static final double MIN_WATER_DEPTH_FOR_CHECK = 0.5; // Minimum water depth to consider for this check

    public JesusCheck(MyAntiCheat plugin) {
        super(plugin, "jesus");
    }

    public void check(PlayerMoveEvent event) {
        if (!isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        Location to = event.getTo();
        Location from = event.getFrom();

        // 1. Basic Exemptions
        if (isExemptBasicJesus(player, playerData)) {
            playerData.resetViolationLevel(WATER_WALK_BUFFER_KEY);
            return;
        }

        Block blockAtFeet = to.getBlock();
        Block blockBelowFeet = blockAtFeet.getRelative(BlockFace.DOWN);
        boolean onWaterSurface = isWater(blockAtFeet) || isWater(blockBelowFeet);
        boolean isSubmerged = player.isInWater() || player.isSwimming();

        // 2. Specific Exemptions for Jesus Check
        if (isSubmerged || player.isFlying() || player.isGliding() || player.getVehicle() != null || hasFrostWalker(player) || isOnLilyPad(blockAtFeet) || isOnLilyPad(blockBelowFeet)) {
            playerData.resetViolationLevel(WATER_WALK_BUFFER_KEY);
            return;
        }
        
        // Check if player is on ground or was recently on ground (e.g. jumping out of water)
        if(player.isOnGround() || playerData.isLastOnGround()){
             playerData.resetViolationLevel(WATER_WALK_BUFFER_KEY);
             return;
        }

        // 3. Detection Logic
        if (onWaterSurface) {
            // Check depth: ensure water isn't too shallow (e.g. single layer over solid ground)
            // We also need to check the block *under* the water block to ensure it's also water or air
            Block blockBelowWater = blockBelowFeet.getRelative(BlockFace.DOWN);
            if (isWater(blockBelowFeet) && (isWater(blockBelowWater) || blockBelowWater.isPassable()) && blockBelowFeet.getY() < to.getY() - MIN_WATER_DEPTH_FOR_CHECK) {
                 // Player is on water surface, not sinking significantly, and water is reasonably deep
                double dY = to.getY() - from.getY();

                // If dY is slightly positive (jumping on water) or near zero (walking on water)
                // Allow small upward movement from water jump, but not sustained upward
                if (dY > -0.1) { // Not sinking much or even going up
                    int consecutiveTicks = playerData.getViolationLevel(WATER_WALK_BUFFER_KEY);
                    playerData.incrementViolationLevel(WATER_WALK_BUFFER_KEY, 1);

                    if (consecutiveTicks >= CONSECUTIVE_TICKS_THRESHOLD) {
                        flag(player, 1);
                        playerData.resetViolationLevel(WATER_WALK_BUFFER_KEY); // Reset after flagging
                    }
                    return; // Still on water, keep checking
                }
            }
        }
        // If not on water under suspicious conditions, reset buffer
        playerData.resetViolationLevel(WATER_WALK_BUFFER_KEY);
    }

    private boolean isExemptBasicJesus(Player player, PlayerData playerData) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return true;
        // Levitation is handled by isFlying() for Paper, but good to keep if logic changes
        // if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return true; 
        if (playerData.wasRecentlyTeleported()) return true;
        if (playerData.wasRecentlyVelocityChanged()) return true;
        return false;
    }

    private boolean isWater(Block block) {
        return block.getType() == Material.WATER;
    }

    private boolean isOnLilyPad(Block block) {
        return block.getType() == Material.LILY_PAD;
    }

    private boolean hasFrostWalker(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        return boots != null && boots.containsEnchantment(Enchantment.FROST_WALKER);
    }
} 