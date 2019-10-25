
package com.github.okocraft.utils.config;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.okocraft.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Config {

    private static final Utils plugin = Utils.getInstance();
    static FileConfiguration config = plugin.getConfig();

    private Config() {
    }

    public static List<String> getPvPAreaDisabledWorlds() {
        return get().getStringList("pvp-area-disabled-worlds");
    }

    public static boolean isDefaultSmashModeEnabled() {
        return get().getBoolean("pvp-area-default.smash-mode");
    }

    public static boolean isDefaultItemUnlimitedEnabled() {
        return get().getBoolean("pvp-area-default.item-unlimited");
    }

    public static int getPvPAreaMaxRegion() {
        return get().getInt("pvp-area-max-region", 125000);
    }

    public static Location getDefaultPvPAreaPos1() {
        World world = getDefaultPvPAreaWorld();
        int x = get().getInt("pvp-area-default.pos1.x");
        int y = get().getInt("pvp-area-default.pos1.y");
        int z = get().getInt("pvp-area-default.pos1.z");
        return new Location(world, x, y, z);
    }

    public static Location getDefaultPvPAreaPos2() {
        World world = getDefaultPvPAreaWorld();
        int x = get().getInt("pvp-area-default.pos2.x");
        int y = get().getInt("pvp-area-default.pos2.y");
        int z = get().getInt("pvp-area-default.pos2.z");
        return new Location(world, x, y, z);
    }

    public static Location getDefaultPvPAreaRespawn() {
        World world = getDefaultPvPAreaWorld();
        int x = get().getInt("pvp-area-default.respawn.x");
        int y = get().getInt("pvp-area-default.respawn.y");
        int z = get().getInt("pvp-area-default.respawn.z");
        float yaw = (float) get().getInt("pvp-area-default.respawn.yaw");
        float pitch = (float) get().getInt("pvp-area-default.respawn.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static World getDefaultPvPAreaWorld() {
        return Bukkit.getWorld(get().getString("pvp-area-default.world", ""));
    }

    public static String getJailName() {
        return get().getString("jail-name-for-auto-punishment");
    }

    public static List<Integer> getPunishmentPointPerWarns() {
        return get().getIntegerList("punishment-point-per-warn");
    }

    public static String getPrefixRemoveCommand() {
        return get().getString("prefix-remove-command");
    }

    public static String getPrefixSetCommand() {
        return get().getString("prefix-set-command");
    }

    public static String getSuffixSetCommand() {
        return get().getString("suffix-set-command");
    }

    public static List<World> getSpawnerAllowedWorlds() {
        return get().getStringList("spawner-allowed-worlds").stream().map(Bukkit::getWorld).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static ItemStack getLegendaryTicket() {
        String name = get().getString("legendary-ticket.display-name", "§6§lレジェンダリーチケット");
        List<String> lore = get().getStringList("legendary-ticket.lore");
        return new ItemStack(Material.PAPER) {
            {
                ItemMeta meta = getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(lore);
                setItemMeta(meta);
            }
        };
    }

    public static int getMaxUnbreakingLevel() {
        return get().getInt("more-unbreaking-max-level", 10);
    }

    /**
     * Reload jail config. If this method used before {@code JailConfig.save()}, the
     * data on memory will be lost.
     */
    public static void reload() {
        saveDefault();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    /**
     * Saves data on memory to yaml.
     */
    public static void save() {
        plugin.saveConfig();
    }

    /**
     * Copies yaml from jar to data folder.
     */
    public static void saveDefault() {
        plugin.saveDefaultConfig();
    }

    static FileConfiguration get() {
        return config;
    }

    public static void reloadAllConfigs() {
		reload();
        PrefixData.reload();
        Messages.reload();
        RepairCostConfig.reload();
        
    }
}