package com.github.okocraft.utils.config;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.bukkit.configuration.file.FileConfiguration;

public final class PrefixData {

    private static CustomConfig prefixData = new CustomConfig("prefix-data.yml");
    static {
        saveDefault();
    }

    public static boolean addPrefix(OfflinePlayer player, String prefix, boolean force) {
        if (isUsed(prefix) && !force) {
            return false;
        }

        List<String> prefixes = getPrefixes(player);
        if (prefixes.contains(prefix)) {
            return false;
        }
        prefixes.add(prefix);
        setPrefixes(player, prefixes, force);
        return true;
    }

    public static boolean removePrefix(OfflinePlayer player, String prefix) {
        List<String> prefixes = getPrefixes(player);
        if (!prefixes.remove(prefix)) {
            return false;
        }
        setPrefixes(player, prefixes, true);
        return true;
    }

    public static void setPrefixes(OfflinePlayer player, List<String> prefixes, boolean force) {
        if (!force) {
            Set<String> all = getAllPrefix();
            all.removeAll(getPrefixes(player));
            prefixes.removeAll(all);
        }

        get().set(player.getUniqueId().toString(), prefixes);
        save();
    }

    public static OfflinePlayer getPlayerOwningPrefix(String prefix) {
        for (OfflinePlayer player : getPlayerOwningPrefix()) {
            if (getPrefixes(player).contains(prefix)) {
                return player;
            }
        }

        return null;
    }

    public static Set<OfflinePlayer> getPlayerOwningPrefix() {
        return get().getKeys(false).stream().map(uuid -> {
            try {
                return UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }).filter(Objects::nonNull).map(Bukkit::getOfflinePlayer)
        .filter(OfflinePlayer::hasPlayedBefore).collect(Collectors.toSet());
    }

    public static List<String> getPrefixes(OfflinePlayer player) {
        return get().getStringList(player.getUniqueId().toString());
    }

    public static boolean isUsed(String prefix) {
        return getAllPrefix().contains(prefix);
    }

    public static Set<String> getAllPrefix() {
        return getPlayerOwningPrefix().stream().flatMap(player -> getPrefixes(player).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Reload jail config. If this method used before {@code JailConfig.save()}, the
     * data on memory will be lost.
     */
    public static void reload() {
        prefixData.initConfig();
    }

    /**
     * Saves data on memory to yaml.
     */
    public static void save() {
        prefixData.saveConfig();
    }

    /**
     * Copies yaml from jar to data folder.
     */
    public static void saveDefault() {
        prefixData.saveDefaultConfig();
    }

    /**
     * Gets FileConfiguration of jail config.
     * 
     * @return jail config.
     */
    static FileConfiguration get() {
        return prefixData.getConfig();
    }
}