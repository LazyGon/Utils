package com.github.okocraft.lazyutils.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class PlayerDeath implements Listener {

    public PlayerDeath(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 4 28 -42, 64 53 18
        Bukkit.getOnlinePlayers().stream().filter(PlayerDeath::isInThePvPArea)
                .forEach(player -> player.sendMessage(event.getDeathMessage()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeathRespawn(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!isInThePvPArea(player)) {
            return;
        }
        player.spigot().respawn();
        player.teleport(new Location(Bukkit.getWorld("lobby_5"), 34, 55, -12));
    }

    private static boolean isInThePvPArea(Player player) {
        Location loc = player.getLocation();
        if (loc.getWorld().getName().equals("lobby_5") && 4 <= loc.getX() && loc.getX() <= 64 && 28 <= loc.getY()
                && loc.getY() <= 60 && -43 <= loc.getZ() && loc.getZ() <= 18)
            return true;
        return false;
    }
}