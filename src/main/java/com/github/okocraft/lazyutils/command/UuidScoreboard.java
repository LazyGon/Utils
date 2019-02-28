package com.github.okocraft.lazyutils.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class UuidScoreboard {

	//&& (sender instanceof Player && !sender.hasPermission("lazyutil.uuidscoreboard.add") || !(sender instanceof Player))

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("add")
					&& (sender instanceof Player && !sender.hasPermission("lazyutil.uuidscoreboard.add")
							|| !(sender instanceof Player))) {
				sender.sendMessage("§clazyutils.uuidscoreboard.add の権限がありません");
				return true;
			} else if (args[0].equalsIgnoreCase("get")
					&& (sender instanceof Player && !sender.hasPermission("lazyutil.uuidscoreboard.add")
							|| !(sender instanceof Player))) {
				sender.sendMessage("§clazyutils.uuidscoreboard.get の権限がありません");
				return true;
			} else if (args[0].equalsIgnoreCase("set")
					&& (sender instanceof Player && !sender.hasPermission("lazyutil.uuidscoreboard.add")
							|| !(sender instanceof Player))) {
				sender.sendMessage("§clazyutils.uuidscoreboard.set の権限がありません");
				return true;
			} else if (args[0].equalsIgnoreCase("remove")
					&& (sender instanceof Player && !sender.hasPermission("lazyutil.uuidscoreboard.add")
							|| !(sender instanceof Player))) {
				sender.sendMessage("§clazyutils.uuidscoreboard.remove の権限がありません");
				return true;
			} else if (args[0].equalsIgnoreCase("ranking")
					&& (sender instanceof Player && !sender.hasPermission("lazyutil.uuidscoreboard.ranking")
							|| !(sender instanceof Player))) {
				sender.sendMessage("§clazyutils.uuidscoreboard.ranking の権限がありません");
				return true;
			}
		}

		if (!(args.length == 4 || args.length == 3)) {
			sender.sendMessage("§c引数の数を間違えています。引数の数は3つか4つである必要があります");
			return true;
		}

		if (args.length != 3 && args[0].equalsIgnoreCase("get")) {
			sender.sendMessage("§c引数の数を間違えています。getの場合は引数は3つです");
			return true;
		}

		if (args.length != 4 && (!args[0].equalsIgnoreCase("get"))) {
			sender.sendMessage("§c引数の数を間違えています。get以外の場合は引数は4つです");
			return true;
		}

		if (!(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")
				|| args[0].equalsIgnoreCase("ranking") || args[0].equalsIgnoreCase("remove"))) {
			sender.sendMessage("§c最初の引数が間違えています");
			return true;
		}

		Scoreboard MainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		if (!args[0].equalsIgnoreCase("ranking")) {
			if (args.length == 4) {
				if (!Commands.isInt(args[3])) {
					sender.sendMessage("§cnumberは整数である必要があります");
					return true;
				}
			}

			OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

			if (!player.hasPlayedBefore()) {
				sender.sendMessage("§b" + args[1].toLowerCase() + " §cのUUIDは見つかりませんでした");
				return true;
			}

			String PlayerUuid = player.getUniqueId().toString();

			Objective Obj = MainScoreboard.getObjective(args[2]);

			if (Obj == null) {
				sender.sendMessage("§b" + args[2] + " §cというobjectiveは見つかりませんでした");
				return true;
			}

			String FinishMessage = "";
			int GivenScore = (args.length == 4) ? Integer.parseInt(args[3]) : 0;
			int CurrentScore = Obj.getScore(player.getUniqueId().toString()).getScore();

			if (args[0].equalsIgnoreCase("add")) {
				Obj.getScore(PlayerUuid).setScore(GivenScore + CurrentScore);
				FinishMessage = PlayerUuid + " の [" + args[2] + "] を " + GivenScore + " 加算しました（現在 "
						+ (GivenScore + CurrentScore) + "）";
				sender.sendMessage(FinishMessage);
				return true;
			}

			if (args[0].equalsIgnoreCase("get")) {
				FinishMessage = PlayerUuid + " は [" + args[2] + "] を " + CurrentScore + " 持っています";
				sender.sendMessage(FinishMessage);
				return true;
			}

			if (args[0].equalsIgnoreCase("set")) {
				Obj.getScore(PlayerUuid).setScore(GivenScore);
				FinishMessage = PlayerUuid + " の [" + args[2] + "] を " + GivenScore + " に設定しました";
				sender.sendMessage(FinishMessage);
				return true;
			}

			if (args[0].equalsIgnoreCase("remove")) {
				Obj.getScore(PlayerUuid).setScore(CurrentScore - GivenScore);
				FinishMessage = PlayerUuid + " の [" + args[2] + "] を " + GivenScore + " 減算しました（現在 "
						+ (CurrentScore - GivenScore) + "）";
				sender.sendMessage(FinishMessage);
				return true;
			}
		} else if (args[0].equalsIgnoreCase("ranking")) {

			if (!Commands.isInt(args[2]) && !Commands.isInt(args[3])) {
				sender.sendMessage("§ctopとbottomの値は正の整数である必要があります");
				return true;
			}

			if (!Commands.isInt(args[2])) {
				sender.sendMessage("§ctopの正の値は整数である必要があります");
				return true;
			}

			if (!Commands.isInt(args[3])) {
				sender.sendMessage("§cbottomの正の値は整数である必要があります");
				return true;
			}

			int top = Integer.parseInt(args[2]);
			int bottom = Integer.parseInt(args[3]);

			if (top < 1) {
				sender.sendMessage("topが小さすぎます");
				return true;
			}

			if (top > bottom) {
				sender.sendMessage("topの値はbottomより小さくなくてはいけません");
				return true;
			}

			OfflinePlayer[] Entries = Bukkit.getOfflinePlayers();

			if (bottom > Entries.length) {
				sender.sendMessage("§bottomが大きすぎます、エントリー数は §b"+Entries.length+" §cです");
				return true;
			}

			Objective Obj = MainScoreboard.getObjective(args[1]);

			if (Obj == null) {
				sender.sendMessage("§b" + args[1] + " §7という名前のobjectiveは見つかりませんでした");
				return true;
			}

			Map<String, Integer> Ranking = new HashMap<String, Integer>();

			for (OfflinePlayer originalentry : Entries)
				if(Obj.getScore(originalentry.getUniqueId().toString()).isScoreSet()){
					Ranking.put(originalentry.getName(), Obj.getScore(originalentry.getUniqueId().toString()).getScore());
				}

			List<Entry<String, Integer>> rank = new ArrayList<Entry<String, Integer>>(Ranking.entrySet());

			Collections.sort(rank, new Comparator<Entry<String, Integer>>() {
				public int compare(Entry<String, Integer> x, Entry<String, Integer> y) {
					return y.getValue().compareTo(x.getValue());
				}
			});

			String result = "§b" + args[1] + " §6ランキング §7(UUID調べ)\n";

			for (int i = top; i <= bottom; i++) {
				String blank = "";
				for (int j = 0; j < (16 - rank.get(i - 1).getKey().length()); j++)
					blank += " ";
				result += "§6" + i + "位： §b" + rank.get(i - 1).getKey() + blank + " §a" + rank.get(i - 1).getValue()
						+ "\n";
			}

			sender.sendMessage(result);
			return true;

		}
		return false;
	}
}
