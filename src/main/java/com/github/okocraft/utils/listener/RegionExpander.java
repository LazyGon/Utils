package com.github.okocraft.utils.listener;

import com.github.okocraft.utils.Utils;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class RegionExpander implements Listener {

    private static final Utils PLUGIN = Utils.getInstance();
    private static RegionExpander INSTANCE = new RegionExpander();

    private RegionExpander() {}

    public static RegionExpander getInstance() {
        return INSTANCE;
    }

    public void startListener() {
        Bukkit.getPluginManager().registerEvents(this, PLUGIN);
    }

    public void stopListener() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreateRegionWithWGGUI(AsyncPlayerChatEvent event) {
        if (!event.getMessage().equals("create")) {
            return;
        }
        World playerWorld = BukkitAdapter.adapt(event.getPlayer().getWorld());
        Player player = BukkitAdapter.adapt(event.getPlayer());
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
        Region selection;
        try {
            selection = session.getSelection(playerWorld);
        } catch (IncompleteRegionException inogred) {
            return;
        }

        BlockVector3 min = selection.getMinimumPoint();
        BlockVector3 max = selection.getMaximumPoint();

        BlockVector3 expandedMin = BlockVector3.at(min.getX(), 0, min.getZ());
        BlockVector3 expandedMax = BlockVector3.at(max.getX(), 255, max.getZ());

        ActorSelectorLimits limit = ActorSelectorLimits.forActor(player);
        session.getRegionSelector(playerWorld).selectPrimary(expandedMin, limit);
        session.getRegionSelector(playerWorld).selectSecondary(expandedMax, limit);
    }
}