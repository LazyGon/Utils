package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.okocraft.utils.config.Messages;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.milkbowl.vault.economy.Economy;

public class InactiveMoney extends UtilsCommand {

    InactiveMoney() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }
        
        Economy econ = plugin.getEconomy();

		long day;
        try {
            day = Long.parseLong(args[0]);
            if (day < 0) {
                throw new NumberFormatException("The number must be positive.");
            }
        } catch (NumberFormatException e) {
            Messages.sendMessage(sender, "command.general.error.invalid-number");
            return false;
        }

		double totalInactiveMoney = Arrays.stream(Bukkit.getOfflinePlayers()).parallel()
				.filter(OfflinePlayer::hasPlayedBefore)
				.filter(player -> (System.currentTimeMillis() - player.getLastPlayed()) / (1000 * 3600 * 24) > day)
				.mapToDouble(econ::getBalance).sum();
		Messages.sendMessage(sender, "command.inactive-money.result", Map.of("%total%", totalInactiveMoney));
		return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("14, 30, 60, 360"), new ArrayList<>());
        }

        return List.of();
    }

    @Override
    int getLeastArgsLength() {
        return 1;
    }

    @Override
    String getUsage() {
        return "/inactivemoney <day>";
    }
}