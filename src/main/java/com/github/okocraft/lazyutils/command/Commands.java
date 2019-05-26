package com.github.okocraft.lazyutils.command;

import java.util.stream.Stream;

import com.github.okocraft.lazyutils.LazyUtils;
import com.google.common.primitives.Longs;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.milkbowl.vault.economy.Economy;

public class Commands implements CommandExecutor {

	private UuidScoreboard commandUuidScoreboard = new UuidScoreboard();
	private UniquePrefix commandUniquePrefix = new UniquePrefix();
	private ScoreRanking commandScoreRanking = new ScoreRanking();

	public Commands() {
		LazyUtils instance = LazyUtils.getInstance();

		instance.getCommand("uniqueprefix").setExecutor(this);
		instance.getCommand("suffix").setExecutor(this);
		instance.getCommand("uuidscoreboard").setExecutor(this);
		instance.getCommand("scoreranking").setExecutor(this);
		instance.getCommand("costrepair").setExecutor(this);
		instance.getCommand("getspawner").setExecutor(this);
		instance.getCommand("oldplayermoney").setExecutor(this);
		instance.getCommand("moreunbreaking").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		String commandName = command.getName().toLowerCase();

		switch (commandName) {
		case "uuidscoreboard":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return commandUuidScoreboard.onCommand(sender, command, label, args);
		case "getspawner":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			if (args.length != 2) {
				return errorOccured(sender, "§c引数の数は2つです。");
			}
			int amount = 1;
			if (args.length < 3) {
				try {
					amount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					return errorOccured(sender, "§c2つ目の引数には数字を入力してください。");
				}
			}
			return Spawner.giveSpawner(sender, args[0], amount);
		case "suffix":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return setSuffix(sender, command, label, args);
		case "uniqueprefix":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return commandUniquePrefix.onCommand(sender, command, label, args);
		case "scoreranking":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return commandScoreRanking.onCommand(sender, command, label, args);
		case "costrepair":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return Repair.onCommand(sender, command, label, args);
		case "oldplayermoney":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return seeOldPlayerMoney(sender, command, label, args);
		case "moreunbreaking":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return MoreUnbreaking.addUnbreaking((Player) sender);
		}
		return false;
	}

	private boolean seeOldPlayerMoney(CommandSender sender, Command command, String label, String[] args) {

		Economy econ = LazyUtils.getInstance().getEconomy();

		if (args.length == 0)
			return errorOccured(sender, "第一引数に日数を入力してください。");

		Long day = Longs.tryParse(args[0]);
		if (day == null)
			return errorOccured(sender, "第一引数の日数は数字を入力してください。");

		double oldPlayerMoneyTotal = Stream.of(Bukkit.getOfflinePlayers()).parallel()
				.filter(OfflinePlayer::hasPlayedBefore)
				.filter(player -> (System.currentTimeMillis() - player.getLastPlayed()) / (1000 * 3600 * 24) > day)
				.mapToDouble(econ::getBalance).filter(number -> number > 2000).map(number -> number - 2000).sum();
		sender.sendMessage(oldPlayerMoneyTotal + "");
		return true;
	}

	/**
	 * おこ鯖の設定に沿って、suffixを変更する
	 * 
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 * @return 成功すればtrue 失敗すればfalse
	 */
	@SuppressWarnings("deprecation")
	private boolean setSuffix(CommandSender sender, Command command, String label, String[] args) {

		if (sender.hasPermission("lazyutils.suffix.other")) {
			if (args.length != 2)
				return errorOccured(sender, "§c引数は2つです。");

			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
			if (!offlinePlayer.hasPlayedBefore())
				return errorOccured(sender, "§cそのプレイヤーはログインしたことがありません。");

			if (args[1].equalsIgnoreCase("remove")) {
				String dispatchedCommand = "lp user " + offlinePlayer.getName() + " meta removesuffix 10";
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);
				return true;
			}

			if (args[1].matches(".*&k.*"))
				return errorOccured(sender, "§c&kは使用できません。");
			String newSuffix = args[1].replaceAll("&", "§");
			if (newSuffix.replaceAll("§.", "").length() != 1)
				return errorOccured(sender, "§c文字数はカラーコードと、それを除いた1文字だけです");

			String dispatchedCommand = "lp user " + offlinePlayer.getName() + " meta setsuffix 10 " + newSuffix;
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);

			return true;
		} else {
			if (args.length != 1)
				return errorOccured(sender, "§c引数は1つです。");

			if (args[0].equalsIgnoreCase("remove")) {
				String dispatchedCommand = "lp user " + sender.getName() + " meta removesuffix 10";
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);
				return true;
			}

			if (args[0].matches(".*&k.*"))
				return errorOccured(sender, "§c&kは使用できません。");
			String newSuffix = args[0].replaceAll("&", "§");
			if (newSuffix.replaceAll("§.", "").length() != 1)
				return errorOccured(sender, "§c文字数はカラーコードと、それを除いた1文字だけです");

			String dispatchedCommand = "lp user " + sender.getName() + " meta setsuffix 10 " + newSuffix;
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);

			return true;
		}
	}

	/**
	 * 権限がない場合にプレイヤーにメッセージを送りつつfalseを返す。
	 * 
	 * @author LazyGon
	 * 
	 * @param sender
	 * @param permission
	 * 
	 * @return 権限があればtrue、なければfalse
	 */
	public static boolean hasPermission(CommandSender sender, String permission) {
		if (!(sender instanceof Player) || sender.hasPermission(permission))
			return true;
		return errorOccured(sender, "§c" + permission + " の権限がありません");
	}

	/**
	 * プレイヤーにエラーメッセージを送りつつfalseを返す。
	 * 
	 * @author LazyGon
	 * 
	 * @param sender
	 * @param message エラーメッセージ。
	 * 
	 * @return false
	 */
	public static boolean errorOccured(CommandSender sender, String message) {
		sender.sendMessage(message);
		return false;
	}
}
