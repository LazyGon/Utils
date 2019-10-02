package com.github.okocraft.utils.command;

import java.util.List;

import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class MoreUnbreaking extends SubCommand {

    MoreUnbreaking() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "command.general.error.player-only");
            return false;
        }

        ItemStack ticket = Config.getLegendaryTicket();
        Player player = (Player) sender;
        PlayerInventory inv = player.getInventory();

        ItemStack mainHandItem = inv.getItemInMainHand();
        int durabilityLevel = mainHandItem.getEnchantments().getOrDefault(Enchantment.DURABILITY, 0);
        if (durabilityLevel < 3) {
            Messages.sendMessage(sender, "command.utils.more-unbreaking.error.must-be-level-3-or-more");
            return false;
        }

        int maxLevel = Config.getMaxUnbreakingLevel();
        if (durabilityLevel >= maxLevel) {
            Messages.sendMessage(sender, "command.utils.more-unbreaking.error.over-max-level");
            return false;
        }
        
        if (mainHandItem.getAmount() > 1) {
            Messages.sendMessage(sender, "command.utils.more-unbreaking.error.stacked-item-is-not-allowed");
            return false;
        }

        if (inv.containsAtLeast(ticket, 1) && inv.removeItem(ticket).containsValue(ticket)) {
            mainHandItem.addUnsafeEnchantment(Enchantment.DURABILITY, durabilityLevel + 1);
            return true;
        } else {
            Messages.sendMessage(player, "command.general.error.no-legendary-ticket");
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }

    @Override
    int getLeastArgsLength() {
        return 1;
    }

    @Override
    String getUsage() {
        return "/utils moreunbreaking";
    }
}