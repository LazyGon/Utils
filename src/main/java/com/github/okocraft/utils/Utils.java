package com.github.okocraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import com.github.okocraft.utils.listener.PlayerPunishmentListener;
import com.github.okocraft.utils.command.Commands;
import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.listener.BlockBreakListener;
import com.github.okocraft.utils.listener.CommandListener;
import com.github.okocraft.utils.listener.PvEReward;
import com.github.okocraft.utils.listener.PvPArea;
import com.github.okocraft.utils.listener.RegionExpander;

public class Utils extends JavaPlugin {

	private static Utils instance;
	private Economy economy;
	private boolean economyEnabled = true;

	@Override
	public void onEnable() {

		Config.reloadAllConfigs();

		if (!setupEconomy()) {
			getLogger().severe("Failed to hook vault.");
			economyEnabled = false;
		}

		Commands.init();

		if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
			RegionExpander.getInstance().startListener();
		}

		try {
			new PvPArea(Config.getDefaultPvPAreaPos1(), Config.getDefaultPvPAreaPos2(), Config.getDefaultPvPAreaRespawn());
		} catch (IllegalArgumentException e) {
			getLogger().warning(e.getMessage());
		}
		new CommandListener(this);
		new PvEReward(this);
		new PlayerPunishmentListener();

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

	public boolean isEconomyEnabled(){
		return economyEnabled;
	}

	public Economy getEconomy(){
		return economy;
	}
	
    /**
     * economyをセットする。
     * 
     * @return 成功したらtrue　失敗したらfalse
     */
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault was not found.");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
	}
}
