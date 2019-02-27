package lazyutils.plugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Commands{

	private Filer CommandFiler = new Filer();
	private UuidScoreboard CommandUuidScoreboard = new UuidScoreboard();
	private ScoreRanking CommandScoreRanking = new ScoreRanking();

	public boolean dispatch(CommandSender sender, Command command, String label, String[] args) {

		if (command.getName().equalsIgnoreCase("filer"))
			return CommandFiler.onCommand(sender, command, label, args);
		if (command.getName().equalsIgnoreCase("uuidscoreboard"))
			return CommandUuidScoreboard.onCommand(sender, command, label, args);
		if (command.getName().equalsIgnoreCase("scoreranking"))
			return CommandScoreRanking.onCommand(sender, command, label, args);

		return true;
	}

	public static boolean isInt(String arg) {
		//-2147483648から+2147483647
		if (arg.matches(
				"^(-(2(1(4(7(4(8(3(6(4([0-8])|[0-3][0-9])|[0-5][0-9]{2})|[0-2][0-9]{3})|[0-7][0-9]{4})|[0-3][0-9]{5})|[0-6][0-9]{6})|[0-3][0-9]{7})|0[0-9]{8})|1[0-9]{9}|[1-9][0-9]{9}|[1-9][0-9]{8}|[1-9][0-9]{7}|[1-9][0-9]{6}|[1-9][0-9]{5}|[1-9][0-9]{4}|[1-9][0-9]{3}|[1-9][0-9]{2}|[1-9][0-9]|[1-9])|(2(1(4(7(4(8(3(6(4([0-7])|[0-3][0-9])|[0-5][0-9]{2})|[0-2][0-9]{3})|[0-7][0-9]{4})|[0-3][0-9]{5})|[0-6][0-9]{6})|[0-3][0-9]{7})|0[0-9]{8})|1[0-9]{9}|[1-9][0-9]{9}|[1-9][0-9]{8}|[1-9][0-9]{7}|[1-9][0-9]{6}|[1-9][0-9]{5}|[1-9][0-9]{4}|[1-9][0-9]{3}|[1-9][0-9]{2}|[1-9][0-9]|[0-9]))$")) {
			return true;
		} else {
			return false;
		}
	}
}
