package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class Commands implements CommandExecutor, TabCompleter {

    private static final Utils plugin = Utils.getInstance();
    private static Commands instance;

    private Commands() {
        PluginCommand utilsCommand = plugin.getCommand("utils");
        utilsCommand.setExecutor(this);
        utilsCommand.setTabCompleter(this);

        new All();
        new CostRepair();
        new GiveLegendaryTicket();
        new InactiveMoney();
        new MoreUnbreaking();
        new PvPArea();
        new ScoreRanking();
        new Suffix();
        new UniquePrefix();
    }

    public static boolean init() {
        if (instance != null) {
            return false;
        }

        instance = new Commands();
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
        case "reload":
            return reload(sender, command, label, args);
        default:
            Messages.sendMessage(sender, "command.general.error.invalid-argument",
                    Map.of("%argument%", subCommand));
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 0) {
            return result;
        }

        List<String> subCommands = new ArrayList<>();
        if (sender.hasPermission("utils.reload")) {
            subCommands.add("reload");
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, result);
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if (!subCommands.contains(subCommand)) {
            return result;
        }
        
        return result;
    }

    private static boolean reload(CommandSender sender, Command command, String label, String[] args) {
        Config.reloadAllConfigs();
        Messages.sendMessage(sender, "command.utils.reload.info.success");
        return true;
    }
}