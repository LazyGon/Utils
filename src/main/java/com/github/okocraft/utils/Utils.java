package com.github.okocraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.okocraft.utils.command.Commands;
import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.listener.BlockBreakListener;
import com.github.okocraft.utils.listener.PvPArea;

public class Utils extends JavaPlugin {

	private static Utils instance;

	@Override
	public void onEnable() {

		Config.reloadAllConfigs();
		Commands.init();

		try {
			new PvPArea(Config.getDefaultPvPAreaPos1(), Config.getDefaultPvPAreaPos2(), Config.getDefaultPvPAreaRespawn());
		} catch (IllegalArgumentException e) {
			getLogger().warning(e.getMessage());
		}

		BlockBreakListener.start();
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	};

	public static Utils getInstance() {
		if (instance == null) {
			instance = (Utils) Bukkit.getPluginManager().getPlugin("Utils");
		}
		return instance;
	}
}
