package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.okocraft.utils.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

public class PvPArea extends UtilsCommand implements Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }
        
        com.github.okocraft.utils.listener.PvPArea instance;
        if (!(sender instanceof Player)) {
            if (args.length == 1) {
                Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
                return false;
            }

            instance = com.github.okocraft.utils.listener.PvPArea.getInstance(null);
            String subCommand = args[0].toLowerCase(Locale.ROOT);
            switch (subCommand) {
            case "smashmode":
            case "itemunlimited":
                boolean enabled = args[1].equalsIgnoreCase("true");
                if (subCommand.equals("smashmode")) {
                    instance.setSmashMode(enabled);
                    if (enabled) {
                        Messages.sendMessage(sender, "command.pvparea.info.enable-smash-mode");
                    } else {
                        Messages.sendMessage(sender, "command.pvparea.info.disable-smash-mode");
                    }
                } else {
                    instance.setItemUnlimited(enabled);
                    if (enabled) {
                        Messages.sendMessage(sender, "command.pvparea.info.enable-item-damage");
                    } else {
                        Messages.sendMessage(sender, "command.pvparea.info.disable-item-damage");
                    }
                }
                return true;
            default:
                Messages.sendMessage(sender, "command.general.error.invalid-argument",
                        Map.of("%argument%", subCommand));
                return false;
            }
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            try {
                instance = new com.github.okocraft.utils.listener.PvPArea(player);
                Messages.sendMessage(sender, "command.pvparea.info.success",
                        Map.of("%x1%", instance.getPos1().getX(), "%y1%", instance.getPos1().getY(), "%z1%",
                                instance.getPos1().getZ(), "%x2%", instance.getPos2().getX(), "%y2%",
                                instance.getPos2().getY(), "%z2%", instance.getPos2().getZ()));
                return true;
            } catch (NullPointerException e) {
                if (e.getMessage() == null) {
                    throw new NullPointerException();

                } else if (e.getMessage().contains("WorldEdit is not installed")) {
                    Messages.sendMessage(sender, "command.pvparea.error.no-worldedit");

                } else if (e.getMessage().contains("WorldGuard is not installed")) {
                    Messages.sendMessage(sender, "command.pvparea.error.no-worldguard");

                } else {
                    throw new NullPointerException();
                }

                return false;
            } catch (IllegalArgumentException e) {
                if (e.getMessage() == null) {
                    throw new NullPointerException();

                } else if (e.getMessage().contains("The player has already created PvP area")) {
                    Messages.sendMessage(sender, "command.pvparea.error.already-exist");

                } else if (e.getMessage().contains("Select region")) {
                    Messages.sendMessage(sender, "command.pvparea.error.no-selection");

                } else if (e.getMessage().contains("Region must be in your own worldguard protection")) {
                    Messages.sendMessage(sender, "command.pvparea.error.must-be-in-owning-protection");

                } else if (e.getMessage().contains("PvP area is disabled in this world")) {
                    Messages.sendMessage(sender, "command.pvparea.error.pvparea-disabled-world");

                } else {
                    throw new NullPointerException();
                }
                return false;
            }
        }

        instance = com.github.okocraft.utils.listener.PvPArea.getInstance((Player) sender);

        if (instance == null) {
            Messages.sendMessage(sender, "command.pvparea.error.you-need-to-create-area");
            return false;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
        case "respawnpoint":
            instance.setRespawnPoint(player.getLocation());
            Messages.sendMessage(sender, "command.pvparea.info.set-respawn");
            return true;
        case "close":
            instance.stopListener();
            Messages.sendMessage(sender, "command.pvparea.info.closed-pvparea");
            return true;
        case "smashmode":
        case "itemunlimited":
            if (args.length == 1) {
                Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
                return false;
            }

            boolean enabled = args[1].equalsIgnoreCase("true");
            if (subCommand.equals("smashmode")) {
                instance.setSmashMode(enabled);
                if (enabled) {
                    Messages.sendMessage(sender, "command.pvparea.info.enable-smash-mode");
                } else {
                    Messages.sendMessage(sender, "command.pvparea.info.disable-smash-mode");
                }
            } else {
                instance.setItemUnlimited(enabled);
                if (enabled) {
                    Messages.sendMessage(sender, "command.pvparea.info.enable-item-damage");
                } else {
                    Messages.sendMessage(sender, "command.pvparea.info.disable-item-damage");
                }
            }
            return true;
        default:
            Messages.sendMessage(sender, "command.general.error.invalid-argument",
                    Map.of("%argument%", subCommand));
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> subCommands = List.of("respawnpoint", "smashmode", "itemunlimited", "close");
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, result);
        }
        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if (subCommand.equals("respawnpoint") || subCommand.equals("close")) {
            return result;
        }

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], List.of("true", "false"), result);
        }

        return result;
    }

    @Override
    int getLeastArgsLength() {
        return 0;
    }

    @Override
    String getUsage() {
        return "/pvparea key value";
    }
}