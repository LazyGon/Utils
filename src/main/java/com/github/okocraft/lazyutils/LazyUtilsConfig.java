package com.github.okocraft.lazyutils;

import org.bukkit.configuration.file.FileConfiguration;

public class LazyUtilsConfig {

	private FileConfiguration config;
	private String PermissionDenied;

	protected LazyUtilsConfig() {
		this.reloadConfig();
	}

	public void reloadConfig() {
		config = LazyUtils.getInstance().getConfig();

		this.PermissionDenied = config.getString("PermissionDenied");
	}

	public String getPermissionDenied() {
		return this.PermissionDenied;
	}
}
