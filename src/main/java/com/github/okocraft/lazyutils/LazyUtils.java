package com.github.okocraft.lazyutils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import com.github.okocraft.lazyutils.command.Commands;
import com.github.okocraft.lazyutils.command.LazyUtilsTabCompleter;
import com.github.okocraft.lazyutils.command.Spawner;

public class LazyUtils extends JavaPlugin {

	private static LazyUtils instance;
	private LazyUtilsConfig config;
	private CustomConfig repairCostConfig;
	private CustomConfig prefixData;
	private Economy economy;
	private boolean isEconomyEnabled = true;

	@Override
	public void onEnable() {

		if (!setupEconomy()){
			getLogger().severe("Failed to hook vault.");
			isEconomyEnabled = false;
		}

		repairCostConfig = new CustomConfig(this, "RepairCost.yml");
		prefixData = new CustomConfig(this, "PrefixData.yml");
		new Spawner(this);
		new Commands();
		new LazyUtilsTabCompleter();
		config = new LazyUtilsConfig();

		this.saveDefaultConfig();
		repairCostConfig.saveDefaultConfig();
		prefixData.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
	}

	@Override
	public void onDisable() {
		prefixData.saveConfig();
		HandlerList.unregisterAll(this);
	}

	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
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

	public CustomConfig getRepairCostConfig(){
		return repairCostConfig;
	}

	public CustomConfig getPrefixData(){
		return prefixData;
	}

	public boolean isEconomyEnabled(){
		return isEconomyEnabled;
	}

	public Economy getEconomy(){
		return economy;
	}
}
