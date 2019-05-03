package com.github.okocraft.lazyutils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.github.okocraft.lazyutils.LazyUtils;
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.StringUtil;

public class LazyUtilsTabCompleter implements TabCompleter {

    public LazyUtilsTabCompleter() {
        LazyUtils instance = LazyUtils.getInstance();
        instance.getCommand("uniqueprefix").setTabCompleter(this);
        instance.getCommand("suffix").setTabCompleter(this);
        instance.getCommand("uuidscoreboard").setTabCompleter(this);
        instance.getCommand("scoreranking").setTabCompleter(this);
        instance.getCommand("costrepair").setTabCompleter(this);
        instance.getCommand("getspawner").setTabCompleter(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> resultList = new ArrayList<>();

        String commandName = command.getName().toLowerCase();
        switch (commandName) {

        case "scoreranking":
            sender.hasPermission("lazyutils." + commandName);
            return onTabCompleteScoreRanking(resultList, args);
        case "uniqueprefix":
            sender.hasPermission("lazyutils." + commandName);
            return onTabCompleteUniquePrefix(sender, resultList, args);
        case "suffix":
            sender.hasPermission("lazyutils." + commandName);
            return onTabCompleteSuffix(sender, resultList, args);
        case "uuidscoreboard":
            sender.hasPermission("lazyutils." + commandName);
            return onTabCompleteUuidScoreboard(resultList, args);
        case "costrepair":
            sender.hasPermission("lazyutils." + commandName);
            return onTabCompleteRepair(sender, resultList, args);
        case "getspawner":
            sender.hasPermission("lazyutils." + commandName);
            return onTabCompleteSpawner(resultList, args);
        }

        return resultList;
    }

    private List<String> onTabCompleteSuffix(CommandSender sender, List<String> resultList, String[] args) {
        if (sender.hasPermission("lazyutils.suffix.other")){
            List<String> playerList = Stream.of(Bukkit.getOfflinePlayers()).parallel().map(OfflinePlayer::getName).collect(Collectors.toList());
            if (args.length == 1){
                return StringUtil.copyPartialMatches(args[0], playerList, resultList);
            }
            
            if (!playerList.contains(args[0])) return resultList;
            
            if (args.length == 2){
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("&0*", "&1*", "&2*", "&3*", "&4*", "&5*", "&6*", "&7*", "&8*", "&9*", "&a*", "&b*", "&c*", "&d*", "&e*", "&f*", "&b&oお&r", "remove"), resultList);
            }
        } else {
            if (args.length == 1){
                return StringUtil.copyPartialMatches(args[0], Arrays.asList("&0*", "&1*", "&2*", "&3*", "&4*", "&5*", "&6*", "&7*", "&8*", "&9*", "&a*", "&b*", "&c*", "&d*", "&e*", "&f*", "&b&oお&r", "remove"), resultList);
            }
        }
        
        return null;
    }

    private List<String> onTabCompleteScoreRanking(List<String> resultList, String[] args) {

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (args.length == 1) {
            List<String> scoreboardList = mainScoreboard.getObjectives().stream()
                    .map(scoreboard -> scoreboard.getDisplayName()).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[0], scoreboardList, resultList);
        }

        Objective obj = mainScoreboard.getObjective(args[0]);
        if (obj == null)
            return resultList;

        long objEntryAmount = mainScoreboard.getEntries().stream().parallel().filter(entry -> obj.getScore(entry).isScoreSet())
                .count();

        if (args.length == 2) {
            List<String> objEntryAmountString = LongStream.range(1, objEntryAmount - 1).boxed().map(String::valueOf)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[1], resultList, objEntryAmountString);
        }

        Long top = Longs.tryParse(args[1]);
        if (top == null)
            return resultList;

        if (args.length == 3) {
            List<String> objEntryAmountString = LongStream.range(top, objEntryAmount).boxed().map(String::valueOf)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[2], objEntryAmountString, resultList);
        }

        return resultList;
    }

    @SuppressWarnings("deprecation")
    private List<String> onTabCompleteUniquePrefix(CommandSender sender, List<String> resultList, String[] args) {

        List<String> operations = new ArrayList<>();
        if (sender.hasPermission("lazyutils.add"))
            operations.add("add");
        if (sender.hasPermission("lazyutils.remove"))
            operations.add("remove");
        if (sender.hasPermission("lazyutils.set"))
            operations.add("set");
        if (sender.hasPermission("lazyutils.list"))
            operations.add("list");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], operations, resultList);
        }

        String operation = args[0].toLowerCase();
        if (!operations.contains(operation))
            return resultList;

        List<String> allPlayers = new ArrayList<>();

        if (args.length == 2) {
            if (sender.hasPermission("lazyutils.uniqueprefix.other")) {
                allPlayers = Stream.of(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName)
                        .collect(Collectors.toList());
                return StringUtil.copyPartialMatches(args[1], allPlayers, resultList);

            } else {
                return uniquePrefixOperation(operation, args[1], (OfflinePlayer) sender, resultList);
            }
        }

        if (args.length == 3) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if (!player.hasPlayedBefore())
                return resultList;
            return uniquePrefixOperation(operation, args[2], player, resultList);
        }

        return resultList;
    }

    private List<String> uniquePrefixOperation(String operation, String input, OfflinePlayer player,
            List<String> resultList) {
        switch (operation) {
        case "add":
            if (input.equals(""))
                return Arrays.asList("&");
            if (input.indexOf("&") == 0)
                return Arrays.asList("&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d",
                        "&e", "&f");
            if (input.matches("&[0-9a-f]"))
                return Arrays.asList(input + "字");
            return resultList;

        case "set":
            List<String> prefixList = LazyUtils.getInstance().getPrefixData().getConfig()
                    .getStringList("Players." + player.getUniqueId().toString());
            if (prefixList.size() == 0)
                return resultList;
            List<String> prefixNumberList = IntStream.range(1, prefixList.size()).boxed().map(String::valueOf)
                    .collect(Collectors.toList());
            prefixList.addAll(prefixNumberList);
            return StringUtil.copyPartialMatches(input, prefixList, resultList);

        case "remove":
            List<String> removedPrefixList = LazyUtils.getInstance().getPrefixData().getConfig()
                    .getStringList("Players." + player.getUniqueId().toString());
            if (removedPrefixList.size() == 0)
                return resultList;
            List<String> removedPrefixNumberList = IntStream.range(1, removedPrefixList.size()).boxed()
                    .map(String::valueOf).collect(Collectors.toList());
            removedPrefixList.addAll(removedPrefixNumberList);
            return StringUtil.copyPartialMatches(input, removedPrefixList, resultList);
        }
        return resultList;
    }

    private List<String> onTabCompleteUuidScoreboard(List<String> resultList, String[] args) {

        List<String> operations = Arrays.asList("add", "set", "get", "remove", "ranking");
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], operations, resultList);
        }

        String operation = args[0];
        if (!operations.contains(operation))
            return resultList;

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        List<String> allPlayersUuid = Stream.of(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getUniqueId)
                .map(UUID::toString).collect(Collectors.toList());
        List<String> objList = mainScoreboard.getObjectives().stream().map(Objective::getDisplayName)
                .collect(Collectors.toList());

        if (args.length == 2) {
            switch (operation) {
            case "add":
                return StringUtil.copyPartialMatches(args[1], allPlayersUuid, resultList);
            case "get":
                return StringUtil.copyPartialMatches(args[1], allPlayersUuid, resultList);
            case "set":
                return StringUtil.copyPartialMatches(args[1], allPlayersUuid, resultList);
            case "remove":
                return StringUtil.copyPartialMatches(args[1], allPlayersUuid, resultList);
            case "ranking":
                return StringUtil.copyPartialMatches(args[1], objList, resultList);
            }
            return resultList;
        }

        if (!operation.equals("ranking") && !allPlayersUuid.contains(args[1]))
            return resultList;
        else

        if (args.length == 3) {
            switch (operation) {
            case "add":
                return StringUtil.copyPartialMatches(args[2], objList, resultList);
            case "get":
                return StringUtil.copyPartialMatches(args[2], objList, resultList);
            case "set":
                return StringUtil.copyPartialMatches(args[2], objList, resultList);
            case "remove":
                return StringUtil.copyPartialMatches(args[2], objList, resultList);
            case "ranking":
                if (!objList.contains(args[2]))
                    return resultList;
                Objective obj = mainScoreboard.getObjective(args[1]);
                if (obj == null)
                    return resultList;
                long entrySize = mainScoreboard.getEntries().stream().parallel().filter(entry -> obj.getScore(entry).isScoreSet())
                        .count();
                if (entrySize <= 1)
                    return resultList;
                List<String> entryAmount = LongStream.range(1, entrySize).boxed().map(String::valueOf)
                        .collect(Collectors.toList());
                return StringUtil.copyPartialMatches(args[2], entryAmount, resultList);
            }
            return resultList;
        }

        if (!operation.equals("ranking") && !objList.contains(args[2]))
            return resultList;

        if (args.length == 4) {
            switch (operation) {
            case "add":
                return StringUtil.copyPartialMatches(args[2], Arrays.asList("1", "10", "100", "1000"), resultList);
            case "set":
                return StringUtil.copyPartialMatches(args[2], Arrays.asList("0", "1", "10", "100", "1000"), resultList);
            case "remove":
                Score score = mainScoreboard.getObjective(args[2]).getScore(args[1]);
                if (!score.isScoreSet())
                    return StringUtil.copyPartialMatches(args[2], Arrays.asList("1", "10", "100", "1000"), resultList);
                return StringUtil.copyPartialMatches(args[2], Arrays.asList(String.valueOf(score.getScore())),
                        resultList);
            case "ranking":
                if (!objList.contains(args[2]))
                    return resultList;
                Objective obj = mainScoreboard.getObjective(args[1]);
                if (obj == null)
                    return resultList;
                long entrySize = mainScoreboard.getEntries().stream().parallel().filter(entry -> obj.getScore(entry).isScoreSet())
                        .count();
                Long top = Longs.tryParse(args[2]);
                if (top == null || top < 0 || top > entrySize)
                    return resultList;
                List<String> entryAmount = LongStream.range(top, entrySize).boxed().map(String::valueOf)
                        .collect(Collectors.toList());
                return StringUtil.copyPartialMatches(args[2], entryAmount, resultList);
            }
            return resultList;
        }
        return resultList;
    }

    private List<String> onTabCompleteSpawner(List<String> resultList, String[] args) {
        List<String> entityList = Stream.of(EntityType.values()).filter(entity -> !Objects.isNull(entity))
                .filter(EntityType::isSpawnable).map(EntityType::name).filter(entity -> !Strings.isNullOrEmpty(entity)).collect(Collectors.toList());
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], entityList, resultList);
        }
        if (!entityList.contains(args[0].toUpperCase()))
            return resultList;
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1],
                    IntStream.range(1, 64).boxed().map(String::valueOf).collect(Collectors.toList()), resultList);
        }
        return resultList;
    }

    private List<String> onTabCompleteRepair(CommandSender sender, List<String> resultList, String[] args) {
        return resultList;
    }
}