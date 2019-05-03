package com.github.okocraft.lazyutils;

import com.github.okocraft.lazyutils.command.CommandScoreRanking;
import com.github.okocraft.lazyutils.command.CommandUuidScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class LazyUtils extends JavaPlugin {

	private static LazyUtils instance;
	private LazyUtilsConfig config;

	@Override
	public void onEnable() {
		super.onEnable();

		config = new LazyUtilsConfig();

		getCommand("uuidscoreboard").setExecutor(new CommandUuidScoreboard());
		getCommand("scoreranking").setExecutor(new CommandScoreRanking());

		this.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	public static boolean isInt(String arg) {
		try{
			Integer.parseInt(arg);
			return true;
		}catch (NumberFormatException e){
			return false;
		}
	}

	public static LazyUtils getInstance() {
		if (instance == null) {
			instance = (LazyUtils) Bukkit.getPluginManager().getPlugin("LazyUtils");
		}
		return instance;
	}

	public LazyUtilsConfig getLazyUtilsConfig() {
		return config;
	}
}
