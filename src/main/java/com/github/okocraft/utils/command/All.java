package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.okocraft.utils.config.Messages;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class All extends SubCommand {

    All() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "command.general.error.player-only");
            return false;
        }

        int radius = 32;
        if (args.length > 1) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (radius >= 128) {
            radius = 128;
        }
 
        Player player = (Player) sender;
        Location loc = player.getLocation();
        loc.setY(128);
        
        int entities = player.getWorld().getNearbyEntities(loc, radius, 127, radius).size();
        Messages.sendMessage(sender, "command.utils.all.info.result", Map.of("%amount%", entities, "%radius%", radius));
		return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], List.of("2", "4", "8", "16", "32", "64", "128"), new ArrayList<>());
        }

        return List.of();
    }

    @Override
    String getName() {
        return "all";
    }

    @Override
    int getLeastArgsLength() {
        return 1;
    }

    @Override
    String getUsage() {
        return "/utils all [radius]";
    }
}