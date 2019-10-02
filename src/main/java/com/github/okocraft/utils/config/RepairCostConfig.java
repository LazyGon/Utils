package com.github.okocraft.utils.config;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class RepairCostConfig {

    private static CustomConfig prefixData = new CustomConfig("repair-cost.yml");
    static {
        saveDefault();
    }

    public static int getMinDamagedPercent() {
        int result = get().getInt("min-damaged-percent", 1);
        if (result > 100) {
            result = 100;
        } else if (result < 0) {
            result = 0;
        }

        return result;
    }

    public static double getMaxCost() {
        return get().getDouble("max-cost", 50000);
    }

    public static double getCost(ItemStack item) {
        double base = getBaseCost(item.getType());
        double enchantCost = 0;
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            enchantCost += getEnchantCost(entry.getKey(), entry.getValue());
        }

        return base + enchantCost;
    }

    public static double getBaseCost(Material item) {
        double def = get().getDouble("item-cost.default", 50000);
        return get().getDouble("item-cost." + item.name(), def);
    }

    public static double getEnchantCost(Enchantment enchant, int level) {
        if (level > 5) {
            level = 5;
        } else if (level < 1) {
            level = 1;
        }

        double def = get().getDouble("enchantments.cost.default." + level, List.of(100, 200, 400, 800, 1600).get(level - 1));
        @SuppressWarnings("deprecation")
        double result = get().getDouble("enchantments.cost." + enchant.getName() + "." + level, def);
        return result;
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