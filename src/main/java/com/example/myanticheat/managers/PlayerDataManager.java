package com.example.myanticheat.managers;

import com.example.myanticheat.MyAntiCheat;
import com.example.myanticheat.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager() {
        this.playerDataMap = new HashMap<>();
    }

    public PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> new PlayerData(uuid));
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void removePlayerData(UUID playerUUID) {
        playerDataMap.remove(playerUUID);
    }

    public void removePlayerData(Player player) {
        removePlayerData(player.getUniqueId());
    }

    public void decayAllViolationLevels() {
        playerDataMap.values().forEach(PlayerData::decayViolationLevels);
    }

    public void punishPlayer(Player player, String checkName) {
        int threshold = MyAntiCheat.getInstance().getCheckThreshold(checkName);
        PlayerData playerData = getPlayerData(player);
        
        if (playerData.getViolationLevel(checkName) >= threshold) {
            String punishCommand = MyAntiCheat.getInstance().getPunishmentCommand()
                    .replace("%player%", player.getName());
            
            Bukkit.getScheduler().runTask(MyAntiCheat.getInstance(), () -> 
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), punishCommand));
            
            // Log the punishment
            MyAntiCheat.getInstance().getLogger().info(
                    "Player " + player.getName() + " was punished for " + checkName + 
                    " with VL: " + playerData.getViolationLevel(checkName));
        }
    }
} 