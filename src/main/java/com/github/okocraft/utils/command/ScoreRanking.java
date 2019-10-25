package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.okocraft.utils.config.Messages;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.StringUtil;

public class ScoreRanking extends UtilsCommand {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }

		Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = mainScoreboard.getObjective(args[0]);

		if (objective == null) {
			Messages.sendMessage(sender, "command.score-ranking.error.objective-does-not-exist");
			return false;
		}

		int page;
		try {
			page = Integer.parseInt(args[1]);
			if (page < 1) {
				throw new NumberFormatException("The page must be more than 1");
			}
		} catch (NumberFormatException e) {
			Messages.sendMessage(sender, "command.general.error.invalid-number");
			return false;
		}

		List<String> entries = new ArrayList<>(mainScoreboard.getEntries());
		entries.removeIf(entry -> !objective.getScore(entry).isScoreSet());
		int entrySize = entries.size();
		int maxPage = entrySize % 9 == 0 ? entrySize / 9 : entrySize / 9 + 1;
		page = Math.min(page, maxPage);

		new BukkitRunnable() {

			@Override
			public void run() {

			}
		};

		Collections.sort(entries, (e1, e2) -> objective.getScore(e1).getScore() - objective.getScore(e2).getScore());
		Messages.sendMessage(sender, "command.score-ranking.info.header",
				Map.of("%scoreboard%", objective.getName(), "%page%", page, "%max-page%", maxPage));

		for (int i = 0; i <= entrySize; i++) {
			Messages.sendMessage(sender, false, "command.score-ranking.info.format", Map.of("%rank%", i + 1, "%entry%",
					String.format("%-40s", entries.get(i)), "%score%", objective.getScore(entries.get(i)).getScore()));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		List<String> objectives = mainScoreboard.getObjectives().stream()
				.map(Objective::getName).collect(Collectors.toList());
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], objectives, result);
		}

		if (!objectives.contains(args[0])) {
			return result;
		}

		int entrySize = (int) mainScoreboard.getEntries().stream()
				.filter(entry -> mainScoreboard.getObjective(args[0]).getScore(entry).isScoreSet()).count();
		int maxPage = entrySize % 9 == 0 ? entrySize / 9 : entrySize / 9 + 1;

		if (args.length == 2) {
			List<String> pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());
			return StringUtil.copyPartialMatches(args[1], pages, result);
		}

		return List.of();

	}

	@Override
	int getLeastArgsLength() {
		return 2;
	}

	@Override
	String getUsage() {
		return "/scoreranking <scoreboard> [page]";
	}

}
