package com.example.myanticheat.checks.movement;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.checks.Check;
import com.example.myanticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class FlyCheck extends Check {

    // Maximum vertical speed allowed normally (blocks per tick) - Adjust as needed
    private static final double MAX_NORMAL_VERTICAL_SPEED = 0.5; // Higher for jumps, but needs more context
    // Allow a small buffer for calculations
    private static final double VERTICAL_BUFFER = 0.05;
    // How long a player can be 'in air' without flagging (ticks) - needs refinement
    private static final int MAX_AIR_TICKS_SUSPICIOUS = 20; // 1 second

    private static final String AIR_TICKS_KEY = "fly_air_ticks";
    private static final String CONSECUTIVE_UPWARD_KEY = "fly_consecutive_upward";
    private static final int CONSECUTIVE_UPWARD_THRESHOLD = 4;

    public FlyCheck(MyAntiCheat plugin) {
        super(plugin, "fly");
    }

    public void check(PlayerMoveEvent event) {
        if (!isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        Location from = event.getFrom();
        Location to = event.getTo();
        double dY = to.getY() - from.getY();

        // 1. Basic Exemptions
        if (isExemptBasicFly(player, playerData)) {
            resetCounters(playerData);
            return;
        }

        // 2. Environmental Exemptions & Ground Check
        boolean isOnGround = player.isOnGround();
        Block blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        boolean wasOnGround = playerData.isLastOnGround();

        if (isOnGround || wasOnGround || isOnBounceBlock(blockBelow) || isNearClimbable(player.getLocation())) {
             // Reset air ticks if on ground or bouncing
             playerData.resetViolationLevel(AIR_TICKS_KEY);
             playerData.resetViolationLevel(CONSECUTIVE_UPWARD_KEY);
             return;
        }

        // --- Player is airborne and not exempt ---

        // 3. Air Time Tracking
        int airTicks = playerData.getViolationLevel(AIR_TICKS_KEY);
        playerData.incrementViolationLevel(AIR_TICKS_KEY, 1);

        // 4. Upward Movement Check
        if (dY > VERTICAL_BUFFER) { // Player is moving upwards
            // Calculate expected max jump height/boosts (simplified here)
            double expectedMaxYSpeed = calculateMaxVerticalSpeed(player);

            if (dY > expectedMaxYSpeed + VERTICAL_BUFFER) {
                // Moving up faster than expected
                 int consecutiveUpward = playerData.getViolationLevel(CONSECUTIVE_UPWARD_KEY);
                 playerData.incrementViolationLevel(CONSECUTIVE_UPWARD_KEY, 1);
                 if (consecutiveUpward >= CONSECUTIVE_UPWARD_THRESHOLD) {
                     flag(player, 1);
                     playerData.resetViolationLevel(CONSECUTIVE_UPWARD_KEY); // Reset after flagging
                 }
            } else {
                 // Moved up normally, reset consecutive counter
                 playerData.resetViolationLevel(CONSECUTIVE_UPWARD_KEY);
            }
        } else {
             // Not moving up, reset consecutive counter
             playerData.resetViolationLevel(CONSECUTIVE_UPWARD_KEY);
        }

        // 5. Sustained Air Time Check (Basic)
        // This is a very basic check and prone to false positives (e.g., falling)
        // Needs more logic to differentiate between falling and hovering
        /*
        if (airTicks > MAX_AIR_TICKS_SUSPICIOUS && Math.abs(dY) < 0.01) {
             // Player is staying at roughly the same Y level while airborne for too long
             flag(player, 1);
             // Consider resetting air ticks after flagging to avoid spam
             // playerData.resetViolationLevel(AIR_TICKS_KEY);
        }
        */

        // Update player data (handled in NoFallCheck, maybe consolidate later)
        // playerData.setLastOnGround(isOnGround);
        // playerData.setLastLocation(to.clone());
    }

    private void resetCounters(PlayerData playerData) {
        playerData.resetViolationLevel(AIR_TICKS_KEY);
        playerData.resetViolationLevel(CONSECUTIVE_UPWARD_KEY);
    }

    private boolean isExemptBasicFly(Player player, PlayerData playerData) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return true;
        if (player.isFlying() || player.isGliding()) return true;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return true;
        if (playerData.wasRecentlyTeleported()) return true;
        if (playerData.wasRecentlyVelocityChanged()) return true; // Important for knockback/explosions
        if (player.getVehicle() != null) return true; // Exempt while in vehicles
        // Consider adding world checks (e.g., disabled worlds) if needed
        return false;
    }

     private boolean isOnBounceBlock(Block block) {
        Material type = block.getType();
        return type == Material.SLIME_BLOCK || type == Material.HONEY_BLOCK || type == Material.WHITE_BED || type == Material.ORANGE_BED || type == Material.MAGENTA_BED || type == Material.LIGHT_BLUE_BED || type == Material.YELLOW_BED || type == Material.LIME_BED || type == Material.PINK_BED || type == Material.GRAY_BED || type == Material.LIGHT_GRAY_BED || type == Material.CYAN_BED || type == Material.PURPLE_BED || type == Material.BLUE_BED || type == Material.BROWN_BED || type == Material.GREEN_BED || type == Material.RED_BED || type == Material.BLACK_BED;
    }

    private boolean isNearClimbable(Location loc) {
        // Check immediate surroundings for ladders, vines, etc.
        // This can be complex and impact performance, keeping it simple/disabled for now
        /*
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) { // Check slightly above/below too
                    Material type = loc.clone().add(x, y, z).getBlock().getType();
                    if (type == Material.LADDER || type == Material.VINE || type == Material.TWISTING_VINES || type == Material.WEEPING_VINES || type == Material.SCAFFOLDING || type == Material.COBWEB) {
                         return true;
                    }
                }
            }
        }
        */
        return false; // Keep disabled for now
    }

     private double calculateMaxVerticalSpeed(Player player) {
         // Simplified: Base jump speed + jump boost
         double baseJump = 0.42; // Vanilla jump speed
         int jumpBoostLevel = getPotionEffectLevel(player, PotionEffectType.JUMP);
         if (jumpBoostLevel > 0) {
             // Jump boost adds roughly 0.1 * level^2 + some base amount - Needs precise calculation
             // For simplicity, let's add a flat amount per level
             baseJump += 0.1 * jumpBoostLevel;
         }
         return baseJump;
     }
} 