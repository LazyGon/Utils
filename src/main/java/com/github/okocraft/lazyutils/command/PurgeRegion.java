package com.github.okocraft.lazyutils.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.okocraft.lazyutils.LazyUtils;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.snapshot.Snapshot;
import com.sk89q.worldedit.world.snapshot.SnapshotRestore;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.MissingWorldException;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class PurgeRegion {

    public static synchronized boolean purgeInactiveRegions(CommandSender sender) {

        Optional<World> optionalWorld = Optional.ofNullable(Bukkit.getWorld("wl4"));
        if (!optionalWorld.isPresent()) {
            Commands.errorOccurred(sender, "§cワールドが存在しません。");
        }

        Player player = BukkitAdapter.adapt((org.bukkit.entity.Player) sender);
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(optionalWorld.get());

        RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = rc.get(world);
        Collection<ProtectedRegion> regions = rm.getRegions().values();
        List<String> removedRegions = new ArrayList<>();

        new BukkitRunnable() {

            @Override
            public void run() {
                regions.forEach(region -> {
                    ApplicableRegionSet appRgSet = rm.getApplicableRegions(region);
                    if (!appRgSet.testState(null, LazyUtils.inactiveAutoPurgeFlag)) {
                        return;
                    }
                    if (region.getIntersectingRegions(regions).stream()
                            .filter(intersectingRegion -> !appRgSet.testState(null, LazyUtils.inactiveAutoPurgeFlag))
                            .findAny().isPresent()) {
                        return;
                    };

                    Set<UUID> uniqueIds = region.getOwners().getUniqueIds();
                    if (uniqueIds.isEmpty()) return;
                    boolean active = uniqueIds.stream().filter(uuid -> Bukkit.getOfflinePlayer(uuid)
                            .getLastPlayed() > System.currentTimeMillis() - (60 * 24 * 60 * 60 * 1000))
                            .findAny().isPresent();
                    if (active) return;

                    Region weRegion;

                    if (region instanceof ProtectedCuboidRegion) {
                        ProtectedCuboidRegion wgCuboidRegion = (ProtectedCuboidRegion) region;
                        weRegion = new CuboidRegion(world, wgCuboidRegion.getMinimumPoint(),
                                wgCuboidRegion.getMaximumPoint());
                    } else {
                        return;
                    }

                    removedRegions.add(region.getId());

                    for (Region separatedRegion : separateRegion(weRegion)) {
                        scheduleRestoreTask(player, separatedRegion);

                        try {
                            Thread.sleep(3000L);
                        } catch (InterruptedException ex) {
                        }
                    }
                });
                removeRegions(sender, removedRegions, rm);
            }
        }.runTaskAsynchronously(LazyUtils.getInstance());

        return true;
    }

    private static synchronized void scheduleRestoreTask(Player player, Region region) {
        new BukkitRunnable() {

            @Override
            public void run() {
                player.print(region.toString());
                try {
                    restore(player, region);
                } catch (WorldEditException ex) {
                    ex.printStackTrace();
                }
            }
        }.runTask(LazyUtils.getInstance());
    }

    public static void removeRegions(CommandSender sender, List<String> regions, RegionManager regionManager) {
        sender.sendMessage("削除する保護:");
        regions.forEach(removedRegionName -> {
            Optional.ofNullable(regionManager.getRegion(removedRegionName)).ifPresent(rg -> {
                sender.sendMessage("  " + removedRegionName);
                sender.sendMessage("    オーナー:");

                rg.getOwners().getUniqueIds().forEach(uuid -> {
                    sender.sendMessage("      " + Bukkit.getOfflinePlayer(uuid).getName());
                });
                regionManager.removeRegion(removedRegionName, RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
            });
        });
    }

    public static void restore(Player player, Region region) throws WorldEditException {

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);
        LocalConfiguration config = WorldEdit.getInstance().getConfiguration();

        if (config.snapshotRepo == null) {
            player.printError("Snapshot/backup restore is not configured.");
            return;
        }

        Snapshot snapshot;

        try {
            snapshot = config.snapshotRepo.getDefaultSnapshot(player.getWorld().getName());

            if (snapshot == null) {
                player.printError("No snapshots were found. See console for details.");

                // Okay, let's toss some debugging information!
                File dir = config.snapshotRepo.getDirectory();

                try {
                    WorldEdit.logger.info("WorldEdit found no snapshots: looked in: " + dir.getCanonicalPath());
                } catch (IOException e) {
                    WorldEdit.logger.info("WorldEdit found no snapshots: looked in "
                            + "(NON-RESOLVABLE PATH - does it exist?): " + dir.getPath());
                }

                return;
            }
        } catch (MissingWorldException ex) {
            player.printError("No snapshots were found for this world.");
            return;
        }

        ChunkStore chunkStore;

        // Load chunk store
        try {
            chunkStore = snapshot.getChunkStore();
            // player.print("Snapshot '" + snapshot.getName() + "' loaded; now restoring...");
        } catch (DataException | IOException e) {
            player.printError("Failed to load snapshot: " + e.getMessage());
            return;
        }

        try {
            // Restore snapshot
            SnapshotRestore restore = new SnapshotRestore(chunkStore, editSession, region);

            restore.restore();
            editSession.close();

            if (restore.hadTotalFailure()) {
                String error = restore.getLastErrorMessage();
                if (!restore.getMissingChunks().isEmpty()) {
                    player.printError("Chunks were not present in snapshot.");
                } else if (error != null) {
                    player.printError("Errors prevented any blocks from being restored.");
                    player.printError("Last error: " + error);
                } else {
                    player.printError("No chunks could be loaded. (Bad archive?)");
                }
            } else {
                // player.print(String.format("Restored; %d " + "missing chunks and %d other errors.",
                // restore.getMissingChunks().size(), restore.getErrorChunks().size()));
            }
        } finally {
            try {
                chunkStore.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static Set<Region> separateRegion(Region originalRegion) {

        com.sk89q.worldedit.world.World world = originalRegion.getWorld();
        BlockVector3 minPoint = originalRegion.getMinimumPoint();
        BlockVector3 maxPoint = originalRegion.getMaximumPoint();

        int rawMinX = minPoint.getX();
        int rawMinZ = minPoint.getZ();
        int rawMaxX = maxPoint.getX();
        int rawMaxZ = maxPoint.getZ();

        return originalRegion.getChunks().stream().map(chunk -> {
            int minX = chunk.getX() << 4;
            int minZ = chunk.getZ() << 4;
            int maxX = minX + 15;
            int maxZ = minZ + 15;
            if (minX < rawMinX)
                minX = rawMinX;
            if (minZ < rawMinZ)
                minZ = rawMinZ;
            if (maxX > rawMaxX)
                maxX = rawMaxX;
            if (maxZ > rawMaxZ)
                maxZ = rawMaxZ;

            BlockVector3 min = BlockVector3.at(minX, 0, minZ);
            BlockVector3 max = BlockVector3.at(maxX, 255, maxZ);

            return new CuboidRegion(world, min, max);
        }).collect(Collectors.toSet());
    }
}