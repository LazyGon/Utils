package lazyutils.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import lazyutils.plugin.command.Commands;

public class LazyUtils extends JavaPlugin implements CommandExecutor{

	protected static LazyUtils instance;

	public static LazyUtils getInstance() {
		if(instance == null) {
			instance = (LazyUtils) Bukkit.getPluginManager().getPlugin("LazyUtils");
		}
		return instance;
	}

	private Commands commands = new Commands();
	private LazyUtilsConfig config = new LazyUtilsConfig();

	@Override
	public void onEnable() {
		super.onEnable();

		config.loadConfig();

		getCommand("filer").setExecutor(this);
		getCommand("uuidscoreboard").setExecutor(this);
		getCommand("scoreranking").setExecutor(this);

		this.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return commands.dispatch(sender, command, label, args);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}
}