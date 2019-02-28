package com.github.okocraft.lazyutils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.okocraft.lazyutils.command.Commands;

public class LazyUtils extends JavaPlugin implements CommandExecutor {

	private static LazyUtils instance;
	private Commands commands;
	private LazyUtilsConfig config;

	@Override
	public void onEnable() {
		super.onEnable();

		commands = new Commands();
		config = new LazyUtilsConfig();

		getCommand("uuidscoreboard").setExecutor(this);
		getCommand("scoreranking").setExecutor(this);

		this.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return commands.onCommand(sender, command, label, args);
	}

	@Override
	public void onDisable() {
		super.onDisable();
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
