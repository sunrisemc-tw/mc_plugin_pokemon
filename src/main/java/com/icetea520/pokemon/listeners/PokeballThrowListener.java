package com.icetea520.pokemon.listeners;

import com.icetea520.pokemon.PokemonPlugin;
import com.icetea520.pokemon.managers.CatchBagManager;
import com.icetea520.pokemon.managers.GameModeManager;
import com.icetea520.pokemon.managers.PokeballManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class PokeballThrowListener implements Listener {
    
    private final PokemonPlugin plugin;
    private final PokeballManager pokeballManager;
    private final CatchBagManager catchBagManager;
    private final GameModeManager gameModeManager;
    
    public PokeballThrowListener(PokemonPlugin plugin) {
        this.plugin = plugin;
        this.pokeballManager = plugin.getPokeballManager();
        this.catchBagManager = plugin.getCatchBagManager();
        this.gameModeManager = plugin.getGameModeManager();
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // 檢查是否在抓寶模式
        if (!gameModeManager.isInPokemonMode(player)) {
            return;
        }
        
        // 檢查是否使用捕捉球
        if (!pokeballManager.isPokeball(item)) {
            return;
        }
        
        // 檢查是否為右鍵點擊
        if (!event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }
        
        event.setCancelled(true);
        
        // 丟出捕捉球
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setCustomName("Pokeball");
        snowball.setCustomNameVisible(false);
        
        // 消耗捕捉球
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
        
        player.sendMessage("§e捕捉球已丟出！");
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        
        // 檢查是否為捕捉球
        if (!(projectile instanceof Snowball) || !"Pokeball".equals(projectile.getCustomName())) {
            return;
        }
        
        ProjectileSource shooter = projectile.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }
        
        Player player = (Player) shooter;
        Entity hitEntity = event.getHitEntity();
        
        if (hitEntity != null && !(hitEntity instanceof Player)) {
            // 擊中生物，嘗試捕捉
            handleCatch(player, hitEntity);
        } else {
            // 沒擊中生物，返回捕捉球
            returnPokeball(player);
        }
        
        // 移除捕捉球實體
        projectile.remove();
    }
    
    private void handleCatch(Player player, Entity entity) {
        String entityType = entity.getType().name();
        String entityName = entity.getName();
        if (entityName == null || entityName.isBlank()) {
            entityName = entityType;
        }
        final String finalEntityName = entityName;
        final String finalEntityType = entityType;
        
        if (!isCatchableEntity(entity)) {
            player.getScheduler().run(plugin, scheduledTask -> {
                player.sendMessage("§c此生物無法捕捉！");
                returnPokeballInternal(player);
            }, () -> {});
            return;
        }
        
        boolean catchSuccessful = pokeballManager.isCatchSuccessful();
        
        if (catchSuccessful) {
            player.getScheduler().run(plugin, scheduledTask -> {
                boolean added = catchBagManager.addPokemon(player, finalEntityType, finalEntityName);
                if (added) {
                    player.sendMessage("§a成功捕捉到 " + finalEntityName + "！");
                    entity.getScheduler().run(plugin, entityTask -> {
                        if (entity.isValid()) {
                            entity.remove();
                        }
                    }, () -> {});
                } else {
                    player.sendMessage("§c捕捉失敗！背包可能已滿。");
                    returnPokeballInternal(player);
                }
            }, () -> {});
        } else {
            player.getScheduler().run(plugin, scheduledTask -> {
                player.sendMessage("§c捕捉失敗！" + finalEntityName + " 逃脫了！");
                returnPokeballInternal(player);
            }, () -> {});
        }
    }
    
    private void returnPokeball(Player player) {
        player.getScheduler().run(plugin, scheduledTask -> returnPokeballInternal(player), () -> {});
    }
    
    private void returnPokeballInternal(Player player) {
        ItemStack pokeball = pokeballManager.createPokeball(1);
        player.getInventory().addItem(pokeball);
        player.sendMessage("§e捕捉球已返回！");
    }
    
    private boolean isCatchableEntity(Entity entity) {
        // 這裡可以定義哪些生物可以被捕捉
        // 目前允許所有生物，除了玩家
        return !(entity instanceof Player);
    }
}
