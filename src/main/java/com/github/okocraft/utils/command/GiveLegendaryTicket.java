package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public class GiveLegendaryTicket extends UtilsCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }

        Player player;

        if (args.length > 0) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Messages.sendMessage(sender, "command.general.error.player-is-not-online");
                return false;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            Messages.sendMessage(sender, "command.general.error.specify-player");
            return false;
        }

        int amount = 1;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        ItemStack ticket = Config.getLegendaryTicket();
        ticket.setAmount(amount);
        player.getInventory().addItem(ticket);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], players, result);
        }

        if (!players.contains(args[0])) {
            return result;
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], List.of("1", "2", "4", "8", "16", "32", "64"), result);
        }

        return result;
    }

    @Override
    int getLeastArgsLength() {
        return 0;
    }

    @Override
    String getUsage() {
        return "/givelegendaryticket [player] [amount]";
    }
}