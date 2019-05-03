package com.github.okocraft.lazyutils.command;

import java.util.List;

import com.github.okocraft.lazyutils.LazyUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class UniquePrefix {

    private static FileConfiguration prefixData = LazyUtils.getInstance().getPrefixData().getConfig();


    /**
     * {@code args}からどのメソッドを実行するか決定する。 また、実行するメソッドに渡す引数を生成する。
     * /upref operation [player] 
     * 
     * @author LazyGon
     * 
     * @param sender
     * @param command
     * @param label
     * @param args
     * 
     * @return 成功すればtrue、失敗すればfalse
     */
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0){
            help(sender);
            return true;
        }

        String newPrefix = "";
        String prefixNumber = "";

        boolean ifUsingOtherPlayer = false;

        OfflinePlayer offlinePlayer;
        try {
            offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (offlinePlayer.hasPlayedBefore() && sender.hasPermission("lazyutils.uniqueprefix.other")) {
                ifUsingOtherPlayer = true;
            } else {
                offlinePlayer = (OfflinePlayer) sender;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cプレイヤーが指定されていません。");
                return false;
            } else {
                offlinePlayer = (OfflinePlayer) sender;
            }
        }

        String operation = args[0].toLowerCase();

        switch (operation) {

        case "add":
            if (!sender.hasPermission("lazyutils.uniqueprefix." + operation))
                return false;
            if (ifUsingOtherPlayer && args.length <= 3)
                newPrefix = args[2];
            if (!ifUsingOtherPlayer && args.length <= 2)
                newPrefix = args[1];
            if (newPrefix.equals("")) {
                sender.sendMessage("§c登録する称号が入力されていません。");
                return false;
            }
            return addUniquePrefix(sender, offlinePlayer, newPrefix);

        case "remove":
            if (!Commands.hasPermission(sender, "lazyutils.uniqueprefix." + operation))
                return false;
            if (ifUsingOtherPlayer && args.length == 3)
                prefixNumber = args[2];
            if (!ifUsingOtherPlayer && args.length == 2)
                prefixNumber = args[1];
            return removeUniquePrefix(sender, offlinePlayer, prefixNumber);

        case "set":
            if (!Commands.hasPermission(sender, "lazyutils.uniqueprefix." + operation))
                return false;
            if (ifUsingOtherPlayer && args.length == 3)
                prefixNumber = args[2];
            if (!ifUsingOtherPlayer && args.length == 2)
                prefixNumber = args[1];
            return setUniquePrefix(sender, offlinePlayer, prefixNumber);

        case "list":
            if (!Commands.hasPermission(sender, "lazyutils.uniqueprefix." + operation)) {
                return false;
            }
            return listUniquePrefix(sender, offlinePlayer);

        default:
            help(sender);
            return false;
        }
    }

    /**
     * 指定したプレイヤーに称号を追加し、データファイルに保存する。 プレイヤーを指定していなければ自分を対象にする。
     * 
     * @author LazyGon
     * 
     * @param sender
     * @param player
     * @param newPrefix
     * 
     * @return true
     */
    private static boolean addUniquePrefix(CommandSender sender, OfflinePlayer player, String newPrefix) {

        String playerUuid = player.getUniqueId().toString();

        List<String> usedPrefixList = prefixData.getStringList("UsedPrefixes");
        List<String> uniquePrefixList = prefixData.getStringList("Players." + playerUuid);

        if (usedPrefixList.contains(newPrefix)) {
            sender.sendMessage("§cその称号は既に使われています");
            return false;
        }

        if (!newPrefix.matches("&([0-9]|[a-f])(\\p{InHiragana}|\\p{InKatakana}|\\p{InCjkUnifiedIdeographs})")) {
            sender.sendMessage("§c称号の構文を間違えています\n§f構文: &(0～9 or a～f) + 日本語1文字\n例: &9お");
            return false;
        }

        // ファイルに情報を追加
        uniquePrefixList.add(newPrefix);
        usedPrefixList.add(newPrefix);
        prefixData.set("Players." + playerUuid, uniquePrefixList);
        prefixData.set("UsedPrefixes", usedPrefixList);

        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + player.getName() + " meta setprefix 1 &7[" + newPrefix + "&7]");
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName()
                + " permission unset lazyutils.uniqueprefix.add");
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName()
                + " permission unset okocraft.legendaryshop.tag.buy");

        sender.sendMessage("§8[§6固有称号§8]§7 プレイヤー §b" + player.getName() + " §7に固有称号 §r" + newPrefix.replaceAll("&", "§")
                + " §7を追加しました");

        return true;
    }

    /**
     * 指定したプレイヤーに称号を削除し、データファイルに保存する。 プレイヤーを指定していなければ自分を対象にする。
     * 
     * @author LazyGon
     * 
     * @param sender
     * @param player
     * @param prefixNumber
     * 
     * @return true
     */
    private static boolean removeUniquePrefix(CommandSender sender, OfflinePlayer player, String prefixRequested) {

        String playerUuid = player.getUniqueId().toString();

        List<String> usedPrefixList = prefixData.getStringList("UsedPrefixes");
        List<String> uniquePrefixList = prefixData.getStringList("Players." + playerUuid);

        int prefixNumber = 0;

        try {
            prefixNumber = Integer.parseInt(prefixRequested);
        } catch (NumberFormatException e) {
            prefixNumber = uniquePrefixList.indexOf(prefixRequested) + 1;
        }

        if (uniquePrefixList.size() < prefixNumber || prefixNumber < 1) {
            sender.sendMessage("§cその固有称号は存在しません。/upref listで所有する固有称号を確認してください。（削除可能な固有称号の番号: 1 ～ "
                    + uniquePrefixList.size() + "）");
            return false;
        }

        String targetPrefix = uniquePrefixList.get(prefixNumber - 1);
        int indexOnUsedList = usedPrefixList.indexOf(targetPrefix);

        // 取得したプレフィックスを削除してファイルに保存
        uniquePrefixList.remove(prefixNumber - 1);
        usedPrefixList.remove(indexOnUsedList);
        prefixData.set("Players." + playerUuid, uniquePrefixList);
        prefixData.set("UsedPrefixes", usedPrefixList);

        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + player.getName() + " meta removeprefix 1 &7[" + targetPrefix + "&7]");

        sender.sendMessage("§8[§6固有称号§8]§7 プレイヤー §b" + player.getName() + " §7の固有称号 §r"
                + targetPrefix.replaceAll("&", "§") + " §7を削除しました");
        return true;
    }

    /**
     * 指定したプレイヤーの称号リストを参照し、そのプレイヤーに称号をセットする。 プレイヤーを指定していなければ自分を対象にする。
     * 
     * @author LazyGon
     * 
     * @param sender
     * @param player
     * @param prefixNumber
     * 
     * @return true
     */
    private static boolean setUniquePrefix(CommandSender sender, OfflinePlayer player, String requestedPrefix) {

        String playerUuid = player.getUniqueId().toString();
        String playerName = player.getName();

        List<String> uniquePrefixList = prefixData.getStringList("Players." + playerUuid);

        String selectedPrefix;

        try {
            int prefixNumber = Integer.parseInt(requestedPrefix);

            if (uniquePrefixList.size() < prefixNumber || prefixNumber < 1) {
                sender.sendMessage("§c固有称号の番号が間違えています（範囲: 1 ～ " + uniquePrefixList.size() + "）");
                return false;
            }

            selectedPrefix = uniquePrefixList.get(prefixNumber - 1);
        } catch (NumberFormatException e) {
            if (!uniquePrefixList.contains(requestedPrefix)) {
                sender.sendMessage("§cその固有称号を持っていません。");
                return false;
            }
            selectedPrefix = requestedPrefix;
        }

        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                "lp user " + playerName + " meta setprefix 1 &7[" + selectedPrefix + "&7]");

        sender.sendMessage("§8[§6固有称号§8]§7 プレイヤー §b" + playerName + " §7の固有称号 §r" + selectedPrefix.replaceAll("&", "§")
                + " §7をセットしました");
        return true;
    }

    /**
     * 指定したプレイヤーの固有称号リストを表示する。 プレイヤーを指定しなければ自分のリストを表示する。
     * 
     * @author LazyGon
     * 
     * @param sender
     * @param player
     * 
     * @return true
     */
    private static boolean listUniquePrefix(CommandSender sender, OfflinePlayer player) {

        String playerUuid = player.getUniqueId().toString();

        List<String> uniquePrefixList = prefixData.getStringList("Players." + playerUuid);

        sender.sendMessage("§6固有称号リスト ( §b" + player.getName() + " §6)");
        for (String prefix : uniquePrefixList) {
            sender.sendMessage("§6#" + (uniquePrefixList.indexOf(prefix) + 1) + " §r" + prefix);
        }

        return true;
    }

    /**
     * 使用者が権限をもつコマンドの使い方を表示する addコマンドは例外として常に表示されている
     * 
     * @author LazyGon
     * 
     * @param sender
     * 
     */
    private static void help(CommandSender sender) {

        String otherArgument = "";
        if (sender.hasPermission("lazyutils.uniqueprefix.other"))
            otherArgument = "[player] ";

        sender.sendMessage("§6固有称号 コマンドリスト");
        sender.sendMessage("§b/uniqueprefix add " + otherArgument + "<prefix> §7- 固有称号を追加する。\nレジェチケと引き換えに1つ分の権限を得る。");

        if (sender.hasPermission("lazyutils.uniqueprefix.remove")) {
            sender.sendMessage("§b/uniqueprefix remove " + otherArgument
                    + "<prefix|number> §7- 固有称号を削除する。\n§7prefixとnumberは/uniqueprefix listから確認できる。");
        }

        if (sender.hasPermission("lazyutils.uniqueprefix.set")) {
            sender.sendMessage("§b/uniqueprefix set " + otherArgument
                    + "<prefix|number> §7- 固有称号をセットする。\n§7prefixとnumberは/uniqueprefix listから確認できる。");
        }

        if (sender.hasPermission("lazyutils.uniqueprefix.list")) {
            sender.sendMessage("§b/uniqueprefix list " + otherArgument + "§7- 固有称号のリストを見る。");
        }
    }
}
