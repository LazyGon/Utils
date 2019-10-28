package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.okocraft.utils.config.Messages;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class Respawn extends UtilsCommand {

    Respawn() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }
        
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            Messages.sendMessage(sender, "command.respawn.error.player-is-not-online");
            return false;
        }

        if (!player.isDead()) {
            Messages.sendMessage(sender, "command.respawn.error.player-is-alive");
            return false;
        }

        player.spigot().respawn();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], players, new ArrayList<>());
        }

        return List.of();
    }

    @Override
    int getLeastArgsLength() {
        return 1;
    }

    @Override
    String getUsage() {
        return "/respawn <player>";
    }
}