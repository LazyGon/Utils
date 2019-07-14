package com.github.okocraft.lazyutils.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PreventCreatingHall implements Listener {

    private static final LazyUtils instance = LazyUtils.getInstance();
    private static final Map<Player, Integer> count = new HashMap<>();
    private static final Map<Player, Location> previousLocation = new HashMap<>();

    public PreventCreatingHall() {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void countAndPreventCreatingHall(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        Location prevLoc = previousLocation.getOrDefault(player, player.getLocation());

        if (player.hasPermission("lazyutils.bypass.creatinghall")) {
            return;
        }

        Material material = event.getBlock().getType();
        if (material != Material.DIRT && material != Material.STONE && material != Material.ANDESITE
                && material != Material.DIORITE && material != Material.GRANITE && material != Material.GRAVEL
                && material != Material.GRASS_BLOCK) {
            return;
        }

        if (!isLookingUpFace(player) || player.getLocation().getY() < event.getBlock().getLocation().getY() + 1) {
            previousLocation.put(player, player.getLocation());
            return;
        }

        if (prevLoc.getBlockX() != loc.getBlockX() || prevLoc.getBlockZ() != loc.getBlockZ()) {
            if (count.containsKey(player)) {
                count.remove(player);
            }
            previousLocation.put(player, player.getLocation());
            return;
        }

        Integer breakCount = count.getOrDefault(player, 0);
        if (breakCount >= 6) {
            player.sendMessage("§8[§6直下掘り対策§8] §c直下掘りはできません。別の位置に行って下さい。");
            event.setCancelled(true);
            return;
        }

        count.put(player, breakCount + 1);
        previousLocation.put(player, player.getLocation());
        return;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (count.containsKey(player)) {
            count.remove(player);
        }
    }

    public boolean isLookingUpFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 6);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) return false;
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock) == BlockFace.UP;
    }
}