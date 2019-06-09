package com.github.okocraft.lazyutils.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.milkbowl.vault.economy.Economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

public class Repair {

    private static Economy economy = LazyUtils.getInstance().getEconomy();
    private static FileConfiguration costConfig = LazyUtils.getInstance().getRepairCostConfig().getConfig();


    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!LazyUtils.getInstance().isEconomyEnabled())
            return Commands.errorOccured(sender, "§c経済が有効化されていません。");

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || Material.AIR.equals(item.getType()))
            return Commands.errorOccured(sender, "§c空気は直せません。");

        Damageable damageableMeta;
        try {
            damageableMeta = (Damageable) item.getItemMeta();
        } catch (ClassCastException e) {
            return Commands.errorOccured(sender, "§cそのアイテムは直せません。");
        }

        int currentDamage = damageableMeta.getDamage();
        int maxDurability = (int) item.getType().getMaxDurability();
        if (currentDamage == 0 || maxDurability == 0)
            return Commands.errorOccured(sender, "§c耐久値が減っていません。");

        double damagePercent = Math.round(((double) currentDamage / (double) maxDurability) * 1000D)/10D;
        if (damagePercent < 1)
            return Commands.errorOccured(sender, "§c損耗率が少なすぎて直せません。(" + damagePercent + "%)");

        double cost = Math.round(damagePercent * calcCost(sender)) / 100D;
        if (cost == 0)
            return Commands.errorOccured(sender, "§cコストが0です。正しい手順を踏んでいてもこうなる場合は管理者に報告してください。");
        
        double maxCost = costConfig.getDouble("MaxCost", 500000.0);
        if (cost > maxCost) cost = maxCost;
        
        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            sender.sendMessage("§7 * 金額は §b" + cost + " §7円です (損耗率: " + damagePercent + "%)。それで良ければ /costrepair confirm と入力してください。");
            return true;
        }

        if (economy.getBalance(player) < cost)
            return Commands.errorOccured(sender, "§cお金が足りません。");
        economy.withdrawPlayer(player, cost);

        damageableMeta.setDamage(0);
        item.setItemMeta((ItemMeta) damageableMeta);
        player.getInventory().setItemInMainHand(item);

        return true;
    }

    @SuppressWarnings("deprecation")
    public static double calcCost(CommandSender sender) {
        Player player = (Player) sender;
        ItemStack mainHandItem = player.getEquipment().getItemInMainHand();

        Object costEnchantmentConfigObject = costConfig.get("Enchantments");
        Object costItemConfigObject = costConfig.get("ItemCost");

        if (!costEnchantmentConfigObject.getClass().getSimpleName().equals("MemorySection")
                || !costItemConfigObject.getClass().getSimpleName().equals("MemorySection")) {
            sender.sendMessage("§cconfigファイルにミスがあります。管理者に報告してください。");
            LazyUtils.getInstance().getLogger()
                    .warning("Enchantments in config is not MemorySection. Please fix to use costrepair.");
            return 0;
        }

        MemorySection costEnchantmentConfig = (MemorySection) costConfig.get("Enchantments");
        MemorySection costItemConfig = (MemorySection) costConfig.get("ItemCost");

        Map<Enchantment, MemorySection> enchantCostConfigMap = costEnchantmentConfig.getValues(false).entrySet()
                .stream().filter(entry -> ((MemorySection) entry.getValue()).getBoolean("Enabled"))
                .collect(Collectors.toMap(entry -> Enchantment.getByName(entry.getKey()),
                        entry -> (MemorySection) entry.getValue(), (enchant1, enchant2) -> enchant1, HashMap::new));

        Map<String, Double> costItemConfigMap;
        try {
            costItemConfigMap = costItemConfig.getValues(false).entrySet().stream().collect(Collectors.toMap(
                    item -> item.getKey(), item -> (double) (int) item.getValue(), (i1, i2) -> i1, HashMap::new));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return 0;
        }

        double itemCostBase = costItemConfigMap.getOrDefault(mainHandItem.getType().name(),
                costItemConfigMap.getOrDefault("DEFAULT", 10000.0));

        if (mainHandItem.getEnchantments().isEmpty())
            return itemCostBase;

        List<Double> globalAdd = new ArrayList<>();
        List<Double> globalSubtract = new ArrayList<>();
        List<Double> globalMultiply = new ArrayList<>();
        List<Double> globalDivide = new ArrayList<>();
        List<Double> globalPower = new ArrayList<>();

        double sumOfCost = mainHandItem.getEnchantments().entrySet().stream().parallel().mapToDouble(entry -> {

            MemorySection config = enchantCostConfigMap.get(entry.getKey());
            if (config == null)
                return itemCostBase;

            String globalModifierOperation = config.getString("GlobalModifierOperation", "NONE");
            if (!globalModifierOperation.equals("NONE")) {
                double globalModifier = config.getDouble("GlobalModifier", Double.MAX_VALUE);
                if (globalModifier != Double.MAX_VALUE) {
                    switch (globalModifierOperation) {
                    case "ADD":
                        globalAdd.add(globalModifier);
                        break;
                    case "SUBTRACT":
                        globalSubtract.add(globalModifier);
                        break;
                    case "MULTIPLY":
                        globalMultiply.add(globalModifier);
                        break;
                    case "DIVIDE":
                        globalDivide.add(globalModifier);
                        break;
                    case "POWER":
                        globalPower.add(globalModifier);
                        break;
                    }
                }

            }

            double costBase = config.getDouble("Cost", 0) + itemCostBase;

            double levelModifier = config.getDouble("LevelModifier", Double.MAX_VALUE);
            String levelModifierOperation = config.getString("LevelModifierOperation", "NONE");
            if (levelModifier == Double.MAX_VALUE || (levelModifier == 0 && levelModifierOperation.equals("DIVIDE")))
                return costBase;
            int enchantLevel = entry.getValue();

            double result = costBase;
            for (int count = 2; count <= enchantLevel; count++) {
                switch (levelModifierOperation) {
                case "ADD":
                    result += levelModifier;
                    break;
                case "SUBTRACT":
                    result -= levelModifier;
                    break;
                case "MULTIPLY":
                    result *= levelModifier;
                    break;
                case "DIVIDE":
                    result /= levelModifier;
                    break;
                case "POWER":
                    result = Math.pow(result, levelModifier);
                    break;
                }
            }

            return result;

        }).sequential().reduce(0, (a, b) -> a + b);
        return Math.pow(
                (((sumOfCost + globalAdd.stream().reduce(0.0, (a, b) -> a + b))
                        - globalSubtract.stream().reduce(0.0, (a, b) -> a + b))
                        * globalMultiply.stream().reduce(1.0, (a, b) -> a * b)
                        / globalDivide.stream().reduce(1.0, (a, b) -> a * b)),
                globalPower.stream().reduce(1.0, (a, b) -> a * b));
    }
}