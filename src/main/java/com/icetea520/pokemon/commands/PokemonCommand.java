package com.icetea520.pokemon.commands;

import com.icetea520.pokemon.PokemonPlugin;
import com.icetea520.pokemon.modules.ModeSwitchModule;
import com.icetea520.pokemon.modules.CatchBagModule;
import com.icetea520.pokemon.modules.PokeballModule;
import com.icetea520.pokemon.modules.ItemGetModule;
import com.icetea520.pokemon.modules.LevelModule;
import com.icetea520.pokemon.modules.SupplyModule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PokemonCommand implements CommandExecutor {
    
    private final PokemonPlugin plugin;
    private final ModeSwitchModule modeModule;
    private final CatchBagModule bagModule;
    private final PokeballModule pokeballModule;
    private final ItemGetModule itemGetModule;
    private final LevelModule levelModule;
    private final SupplyModule supplyModule;
    
    public PokemonCommand(PokemonPlugin plugin) {
        this.plugin = plugin;
        this.modeModule = new ModeSwitchModule(plugin);
        this.bagModule = new CatchBagModule(plugin);
        this.pokeballModule = new PokeballModule(plugin);
        this.itemGetModule = new ItemGetModule(plugin);
        this.levelModule = new LevelModule(plugin);
        this.supplyModule = new SupplyModule(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此指令只能由玩家使用！");
            return true;
        }
        
        Player player = (Player) sender;
        
        player.getScheduler().run(plugin, scheduledTask -> {
            if (args.length == 0) {
                sendHelpMessage(player);
                return;
            }
            
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "mode":
                    if (!player.hasPermission("pokemon.mode")) {
                        player.sendMessage("§c您沒有權限使用此功能！");
                        return;
                    }
                    modeModule.execute(player, args);
                    break;
                    
                case "bag":
                    if (!player.hasPermission("pokemon.bag")) {
                        player.sendMessage("§c您沒有權限使用此功能！");
                        return;
                    }
                    bagModule.execute(player, args);
                    break;
                    
                case "catch":
                    if (!player.hasPermission("pokemon.catch")) {
                        player.sendMessage("§c您沒有權限使用此功能！");
                        return;
                    }
                    pokeballModule.execute(player, args);
                    break;
                    
                case "get":
                    if (!player.hasPermission("pokemon.get")) {
                        player.sendMessage("§c您沒有權限使用此功能！");
                        return;
                    }
                    itemGetModule.execute(player, args);
                    break;
                    
                case "level":
                    if (!player.hasPermission("pokemon.level")) {
                        player.sendMessage("§c您沒有權限使用此功能！");
                        return;
                    }
                    levelModule.execute(player, args);
                    break;
                    
                case "supply":
                    if (!player.hasPermission("pokemon.supply")) {
                        player.sendMessage("§c您沒有權限使用此功能！");
                        return;
                    }
                    supplyModule.execute(player, args);
                    break;
                    
                default:
                    sendHelpMessage(player);
                    break;
            }
        }, () -> {});
        
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== 寶可夢插件指令 ===");
        player.sendMessage("§e/e mode §7- 切換抓寶模式");
        player.sendMessage("§e/e bag §7- 開啟捕捉背包 (GUI)");
        player.sendMessage("§e/e catch §7- 使用捕捉球 (丟出)");
        player.sendMessage("§e/e level §7- 查看等級資訊");
        if (player.hasPermission("pokemon.supply")) {
            player.sendMessage("§e/e supply §7- 物資補給管理");
        }
        if (player.hasPermission("pokemon.get")) {
            player.sendMessage("§e/e get <物品> §7- 獲取物品");
        }
        player.sendMessage("§7或使用 /sc 作為替代指令");
        player.sendMessage("§6=====================");
    }
}
