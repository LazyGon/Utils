package com.github.okocraft.lazyutils.listener;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeath implements Listener {

    public PlayerDeath(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathLowest(PlayerDeathEvent event) {
        // 4 28 -42, 64 53 18
        Bukkit.getOnlinePlayers().stream().filter(PlayerDeath::isInPvPArea)
                .forEach(player -> player.sendMessage(event.getDeathMessage()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeathHigh(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!isInPvPArea(player)) return;
        new BukkitRunnable(){
            
                @Override
                public void run() {   
                    player.spigot().respawn();
                }
        }.runTaskLater(LazyUtils.getInstance(), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!isInPvPArea(event.getPlayer())) return;
        event.setRespawnLocation(new Location(Bukkit.getWorld("lobby_5"), 34, 55, -12));
    }

    private static boolean isInPvPArea(Player player) {
        Location loc = player.getLocation();
        return loc.getWorld().getName().equals("lobby_5") &&
                4 <= loc.getX() && loc.getX() <= 64 &&
                28 <= loc.getY() && loc.getY() <= 60 &&
                -43 <= loc.getZ() && loc.getZ() <= 18;
    }
}