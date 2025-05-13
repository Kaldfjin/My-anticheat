package com.example.myanticheat.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUUID;
    private final Map<String, Integer> violationLevels;
    private Location lastLocation;
    private boolean lastOnGround;
    private long lastViolationTime;
    private long lastTeleportTime;
    private long lastVelocityTime;
    private float lastFallDistance;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.violationLevels = new HashMap<>();
        this.lastViolationTime = 0;
        this.lastTeleportTime = 0;
        this.lastVelocityTime = 0;
        this.lastFallDistance = 0;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getViolationLevel(String checkName) {
        return violationLevels.getOrDefault(checkName, 0);
    }

    public void incrementViolationLevel(String checkName, int amount) {
        int currentLevel = getViolationLevel(checkName);
        violationLevels.put(checkName, currentLevel + amount);
        lastViolationTime = System.currentTimeMillis();
    }

    public void resetViolationLevel(String checkName) {
        violationLevels.put(checkName, 0);
    }

    public void decayViolationLevels() {
        // Only decay if no violations in the last minute
        if (System.currentTimeMillis() - lastViolationTime > 60000) {
            for (String checkName : violationLevels.keySet()) {
                int currentLevel = violationLevels.get(checkName);
                if (currentLevel > 0) {
                    violationLevels.put(checkName, currentLevel - 1);
                }
            }
        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public boolean isLastOnGround() {
        return lastOnGround;
    }

    public void setLastOnGround(boolean lastOnGround) {
        this.lastOnGround = lastOnGround;
    }

    public long getLastViolationTime() {
        return lastViolationTime;
    }

    public long getLastTeleportTime() {
        return lastTeleportTime;
    }

    public void setLastTeleportTime(long lastTeleportTime) {
        this.lastTeleportTime = lastTeleportTime;
    }

    public long getLastVelocityTime() {
        return lastVelocityTime;
    }

    public void setLastVelocityTime(long lastVelocityTime) {
        this.lastVelocityTime = lastVelocityTime;
    }
    
    public float getLastFallDistance() {
        return lastFallDistance;
    }
    
    public void setLastFallDistance(float lastFallDistance) {
        this.lastFallDistance = lastFallDistance;
    }
    
    public boolean wasRecentlyTeleported() {
        return System.currentTimeMillis() - lastTeleportTime < 5000; // Within 5 seconds
    }
    
    public boolean wasRecentlyVelocityChanged() {
        return System.currentTimeMillis() - lastVelocityTime < 3000; // Within 3 seconds
    }
} 