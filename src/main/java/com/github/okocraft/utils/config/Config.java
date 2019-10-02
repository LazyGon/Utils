
package com.github.okocraft.utils.config;

import java.util.List;

import com.github.okocraft.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Config {

    private static Utils plugin = Utils.getInstance();
    static FileConfiguration config = plugin.getConfig();

    private Config() {
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
        List<World> worlds = Bukkit.getWorlds();
        worlds.removeIf(world -> !get().getStringList("spawner-allowed-worlds").contains(world.getName()));
        return worlds;
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

    public static String getConfigVersion() {
        return get().getString("plugin.version", "0.0.0");
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

    public static void saveAllDefaultConfigs() {
		reload();
		PrefixData.reload();
    }
}