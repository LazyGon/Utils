package com.github.okocraft.lazyutils.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public class TestListener implements Listener {

    public TestListener(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onTest(EntityDamageEvent event) {
        Entity ent = event.getEntity();
        if (!(ent instanceof Player) || !ent.getName().equalsIgnoreCase("lazy_gon")) {
            return;
        }

        ent.sendMessage(event.getCause().name());
    }
}