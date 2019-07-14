package com.github.okocraft.lazyutils.command;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.okocraft.lazyutils.LazyUtils;
import com.google.common.primitives.Longs;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import net.milkbowl.vault.economy.Economy;

@SuppressWarnings("unused")
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
		instance.getCommand("respawn").setExecutor(this);
		instance.getCommand("wgregion").setExecutor(this);
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
				return errorOccurred(sender, "§c引数の数は2つです。");
			}
			int amount = 1;
			if (args.length < 3) {
				try {
					amount = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					return errorOccurred(sender, "§c2つ目の引数には数字を入力してください。");
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
		case "respawn":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return respawnPlayer(sender, args);
		case "wgregion":
			if (!hasPermission(sender, "lazyutils." + commandName))
				return false;
			return PurgeRegion.purgeInactiveRegions(sender);
		}
		return false;
	}

	private boolean respawnPlayer(CommandSender sender, String[] args) {
		if (args.length == 0)
			return errorOccurred(sender, "§c引数が足りません。");

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null)
			return errorOccurred(sender, "§cプレイヤーが見つかりませんでした。");

		if (!target.isDead())
			return errorOccurred(sender, "§cプレイヤーは行きています。");

		target.spigot().respawn();
		return true;
	}

	private boolean seeOldPlayerMoney(CommandSender sender, Command command, String label, String[] args) {

		Economy econ = LazyUtils.getInstance().getEconomy();

		if (args.length == 0)
			return errorOccurred(sender, "第一引数に日数を入力してください。");

		Long day = Longs.tryParse(args[0]);
		if (day == null)
			return errorOccurred(sender, "第一引数の日数は数字を入力してください。");

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
				return errorOccurred(sender, "§c引数は2つです。");

			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
			if (!offlinePlayer.hasPlayedBefore())
				return errorOccurred(sender, "§cそのプレイヤーはログインしたことがありません。");

			if (args[1].equalsIgnoreCase("remove")) {
				String dispatchedCommand = "lp user " + offlinePlayer.getName() + " meta removesuffix 10";
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);
				return true;
			}

			if (args[1].matches(".*&k.*"))
				return errorOccurred(sender, "§c&kは使用できません。");
			String newSuffix = args[1].replaceAll("&", "§");
			if (newSuffix.replaceAll("§.", "").length() != 1)
				return errorOccurred(sender, "§c文字数はカラーコードと、それを除いた1文字だけです");

			String dispatchedCommand = "lp user " + offlinePlayer.getName() + " meta setsuffix 10 " + newSuffix;
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);

			return true;
		} else {
			if (args.length != 1)
				return errorOccurred(sender, "§c引数は1つです。");

			if (args[0].equalsIgnoreCase("remove")) {
				String dispatchedCommand = "lp user " + sender.getName() + " meta removesuffix 10";
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), dispatchedCommand);
				return true;
			}

			if (args[0].matches(".*&k.*"))
				return errorOccurred(sender, "§c&kは使用できません。");
			String newSuffix = args[0].replaceAll("&", "§");
			if (newSuffix.replaceAll("§.", "").length() != 1)
				return errorOccurred(sender, "§c文字数はカラーコードと、それを除いた1文字だけです");

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
		return errorOccurred(sender, "§c" + permission + " の権限がありません");
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
	public static boolean errorOccurred(CommandSender sender, String message) {
		sender.sendMessage(message);
		return false;
	}
}
