package com.github.okocraft.lazyutils.listener;

import java.util.HashMap;
import java.util.Map;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FarmAutoReplace implements Listener {

    private FileConfiguration farmConfig;
    private LazyUtils instance;

    private Map<Material, Material> plants = new HashMap<Material, Material>() {
        private static final long serialVersionUID = 1L;
        {
            put(Material.WHEAT, Material.WHEAT_SEEDS);
            put(Material.POTATOES, Material.POTATO);
            put(Material.CARROTS, Material.CARROT);
            put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        }
    };

    public FarmAutoReplace(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        instance = LazyUtils.getInstance();
        farmConfig = instance.getPublicFarmConfig().getConfig();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void plantAutoReplace(BlockBreakEvent event) {

        if (event.isCancelled())
            return;

        Block brokenBlock = event.getBlock();
        Material brokenBlockType = brokenBlock.getType();
        Player player = event.getPlayer();
        if (!farmConfig.getStringList("PlantAutoReplaceEnebledWorlds")
                .contains(brokenBlock.getWorld().getName()))
            return;

        if (!plants.containsKey(brokenBlockType)) return;

        if (!player.getInventory().contains(plants.get(brokenBlockType))) {
            event.setCancelled(true);
            return;
        }

        Ageable blockDataAgable = (Ageable) brokenBlock.getBlockData();
        if (blockDataAgable.getAge() != blockDataAgable.getMaximumAge()) {
            event.setCancelled(true);
            return;
        }

        Ageable newBlockDataAgeable = (Ageable) blockDataAgable.clone();
        newBlockDataAgeable.setAge(0);

        new BukkitRunnable(){
        
            @Override
            public void run() {
                Location blockLoc = brokenBlock.getLocation();
                if (!blockLoc.getBlock().getType().equals(Material.AIR))
                    return;
                blockLoc.getBlock().setType(brokenBlockType);
                blockLoc.getBlock().setBlockData(newBlockDataAgeable);
                player.getInventory().removeItem(new ItemStack(plants.get(brokenBlockType)));
            }
        }. runTaskLater(instance, 3L);

    }
}