package com.github.okocraft.lazyutils;

import org.bukkit.configuration.file.FileConfiguration;

public class LazyUtilsConfig {

	private FileConfiguration config;

	private String PermissionDenied;
	private String FilerHomeDirectory;
	private String FilerDownloadMessage;
	private String FilerRemoveMessage;
	private String FilerHelpMessage;

	void loadConfig(){

		config = LazyUtils.getInstance().getConfig();

		this.PermissionDenied = config.getString("PermissionDenied");
		this.FilerHomeDirectory = config.getString("HomeDirectory");
		this.FilerDownloadMessage = config.getString("DownloadMessage");
		this.FilerRemoveMessage = config.getString("RemoveMessage");
		this.FilerHelpMessage = config.getString("HelpMessage");

	}

	public String getPermissionDenied() {
		return this.PermissionDenied;
	}

	public String getFilerHomeDirectory() {
		return this.FilerHomeDirectory;
	}

	public String getFilerDownloadMessage() {
		return this.FilerDownloadMessage;
	}

	public String getFilerRemoveMessage() {
		return this.FilerRemoveMessage;
	}

	public String getFilerHelpMessage() {
		return this.FilerHelpMessage;
	}

}
