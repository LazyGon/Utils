package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

public class UtilsCommand implements CommandExecutor, TabCompleter {

    private static Utils plugin = Utils.getInstance();

    private static enum SubCommands {
        ALL(new All()),
        COST_REPAIR(new CostRepair()),
        GIVE_LEGENDARY_TICKET(new GiveLegendaryTicket()),
        HIGH_JUMP(new HighJump()),
        INACTIVE_MONEY(new InactiveMoney()),
        MORE_UNBREAKING(new MoreUnbreaking()),
        SCORE_RANKING(new ScoreRanking()),
        SPAWNER(new Spawner()),
        SUFFIX(new Suffix()),
        UNIQUE_PREFIX(new UniquePrefix());

        private SubCommand subCommand;
        private static List<String> subCommandNames = List.of("all", "costrepair", "givelegendaryticket", "givelt", "highjump", "hj", "moreunbreaking", "scoreranking", "srank", "spawner", "suffix", "uniqueprefix", "upref");
        private static Map<String, String> aliases = Map.of("givelt", "givelegendaryticket", "hj", "highjump", "srank", "scoreranking", "upref", "uniqueprefix");
        private SubCommands(SubCommand subCommand) {
            this.subCommand = subCommand;
        }

        SubCommand get() {
            return subCommand;
        }

        static SubCommand getSubCommand(String name) {
            name = aliases.getOrDefault(name, name);
            for (SubCommands subCommand : SubCommands.values()) {
                if (subCommand.get().getName().equalsIgnoreCase(name)) {
                    return subCommand.get();
                }
            }

            return null;
        }

        static List<String> getCommandNames() {
            return new ArrayList<>(subCommandNames);
        }
    }

    public UtilsCommand() {
        PluginCommand utilsCommand = plugin.getCommand("utils");
        utilsCommand.setExecutor(this);
        utilsCommand.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Messages.sendMessage(sender, "command.general.error.not-enough.arguments");
            return false;
        }

        SubCommand subCommand = SubCommands.getSubCommand(args[0]);
        if (subCommand == null) {
            Messages.sendMessage(sender, "command.general.error.invalid-argument", Map.of("%argument%", args[0]));
            return false;
        }

        if (!subCommand.hasPermission(sender)) {
            Messages.sendMessage(sender, "command.general.no-permission");
            return false;
        }

        return subCommand.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            Messages.sendMessage(sender, "command.general.error.not-enough.arguments");
            return List.of();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SubCommands.getCommandNames(), new ArrayList<>());
        }

        if (!SubCommands.getCommandNames().contains(args[0])) {
            return List.of();
        }

        SubCommand subCommand = SubCommands.getSubCommand(args[0]);
        if (subCommand == null) {
            Messages.sendMessage(sender, "command.general.error.invalid-argument", Map.of("%argument%", args[0]));
            return List.of();
        }

        if (!subCommand.hasPermission(sender)) {
            Messages.sendMessage(sender, "command.general.no-permission");
            return List.of();
        }

        return subCommand.onTabComplete(sender, command, alias, args);
    }
}