package com.github.okocraft.lazyutils.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.okocraft.lazyutils.LazyUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class CommandScoreRanking implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (!(args.length == 3)) {
			sender.sendMessage("§c引数の数は3つです");
			return false;
		}

		if (!LazyUtils.isInt(args[1]) && !LazyUtils.isInt(args[2])) {
			sender.sendMessage("§ctopとbottomの値は正の整数である必要があります");
			return false;
		}

		if (!LazyUtils.isInt(args[1])) {
			sender.sendMessage("§ctopの正の値は整数である必要があります");
			return false;
		}

		if (!LazyUtils.isInt(args[2])) {
			sender.sendMessage("§cbottomの正の値は整数である必要があります");
			return false;
		}

		int top = Integer.parseInt(args[1]);
		int bottom = Integer.parseInt(args[2]);

		if (top < 1) {
			sender.sendMessage("topが小さすぎます");
			return false;
		}

		if (top > bottom) {
			sender.sendMessage("topの値はbottomより小さくなくてはいけません");
			return false;
		}

		Objective Obj = mainScoreboard.getObjective(args[0]);

		if (Obj == null) {
			sender.sendMessage("§b" + args[0] + " §7という名前のobjectiveは見つかりませんでした");
			return false;
		}

		Map<String, Integer> ranking = new HashMap<String, Integer>();

		Set<String> entries = mainScoreboard.getEntries();

		for (String originalentry : entries)
			if (Obj.getScore(originalentry).isScoreSet())
				ranking.put(originalentry, Obj.getScore(originalentry).getScore());

		List<Entry<String, Integer>> rank =
				new ArrayList<Entry<String, Integer>>(ranking.entrySet());

		if (bottom > rank.size()) {
			sender.sendMessage("§cbottomが大きすぎます、エントリー数は §b" + rank.size() + " §cです");
			return false;
		}

		Collections.sort(rank, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> x, Entry<String, Integer> y) {
				return y.getValue().compareTo(x.getValue());
			}
		});

		String result = "§b" + args[0] + " §6ランキング §7(全エントリー)\n";

		for (int i = top; i <= bottom; i++) {
			String blank = "";
			for (int j = 0; j < (40 - rank.get(i - 1).getKey().length()); j++)
				blank += " ";
			result += "§6" + i + "位： §b" + rank.get(i - 1).getKey() + blank + " §a"
					+ rank.get(i - 1).getValue() + "\n";
		}

		sender.sendMessage(result);
		return true;
	}

}
