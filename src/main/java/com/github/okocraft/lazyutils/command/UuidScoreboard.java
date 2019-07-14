package com.github.okocraft.lazyutils.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class UuidScoreboard {

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0)
			return Commands.errorOccurred(sender, "§c第一引数を指定してください。");

		String operation = args[0].toLowerCase();

		Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective obj = null;
		String playerUuid = "";

		int givenScore = 0;

		OfflinePlayer player;

		if (!args[0].equalsIgnoreCase("ranking")) {

			if (args.length != 3)
				return Commands.errorOccurred(sender, "§c構文が間違えています。");

			player = Bukkit.getOfflinePlayer(args[1]);

			playerUuid = player.getUniqueId().toString();

			obj = mainScoreboard.getObjective(args[2]);

			if (!player.hasPlayedBefore())
				return Commands.errorOccurred(sender, "§b" + args[1].toLowerCase() + " §cのUUIDは見つかりませんでした");

			if (obj == null)
				return Commands.errorOccurred(sender, "§b" + args[2] + " §cというobjectiveは見つかりませんでした");
		}

		if (!args[0].equalsIgnoreCase("ranking") && !args[0].equalsIgnoreCase("get")) {

			if (args.length != 4)
				return Commands.errorOccurred(sender, "§c構文が間違えています。");

			try {
				givenScore = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				return Commands.errorOccurred(sender, "§cnumberには数字を指定してください。");
			}
		}

		switch (operation) {

		case "add":
			if (!Commands.hasPermission(sender, "lazyutils.uuidscoreboard." + operation))
				return false;
			return add(sender, playerUuid, obj, givenScore);

		case "get":
			if (!Commands.hasPermission(sender, "lazyutils.uuidscoreboard." + operation))
				return false;
			return get(sender, playerUuid, obj);

		case "set":
			if (!Commands.hasPermission(sender, "lazyutils.uuidscoreboard." + operation))
				return false;
			return set(sender, playerUuid, obj, givenScore);

		case "remove":
			if (!Commands.hasPermission(sender, "lazyutils.uuidscoreboard." + operation))
				return false;
			return remove(sender, playerUuid, obj, givenScore);

		case "ranking":
			int top, bottom;

			if (!Commands.hasPermission(sender, "lazyutils.uuidscoreboard." + operation))
				return false;
			try {
				top = Integer.parseInt(args[2]);
				bottom = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				return Commands.errorOccurred(sender, "§ctopとbottomには数字を指定してください。");
			}

			obj = mainScoreboard.getObjective(args[1]);
			if (obj == null)
				return Commands.errorOccurred(sender, "§b" + args[1] + " §cというobjectiveは見つかりませんでした");

			return ranking(sender, obj, top, bottom);

		default:
			help(sender);
			return true;

		}
	}

	private static boolean add(CommandSender sender, String playerUuid, Objective obj, int givenScore) {
		int currentScore = obj.getScore(playerUuid).getScore();
		obj.getScore(playerUuid).setScore(givenScore + currentScore);
		sender.sendMessage(playerUuid + " の [" + obj.getName() + "] を " + givenScore + " 加算しました（現在 "
				+ (givenScore + currentScore) + "）");
		return true;
	}

	private static boolean get(CommandSender sender, String playerUuid, Objective obj) {
		int currentScore = obj.getScore(playerUuid).getScore();
		sender.sendMessage(playerUuid + " は [" + obj.getName() + "] を " + currentScore + " 持っています");
		return true;
	}

	private static boolean set(CommandSender sender, String playerUuid, Objective obj, int givenScore) {
		obj.getScore(playerUuid).setScore(givenScore);
		sender.sendMessage(playerUuid + " の [" + obj.getName() + "] を " + givenScore + " に設定しました");
		return true;
	}

	private static boolean remove(CommandSender sender, String playerUuid, Objective obj, int givenScore) {
		int currentScore = obj.getScore(playerUuid).getScore();
		obj.getScore(playerUuid).setScore(currentScore - givenScore);
		sender.sendMessage(playerUuid + " の [" + obj.getName() + "] を " + givenScore + " 減算しました（現在 "
				+ (currentScore - givenScore) + "）");
		return true;
	}

	private static boolean ranking(CommandSender sender, Objective obj, int top, int bottom) {

		if (top < 1)
			return Commands.errorOccurred(sender, "§ctopが小さすぎます");

		if (top > bottom)
			return Commands.errorOccurred(sender, "§ctopの値はbottom以下でなくてはいけません");

		Map<String, Integer> resultMap = Stream.of(Bukkit.getOfflinePlayers()).parallel()
				.filter(player -> obj.getScore(player.getUniqueId().toString()).isScoreSet())
				.collect(Collectors.toMap(entry -> entry.getName(),
						entry -> obj.getScore(entry.getUniqueId().toString()).getScore(), (e1, e2) -> e1, HashMap::new))
				.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
						.thenComparing(Map.Entry.comparingByKey())).skip(top - 1).limit(bottom)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		List<Entry<String, Integer>> resultList = new ArrayList<>(resultMap.entrySet());

		sender.sendMessage("§b" + obj.getName() + " §6ランキング §7(全エントリー)");
		for (Map.Entry<String, Integer> entry : resultList) {
			sender.sendMessage("§6" + (top + resultList.indexOf(entry)) + "位:" + " §b"
					+ String.format("%-40s", entry.getKey()) + " §a" + entry.getValue());
		}
		return true;
	}

	/**
	 * 使用者が権限をもつコマンドの使い方を表示する。
	 * 
	 * @author LazyGon
	 * 
	 * @param sender
	 * 
	 */
	private static void help(CommandSender sender) {

		sender.sendMessage("§6UUIDスコアボード コマンドリスト");

		if (sender.hasPermission("lazyutils.uuidscoreboard.add")) {
			sender.sendMessage(
					"§b/uuidscoreboard add <player> <objective> <number> §7- 指定したプレイヤーをUUID形式のエントリーに変換してスコアを増やす。");
		}

		if (sender.hasPermission("lazyutils.uuidscoreboard.remove")) {
			sender.sendMessage(
					"§b/uuidscoreboard remove <player> <objective> <number> §7- 指定したプレイヤーをUUID形式のエントリーに変換してスコアを減らす。");
		}

		if (sender.hasPermission("lazyutils.uuidscoreboard.set")) {
			sender.sendMessage(
					"§b/uuidscoreboard set <player> <objective> <number> §7- 指定したプレイヤーをUUID形式のエントリーに変換してスコアをセットする。");
		}

		if (sender.hasPermission("lazyutils.uuidscoreboard.get")) {
			sender.sendMessage("§b/uuidscoreboard get <player> <objective> §7- 指定したプレイヤーをUUID形式のエントリーに変換してスコアを取得する。");
		}

		if (sender.hasPermission("lazyutils.uuidscoreboard.ranking")) {
			sender.sendMessage(
					"§b/uuidscoreboard ranking <objective> <top> <bottom> §7- objectiveに含まれるUUID形式のエントリーのみで順位づけする。");
		}

	}
}
