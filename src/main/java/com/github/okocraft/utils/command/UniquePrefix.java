package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.config.Messages;
import com.github.okocraft.utils.config.PrefixData;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class UniquePrefix extends SubCommand {

    UniquePrefix() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand = args[1].toLowerCase(Locale.ROOT);
        switch (subCommand) {
        case "add":
            return add(sender, command, label, args);
        case "list":
            return list(sender, command, label, args);
        case "remove":
            return remove(sender, command, label, args);
        case "set":
            return set(sender, command, label, args);
        default:
            Messages.sendMessage(sender, "command.general.error.invalid-argument", Map.of("%argument%", subCommand));
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean add(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("utils.uniqueprefix.add")) {
            Messages.sendMessage(sender, "command.general.error.no-permission");
            return false;
        }

        OfflinePlayer player;
        String prefix;

        // /utils uniqueprefix add player prefix
        if (args.length > 3 && sender.hasPermission("utils.uniqueprefix.add.other")) {
            player = Bukkit.getOfflinePlayer(args[2]);
            if (!player.hasPlayedBefore() && player.getName() == null) {
                Messages.sendMessage(sender, "command.general.error.player-does-not-exist");
                return false;
            }
            prefix = args[3];
        } else if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
            prefix = args[2];
        } else {
            Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        if (!prefix.matches("&([0-9]|[a-f])(\\p{InHiragana}|\\p{InKatakana}|\\p{InCjkUnifiedIdeographs})")) {
            Messages.sendMessage(sender, "command.utils.unique-prefix.error.invalid-syntax");
            return false;
        }

        if (!PrefixData.addPrefix(player, prefix, false)) {
            Messages.sendMessage(sender, "command.utils.unique-prefix.error.prefix-is-already-in-use");
            return false;
        }

        if (player == sender && !player.getPlayer().getInventory().removeItem(Config.getLegendaryTicket()).isEmpty()) {
            Messages.sendMessage(sender, "command.general.error.no-legendary-ticket");
            return false;
        }

        prefix = "&7[" + prefix + "&7]";
        String prefixCommand = Config.getPrefixSetCommand().replace("%player%", player.getUniqueId().toString())
                .replace("%prefix%", prefix);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), prefixCommand);
        Messages.sendMessage(sender, "command.utils.unique-prefix.info.add-success", Map.of("%player%", player.getName(), "%prefix%", prefix));

        return true;
    }

    @SuppressWarnings("deprecation")
    private static boolean remove(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("utils.uniqueprefix.remove")) {
            Messages.sendMessage(sender, "command.general.error.no-permission");
            return false;
        }

        OfflinePlayer player;
        String prefix;

        if (args.length > 3 && sender.hasPermission("utils.uniqueprefix.remove.other")) {
            player = Bukkit.getOfflinePlayer(args[2]);
            if (!player.hasPlayedBefore() && player.getName() == null) {
                Messages.sendMessage(sender, "command.general.error.player-does-not-exist");
                return false;
            }
            prefix = args[3];
        } else if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
            prefix = args[2];
        } else {
            Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        if (!PrefixData.removePrefix(player, prefix)) {
            Messages.sendMessage(sender, "command.utils.unique-prefix.error.player-do-not-have-the-prefix");
            return false;
        }

        prefix = "&7[" + prefix + "&7]";
        String prefixCommand = Config.getPrefixRemoveCommand().replace("%player%", player.getUniqueId().toString())
                .replace("%prefix%", prefix);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), prefixCommand);
        Messages.sendMessage(sender, "command.utils.unique-prefix.info.remove-success", Map.of("%player%", player.getName(), "%prefix%", prefix));

        return true;
    }

    @SuppressWarnings("deprecation")
    private static boolean set(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("utils.uniqueprefix.set")) {
            Messages.sendMessage(sender, "command.general.error.no-permission");
            return false;
        }

        OfflinePlayer player;
        String prefix;

        if (args.length > 3 && sender.hasPermission("utils.uniqueprefix.set.other")) {
            player = Bukkit.getOfflinePlayer(args[2]);
            if (!player.hasPlayedBefore() && player.getName() == null) {
                Messages.sendMessage(sender, "command.general.error.player-does-not-exist");
                return false;
            }
            prefix = args[3];
        } else if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
            prefix = args[2];
        } else {
            Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        if (player != PrefixData.getPlayerOwningPrefix(prefix)) {
            Messages.sendMessage(sender, "command.utils.unique-prefix.error.player-do-not-have-the-prefix");
            return false;
        }

        prefix = "&7[" + prefix + "&7]";
        String prefixCommand = Config.getPrefixSetCommand().replace("%player%", player.getUniqueId().toString())
                .replace("%prefix%", prefix);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), prefixCommand);
        Messages.sendMessage(sender, "command.utils.unique-prefix.info.set-success", Map.of("%player%", player.getName(), "%prefix%", prefix));

        return true;
    }

    @SuppressWarnings("deprecation")
    private static boolean list(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("utils.uniqueprefix.list")) {
            Messages.sendMessage(sender, "command.general.error.no-permission");
            return false;
        }
        
        OfflinePlayer player;

        if (args.length > 2 && sender.hasPermission("utils.uniqueprefix.list.other")) {
            player = Bukkit.getOfflinePlayer(args[2]);
            if (!player.hasPlayedBefore() && player.getName() == null) {
                Messages.sendMessage(sender, "command.general.error.player-does-not-exist");
                return false;
            }
        } else if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
        } else {
            Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        Messages.sendMessage(sender, "command.utils.unique-prefix.info.list-header", Map.of("%player%", player.getName()));
        PrefixData.getPrefixes(player).forEach(prefix -> {
            Messages.sendMessage(sender, false, "command.utils.unique-prefix.info.list-format", Map.of("%prefix%", prefix));
        });

        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> subCommands = List.of("add", "set", "remove", "list");
        subCommands.removeIf(commandName -> !sender.hasPermission("utils.uniqueprefix." + commandName));

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], subCommands, result);
        }

        String subCommand = args[1].toLowerCase(Locale.ROOT);
        if (!subCommands.contains(subCommand)) {
            return result;
        }

        if (sender.hasPermission("utils.suffix.other")) {
            List<String> players = Arrays.stream(Bukkit.getOfflinePlayers()).filter(OfflinePlayer::hasPlayedBefore).map(OfflinePlayer::getName).collect(Collectors.toList());
            if (args.length == 3) {
                return StringUtil.copyPartialMatches(args[2], players, result);
            }

            if (!players.contains(args[2]) || subCommand.equals("list")) {
                return result;
            }

            switch (subCommand) {
                case "add":
                return StringUtil.copyPartialMatches(args[3], List.of("<prefix>"), result);
                case "remove":
                case "set":
                return StringUtil.copyPartialMatches(args[3], PrefixData.getPrefixes(Bukkit.getOfflinePlayer(args[2])), result);
            }
        } else {

            if (args[1].equalsIgnoreCase("list")) {
                return result;
            }

            if (args.length == 3) {
                switch (subCommand) {
                    case "add":
                    return StringUtil.copyPartialMatches(args[2], List.of("<prefix>"), result);
                    case "remove":
                    case "set":
                    return StringUtil.copyPartialMatches(args[2], PrefixData.getPrefixes((OfflinePlayer) sender), result);
                }
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
        // utils.uniqueprefix.otherを保つ時は add|set|remove|list player [prefix]となる
        return "/utils uniqueprefix < <add|set|remove> <prefix> | list >";
    }
}
