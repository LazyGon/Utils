package com.github.okocraft.utils.listener;

import java.util.Arrays;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

public class CommandListener implements Listener {

    public CommandListener(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRgClaimCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("/rg claim") && !message.startsWith("/region claim")
                && !message.startsWith("/regions claim") && !message.startsWith("/worldguard:rg claim")
                && !message.startsWith("/worldguard:region claim")
                && !message.startsWith("/worldguard:regions claim")) {
            return;
        }

        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("wl4")) {
            return;
        }
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(event.getPlayer());
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        Region selection;
        try {
            selection = localSession.getSelection(localSession.getSelectionWorld());
        } catch (IncompleteRegionException ex) {
            return;
        }
        ProtectedCuboidRegion limitedRegion = new ProtectedCuboidRegion("__temp_limit__", true,
                BlockVector3.at(150, 0, 275), BlockVector3.at(-353, 255, -308));
        ProtectedCuboidRegion selectedRegion = new ProtectedCuboidRegion("__temp_selected__", true,
                selection.getMaximumPoint(), selection.getMinimumPoint());
        boolean intersect = !selectedRegion.getIntersectingRegions(Arrays.asList(limitedRegion)).isEmpty();
        if (!intersect) {
            return;
        }

        World world = BukkitAdapter.adapt(event.getPlayer().getWorld());
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (limitedRegion.getIntersectingRegions(rm.getRegions().values()).stream().filter(
                protectedRegion -> protectedRegion.getOwners().getUniqueIds().contains(event.getPlayer().getUniqueId()))
                .findAny().isPresent()) {
            event.getPlayer().sendMessage("§c区画を2つ以上保護することはできません。");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvPCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("/bw ") && !message.startsWith("/bedwars") && !message.startsWith("/1v1")
                && !message.startsWith("/duel") && !message.startsWith("/duels") && !message.startsWith("/spec")) {
            return;
        }

        if (event.getPlayer().getWorld().getName().equalsIgnoreCase("Playground")) {
            return;
        }

        if (event.getPlayer().hasPermission("okocraft.command.pvp")) {
            return;
        }

        event.getPlayer().sendMessage("§7* §cこのコマンドはBedwarsやKitPvPの会場でしか使えません。");
        event.setCancelled(true);
    }
}