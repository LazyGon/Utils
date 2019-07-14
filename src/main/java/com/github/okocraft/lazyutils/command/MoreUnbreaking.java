package com.github.okocraft.lazyutils.command;

import java.util.Arrays;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class MoreUnbreaking {

    public static boolean addUnbreaking(Player player) {

        ItemStack ticket = new ItemStack(Material.PAPER);
        ItemMeta ticketMeta = ticket.getItemMeta();
        ticketMeta.setLore(Arrays.asList("§7", "§7おこチケ超大当たりで排出されるチケット。", "§7超レア称号、スポナーなどと交換可能。", ""));
        ticketMeta.setDisplayName("§6§lレジェンダリーチケット");
        ticket.setItemMeta(ticketMeta);

        PlayerInventory inv = player.getInventory();

        ItemStack mainHandItem = inv.getItemInMainHand();
        int durabilityLevel = mainHandItem.getEnchantments().getOrDefault(Enchantment.DURABILITY, 0);
        if (durabilityLevel < 3)
            return Commands.errorOccurred(player, "§c耐久力のレベルは3以上でなくてはなりません。");

        int maxLevel = LazyUtils.getInstance().getConfig().getInt("MoreUnbreakingMaxLevel", 10);
        if (durabilityLevel >= maxLevel)
            return Commands.errorOccurred(player, "§c耐久力の最大値は §b" + maxLevel + " §cレベルまでです。");
            
        if (mainHandItem.getAmount() > 1)
            return Commands.errorOccurred(player, "§cスタックされたアイテムには使えません。");

        for(ItemStack item : inv) {
            if (item != null &&
                    item.getType() == Material.PAPER &&
                    item.getItemMeta().getDisplayName().equals("§6§lレジェンダリーチケット") &&
                    item.getItemMeta().hasLore() &&
                    item.getItemMeta().getLore().equals(Arrays.asList("§7", "§7おこチケ超大当たりで排出されるチケット。", "§7超レア称号、スポナーなどと交換可能。", ""))
            ) {
                item.setAmount(item.getAmount() - 1);
                mainHandItem.addUnsafeEnchantment(Enchantment.DURABILITY, durabilityLevel + 1);
                return true;
            }
        }
        return Commands.errorOccurred(player, "§cレジェンダリーチケットがありません。");        
    }

}