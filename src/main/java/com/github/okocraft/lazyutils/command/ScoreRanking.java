package com.github.okocraft.lazyutils.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreRanking {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Scoreboard MainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (!(args.length == 3))
			return Commands.errorOccured(sender, "§c引数の数は3つです 構文: /scoreranking <objective> <top> <bottom>");

		int top;
		int bottom;

		try {
			top = Integer.parseInt(args[1]);
			bottom = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			return Commands.errorOccured(sender, "§c2つ目と3つ目の引数は数字である必要があります");
		}

		if (top < 1)
			return Commands.errorOccured(sender, "§ctopが小さすぎます");

		if (top > bottom)
			return Commands.errorOccured(sender, "§ctopの値はbottomより小さくなくてはいけません");

		Objective Obj = MainScoreboard.getObjective(args[0]);

		if (Obj == null)
			return Commands.errorOccured(sender, "§b" + args[0] + " §7という名前のobjectiveは見つかりませんでした");

		Map<String, Integer> resultMap = MainScoreboard.getEntries().stream().parallel()
				.filter(entry -> Obj.getScore(entry).isScoreSet())
				.collect(Collectors
						.toMap(entry -> entry, entry -> Obj.getScore(entry).getScore(), (e1, e2) -> e1, HashMap::new))
				.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
						.thenComparing(Map.Entry.comparingByKey())).skip(top - 1).limit(bottom)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		List<Entry<String, Integer>> resultList = new ArrayList<>(resultMap.entrySet());

		sender.sendMessage("§b" + args[0] + " §6ランキング §7(全エントリー)");
		for (Map.Entry<String, Integer> entry : resultList) {
			sender.sendMessage("§6" + (top + resultList.indexOf(entry)) + "位:" + " §b"
					+ String.format("%-40s", entry.getKey()) + " §a" + entry.getValue());
		}
		return true;
	}

}
