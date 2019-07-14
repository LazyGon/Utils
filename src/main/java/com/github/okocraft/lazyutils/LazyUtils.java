package com.github.okocraft.lazyutils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import com.github.okocraft.lazyutils.command.Commands;
import com.github.okocraft.lazyutils.command.LazyUtilsTabCompleter;
import com.github.okocraft.lazyutils.command.Spawner;
import com.github.okocraft.lazyutils.listener.FarmAutoReplace;
import com.github.okocraft.lazyutils.listener.PlayerDeath;
import com.github.okocraft.lazyutils.listener.PreventCreatingHall;
import com.github.okocraft.lazyutils.listener.PreventMultiCenterProtection;
import com.github.okocraft.lazyutils.listener.SaplingAutoReplace;
import com.github.okocraft.lazyutils.listener.TestListener;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class LazyUtils extends JavaPlugin {

	private static LazyUtils instance;
	private LazyUtilsConfig config;
	private CustomConfig repairCostConfig;
	private CustomConfig prefixData;
	private CustomConfig publicFarmConfig;
	private Economy economy;
	private boolean isEconomyEnabled = true;
	public static StateFlag inactiveAutoPurgeFlag = new StateFlag("inactive-auto-purge", true);

	@Override
	public void onLoad() {
    	FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
    	try {
    	    // register our flag with the registry
    	    registry.register(inactiveAutoPurgeFlag);
    	} catch (FlagConflictException e) {
			getLogger().warning("The worldguard flag named inactive-auto-purge is already registered.");
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {

		if (!setupEconomy()) {
			getLogger().severe("Failed to hook vault.");
			isEconomyEnabled = false;
		}

		repairCostConfig = new CustomConfig(this, "RepairCost.yml");
		prefixData = new CustomConfig(this, "PrefixData.yml");
		publicFarmConfig = new CustomConfig(this, "PublicFarm.yml");
		config = new LazyUtilsConfig();
		new Spawner(this);

		this.saveDefaultConfig();
		repairCostConfig.saveDefaultConfig();
		prefixData.saveDefaultConfig();
		publicFarmConfig.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
		new PlayerDeath(this);
		new SaplingAutoReplace(this);
		new FarmAutoReplace(this);
		new PreventCreatingHall();
		new PreventMultiCenterProtection(this);
		new TestListener(this);
		new Commands();
		new LazyUtilsTabCompleter();
	}

	@Override
	public void onDisable() {
		prefixData.saveConfig();
		HandlerList.unregisterAll(this);
	};

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

	public CustomConfig getPublicFarmConfig(){
		return publicFarmConfig;
	}

	public boolean isEconomyEnabled(){
		return isEconomyEnabled;
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
        return economy != null;
	}
}
