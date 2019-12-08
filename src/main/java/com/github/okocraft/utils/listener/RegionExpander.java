package com.github.okocraft.utils.listener;

import java.math.BigInteger;
import java.util.Map;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Messages;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;

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
        if (!event.getMessage().equals("create") && !event.getMessage().equals("edit")) {
            return;
        }

        World playerWorld = BukkitAdapter.adapt(event.getPlayer().getWorld());
        Player player = BukkitAdapter.adapt(event.getPlayer());

        int maxCount = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(playerWorld).maxRegionCountPerPlayer;
        long regionCount = WorldGuard.getInstance().getPlatform().getRegionContainer().get(playerWorld).getRegions().values().stream().filter(region -> region.getOwners().contains(player.getUniqueId())).count();
        
        if (maxCount <= regionCount) {
            Messages.sendMessage(event.getPlayer(), false, "listener.wg-gui.too-many-regions", Map.of("%max-count%", maxCount));
            event.setMessage("q");
            return;
        }
        
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
        Region selection;
        try {
            selection = session.getSelection(playerWorld);
        } catch (IncompleteRegionException ignored) {
            return;
        }

        BlockVector3 min = selection.getMinimumPoint();
        BlockVector3 max = selection.getMaximumPoint();

        int maxVolume = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(playerWorld).maxClaimVolume;
        if (isExceedingMaxVolume(playerWorld, min, max)) {
            Messages.sendMessage(event.getPlayer(), false, "listener.wg-gui.too-large-region", Map.of("%max-volume%", maxVolume));
            event.setMessage("q");
            return;
        }

        BlockVector3 expandedMin = BlockVector3.at(min.getX(), 0, min.getZ());
        BlockVector3 expandedMax = BlockVector3.at(max.getX(), 255, max.getZ());

        ActorSelectorLimits limit = ActorSelectorLimits.forActor(player);
        session.getRegionSelector(playerWorld).selectPrimary(expandedMin, limit);
        session.getRegionSelector(playerWorld).selectSecondary(expandedMax, limit);
    }

    private static boolean isExceedingMaxVolume(World world, BlockVector3 min, BlockVector3 max) {
        int maxVolume = WorldGuard.getInstance().getPlatform().getGlobalStateManager().get(world).maxClaimVolume;

        BigInteger maxX = BigInteger.valueOf(max.getBlockX());
        BigInteger maxZ = BigInteger.valueOf(max.getBlockZ());
        BigInteger minX = BigInteger.valueOf(min.getBlockX());
        BigInteger minZ = BigInteger.valueOf(min.getBlockZ());
        return maxX.subtract(minX).multiply(maxZ.subtract(minZ)).abs().compareTo(BigInteger.valueOf(maxVolume)) == 1;
    }
}