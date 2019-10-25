package com.github.okocraft.utils.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import net.milkbowl.vault.economy.Economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.okocraft.utils.config.Messages;
import com.github.okocraft.utils.config.RepairCostConfig;

public class CostRepair extends UtilsCommand {

    private static Economy economy = plugin.getEconomy();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }        
        
        if (!plugin.isEconomyEnabled()) {
            Messages.sendMessage(sender, "command.cost-repair.error.economy-is-not-enabled");
            return false;
        }

        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "command.general.error.player-only");
            return false;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || Material.AIR.equals(item.getType())) {
            Messages.sendMessage(sender, "command.cost-repair.error.cannot-repair-air");
            return false;
        }

        if (!(item.getItemMeta() instanceof Damageable)) {
            Messages.sendMessage(sender, "command.cost-repair.error.cannot-repair-the-item");
            return false;
        }
        Damageable damageableMeta = (Damageable) item.getItemMeta();

        int currentDamage = damageableMeta.getDamage();
        int maxDurability = (int) item.getType().getMaxDurability();
        if (currentDamage == 0 || maxDurability == 0) {
            Messages.sendMessage(sender, "command.cost-repair.error.durability-is-full");
            return false;
        }
        
        double minDamagePercent = RepairCostConfig.getMinDamagedPercent() / 100D;
        double damagePercent = Math.round(((double) currentDamage / (double) maxDurability) * 1000D)/10D;
        if (damagePercent < minDamagePercent) {
            Messages.sendMessage(sender, "command.cost-repair.error.too-low-damaged-percent", Map.of("%percent%", damagePercent, "%min-percent%", minDamagePercent));
            return false;
        }
            
        double cost = Math.round(damagePercent * RepairCostConfig.getCost(item)) / 100D;
        cost = Math.min(RepairCostConfig.getMaxCost(), cost);
        
        if (args.length < 1 || !args[0].equalsIgnoreCase("confirm")) {
            Messages.sendMessage(sender, "command.cost-repair.info.notify-cost", Map.of("%cost%", cost));
            return true;
        }
        
        if (economy.getBalance(player) < cost) {
            Messages.sendMessage(sender, "command.cost-repair.error.not-enough-money");
            return false;
        }

        economy.withdrawPlayer(player, cost);

        damageableMeta.setDamage(0);
        item.setItemMeta((ItemMeta) damageableMeta);
        player.getInventory().setItemInMainHand(item);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {    
            return StringUtil.copyPartialMatches(args[0], List.of("confirm"), new ArrayList<>());
        }

        return List.of();
    }

    @Override
    int getLeastArgsLength() {
        return 0;
    }

    @Override
    String getUsage() {
        return "/costrepair [confirm]";
    }
}