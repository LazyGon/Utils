package com.github.okocraft.lazyutils.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SaplingAutoReplace implements Listener {

    private FileConfiguration treeFarmConfig;
    private Set<Location> treeLocations;
    private Map<Material, Material> trees = new HashMap<Material, Material>() {
        private static final long serialVersionUID = 1L;
        {
            put(Material.ACACIA_LOG, Material.ACACIA_SAPLING);
            put(Material.ACACIA_WOOD, Material.ACACIA_SAPLING);
            put(Material.SPRUCE_LOG, Material.SPRUCE_SAPLING);
            put(Material.SPRUCE_WOOD, Material.SPRUCE_SAPLING);
            put(Material.BIRCH_LOG, Material.BIRCH_SAPLING);
            put(Material.BIRCH_WOOD, Material.BIRCH_SAPLING);
            put(Material.JUNGLE_LOG, Material.JUNGLE_SAPLING);
            put(Material.JUNGLE_WOOD, Material.JUNGLE_SAPLING);
            put(Material.DARK_OAK_LOG, Material.DARK_OAK_SAPLING);
            put(Material.DARK_OAK_WOOD, Material.DARK_OAK_SAPLING);
            put(Material.OAK_LOG, Material.OAK_SAPLING);
            put(Material.OAK_WOOD, Material.OAK_SAPLING);
            put(Material.STRIPPED_ACACIA_LOG, Material.ACACIA_SAPLING);
            put(Material.STRIPPED_ACACIA_WOOD, Material.ACACIA_SAPLING);
            put(Material.STRIPPED_SPRUCE_LOG, Material.SPRUCE_SAPLING);
            put(Material.STRIPPED_SPRUCE_WOOD, Material.SPRUCE_SAPLING);
            put(Material.STRIPPED_BIRCH_LOG, Material.BIRCH_SAPLING);
            put(Material.STRIPPED_BIRCH_WOOD, Material.BIRCH_SAPLING);
            put(Material.STRIPPED_JUNGLE_LOG, Material.JUNGLE_SAPLING);
            put(Material.STRIPPED_JUNGLE_WOOD, Material.JUNGLE_SAPLING);
            put(Material.STRIPPED_DARK_OAK_LOG, Material.DARK_OAK_SAPLING);
            put(Material.STRIPPED_DARK_OAK_WOOD, Material.DARK_OAK_SAPLING);
            put(Material.STRIPPED_OAK_LOG, Material.OAK_SAPLING);
            put(Material.STRIPPED_OAK_WOOD, Material.OAK_SAPLING);
        }
    };

    public SaplingAutoReplace(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        treeFarmConfig = LazyUtils.getInstance().getPublicTreeFarmConfig().getConfig();
        treeLocations = getTreeLocations();
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void saplingAutoReplace(BlockBreakEvent event) {

        if (event.isCancelled())
            return;

        Block brokenBlock = event.getBlock();
        Location blockLoc = brokenBlock.getLocation();
        if (!treeLocations.contains(blockLoc))
            return;

        Material brokenMaterial = brokenBlock.getBlockData().getMaterial();
        if (trees.values().contains(brokenMaterial)) {
            event.setCancelled(true);
            return;
        }
        if (!trees.containsKey(brokenMaterial))
            return;

        Material blockBelow = brokenBlock.getLocation().add(0D, -1D, 0D).getBlock().getBlockData().getMaterial();
        if (!blockBelow.equals(Material.DIRT) && !blockBelow.equals(Material.GRASS_BLOCK) && !blockBelow.equals(Material.PODZOL))
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Material sapling = trees.get(brokenMaterial);
                blockLoc.getBlock().setType(sapling);
                blockLoc.getBlock().setBlockData(sapling.createBlockData());
            }
        }.runTaskLater(LazyUtils.getInstance(), 3L);
    }

    private Set<Location> getTreeLocations() {

        Set<Location> treeLocations = new HashSet<>();
        Set<Location> treeLocationsRelative = new HashSet<>();

        if (!treeFarmConfig.isSet("trees")) {
            return treeLocations;
        }

        if (!treeFarmConfig.get("trees").getClass().getSimpleName().equals("MemorySection")) {
            return treeLocations;
        }

        World worldForRelativeLocation = Bukkit.getServer()
                .getWorld(treeFarmConfig.getString("WorldForRelativeLocation", ""));
        Location relativeBase = null;
        if (worldForRelativeLocation != null) {
            double relativeBaseX = treeFarmConfig.getDouble("RelativeLocationBase.x", Double.MAX_VALUE);
            double relativeBaseY = treeFarmConfig.getDouble("RelativeLocationBase.y", Double.MAX_VALUE);
            double relativeBaseZ = treeFarmConfig.getDouble("RelativeLocationBase.z", Double.MAX_VALUE);
            if (relativeBaseX != Double.MAX_VALUE && relativeBaseY != Double.MAX_VALUE
                    && relativeBaseZ != Double.MAX_VALUE) {
                relativeBase = new Location(worldForRelativeLocation, relativeBaseX, relativeBaseY, relativeBaseZ);
            }
        }

        ((MemorySection) treeFarmConfig.get("trees")).getValues(false).entrySet().stream()
                .filter(entry -> entry.getValue().getClass().getSimpleName().equals("MemorySection"))
                .forEach(configSections -> {

                    MemorySection section = (MemorySection) configSections.getValue();

                    if (worldForRelativeLocation != null && section.isSet("RelativeLocation")) {
                        double x = section.getDouble("RelativeLocation.x", Double.MAX_VALUE);
                        double y = section.getDouble("RelativeLocation.y", Double.MAX_VALUE);
                        double z = section.getDouble("RelativeLocation.z", Double.MAX_VALUE);

                        if (x != Double.MAX_VALUE && y != Double.MAX_VALUE && z != Double.MAX_VALUE) {
                            treeLocationsRelative.add(new Location(worldForRelativeLocation, x, y, z));
                            return;
                        }

                    } else if (section.isSet("AbsoluteLocation") && section.isSet("World")) {

                        World world = Bukkit.getServer().getWorld(section.getString("World", ""));

                        if (world == null)
                            return;
                        double x = section.getDouble("AbsoluteLocation.x", Double.MAX_VALUE);
                        double y = section.getDouble("AbsoluteLocation.y", Double.MAX_VALUE);
                        double z = section.getDouble("AbsoluteLocation.z", Double.MAX_VALUE);

                        if (x != Double.MAX_VALUE && y != Double.MAX_VALUE && z != Double.MAX_VALUE) {
                            treeLocations.add(new Location(world, x, y, z));
                            return;
                        }
                    }
                });

        for (Location location : treeLocationsRelative) {
            treeLocations.add(location.add(relativeBase));
        }

        return treeLocations;
    }
}