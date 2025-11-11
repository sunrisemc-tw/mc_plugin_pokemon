package com.icetea520.pokemon.managers;

import com.icetea520.pokemon.PokemonPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class CatchBagManager {
    
    private final PokemonPlugin plugin;
    private final Map<UUID, List<CaughtPokemon>> playerBags;
    private static final int MAX_BAG_SIZE = 100;
    
    public CatchBagManager(PokemonPlugin plugin) {
        this.plugin = plugin;
        this.playerBags = new HashMap<>();
        
        // 載入玩家資料
        loadPlayerData();
    }
    
    public boolean addPokemon(Player player, Entity entity) {
        return addPokemon(player, entity.getType().name(), entity.getName());
    }

    public boolean addPokemon(Player player, String entityType, String entityName) {
        UUID playerId = player.getUniqueId();
        List<CaughtPokemon> bag = playerBags.getOrDefault(playerId, new ArrayList<>());
        
        if (bag.size() >= MAX_BAG_SIZE) {
            return false; // 背包已滿
        }
        
        CaughtPokemon pokemon = new CaughtPokemon(
            entityType,
            entityName,
            System.currentTimeMillis()
        );
        
        bag.add(pokemon);
        playerBags.put(playerId, bag);
        
        // 立即儲存到檔案
        savePlayerData(playerId, bag);
        
        // 觸發等級更新
        if (plugin.getLevelManager() != null) {
            plugin.getLevelManager().updatePlayerLevel(player);
        }
        
        return true;
    }
    
    public List<CaughtPokemon> getPlayerBag(Player player) {
        return playerBags.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    public int getBagSize(Player player) {
        return getPlayerBag(player).size();
    }
    
    /**
     * 根據 UUID 獲取玩家背包
     */
    public List<CaughtPokemon> getPlayerBagByUUID(UUID playerId) {
        return playerBags.getOrDefault(playerId, new ArrayList<>());
    }
    
    /**
     * 根據 UUID 獲取玩家背包大小
     */
    public int getBagSizeByUUID(UUID playerId) {
        return getPlayerBagByUUID(playerId).size();
    }
    
    /**
     * 載入玩家資料
     */
    private void loadPlayerData() {
        if (plugin.getDataManager() != null) {
            Map<UUID, List<CaughtPokemon>> loadedData = plugin.getDataManager().loadPlayerData();
            playerBags.putAll(loadedData);
            plugin.getLogger().info("已載入 " + loadedData.size() + " 個玩家的捕捉背包資料");
        }
    }
    
    /**
     * 儲存玩家資料
     */
    private void savePlayerData(UUID playerId, List<CaughtPokemon> bag) {
        if (plugin.getDataManager() != null) {
            plugin.getDataManager().savePlayerData(playerId, bag);
        }
    }
    
    /**
     * 儲存所有玩家資料
     */
    public void saveAllPlayerData() {
        if (plugin.getDataManager() != null) {
            plugin.getDataManager().saveAllPlayerData(playerBags);
        }
    }
    
    public static class CaughtPokemon {
        private final String type;
        private final String name;
        private final long catchTime;
        
        public CaughtPokemon(String type, String name, long catchTime) {
            this.type = type;
            this.name = name;
            this.catchTime = catchTime;
        }
        
        public String getType() { return type; }
        public String getName() { return name; }
        public long getCatchTime() { return catchTime; }
    }
}
