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
import org.bukkit.util.StringUtil;

public class Suffix extends SubCommand {

    Suffix() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        String suffix;

        if (args.length > 2) {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                Messages.sendMessage(sender, "command.general.error.player-is-not-online");
                return false;
            }

            suffix = args[2];
        } else if (sender instanceof Player) {
            player = (Player) sender;
            suffix = args[1];
        } else {
            Messages.sendMessage(sender, "command.general.error.specify-player");
            return false;
        }

        System.out.println(suffix);
        char suffixChar = suffix.replaceAll("&[0-9a-f]", "").charAt(0);
        System.out.println(suffix);
        suffix = suffix.substring(0, suffix.indexOf(suffixChar) + 1);
        System.out.println(suffix);
        String suffixCommand = Config.getSuffixSetCommand().replace("%player%", player.getName()).replace("%suffix%", suffix);
        System.out.println(suffix);
        System.out.println(suffixCommand);
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), suffixCommand);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        if (sender.hasPermission("utils.suffix.other")) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], players, result);
            }

            if (!players.contains(args[1])) {
                return result;
            }

            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], List.of("<suffix>"), result);
            }
        } else {
            if (args.length == 2) {
                return StringUtil.copyPartialMatches(args[1], List.of("<suffix>"), result);
            }
        }

        return result;
    }

    @Override
    int getLeastArgsLength() {
        return 2;
    }

    @Override
    String getUsage() {
        return "/utils suffix <suffix>";
    }
}