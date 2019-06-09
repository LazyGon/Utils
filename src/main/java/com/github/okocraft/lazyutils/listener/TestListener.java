package com.github.okocraft.lazyutils.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;

public class TestListener implements Listener {

    public TestListener(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        System.out.println(event.getSender().getName() + "tried to tabcomplete of " + event.getBuffer());
    }
}