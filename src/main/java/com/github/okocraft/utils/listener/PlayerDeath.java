package com.github.okocraft.utils.listener;

import com.github.okocraft.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PlayerDeath implements Listener {

    private static final Utils plugin = Utils.getInstance();

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
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!isInPvPArea(event.getPlayer())) return;
        event.setRespawnLocation(new Location(Bukkit.getWorld("lobby_5"), 34, 55, -12));
    }

    @EventHandler
    public void onPlayerItemDamaged(PlayerItemDamageEvent event) {
        if (isInPvPArea(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        
        if (!isInPvPArea(player)) {
            return;
        }

        event.setCancelled(true);
        player.damage(event.getDamage());

        Vector knockback = player.getLocation().toVector().subtract(event.getDamager().getLocation().toVector());
        double power = Math.sqrt(event.getDamage() * 0.75);

        knockback = knockback.normalize().multiply(power).add(new Vector(0, power / 3, 0));
        player.setVelocity(knockback);

        new BukkitRunnable(){
            long startTime = System.currentTimeMillis();

            @Override
            public void run() {
                player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);
                
                if (player.getVelocity().getY() < 0 || player.isDead() || startTime + 1000 < System.currentTimeMillis()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private static boolean isInPvPArea(Player player) {
        Location loc = player.getLocation();
        return loc.getWorld().getName().equals("lobby_5") &&
                4 <= loc.getX() && loc.getX() <= 64 &&
                28 <= loc.getY() && loc.getY() <= 60 &&
                -43 <= loc.getZ() && loc.getZ() <= 18;
    }
}