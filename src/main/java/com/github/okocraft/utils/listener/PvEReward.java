package com.github.okocraft.utils.listener;

import com.github.okocraft.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;

public class PvEReward implements Listener {

    Utils plugin = Utils.getInstance();

    public PvEReward(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void myPetToMythicMob(MythicMobDeathEvent event) {
        if (!event.getMob().getLivingEntity().getLocation().getWorld().getName().equals("PvE")) {
            return;
        }
        
        if (event.getKiller() instanceof Player) {
            Player killer = (Player) event.getKiller();
            killer.playSound(killer.getLocation(), Sound.ITEM_SHIELD_BREAK, SoundCategory.MASTER, 2L, 2L);
            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 2L, 2L);
        }
    }
}