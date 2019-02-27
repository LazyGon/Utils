package lazyutils.plugin.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lazyutils.plugin.LazyUtilsConfig;

public class Filer {

	LazyUtilsConfig config = new LazyUtilsConfig();



	private String PermissionDenied;
	private String HomeDirectory;
	private String DownloadMessage;
	private String RemoveMessage;
	private String HelpMessage = config.getFilerHelpMessage();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		PermissionDenied = config.getPermissionDenied();
		HomeDirectory = config.getFilerHomeDirectory();
		DownloadMessage = config.getFilerDownloadMessage();
		RemoveMessage = config.getFilerRemoveMessage();
		HelpMessage = config.getFilerHelpMessage();

		if (args.length == 3)
			if(args[0].equalsIgnoreCase("download"))
				return download(args[1], args[2], sender);

		if (args.length == 2)
			if(args[0].equalsIgnoreCase("remove"))
				return remove(args[1], sender);

		if(args.length == 1)
			if(args[0].equalsIgnoreCase("list"))
				return list(sender);

		return help(sender);
	}





	private boolean download(String filename, String baseURL, CommandSender sender) {

		if (sender instanceof Player && !sender.hasPermission("lazyutil.filer.download")) {
			sender.sendMessage(PermissionDenied);
			return false;
		}

		try {
			URL url = new URL(baseURL);
			URLConnection conn = url.openConnection();
			InputStream input = conn.getInputStream();
			File file = new File(HomeDirectory + filename);
			FileOutputStream output = new FileOutputStream(file, false);
			int b;
			while ((b = input.read()) != -1) {
				output.write(b);
			}
			output.close();
			input.close();
			sender.sendMessage(DownloadMessage);
			return true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean remove(String filename, CommandSender sender) {

		if (sender instanceof Player && !sender.hasPermission("lazyutil.filer.remove")) {
			sender.sendMessage(PermissionDenied);
			return false;
		}

		File file = new File(HomeDirectory + filename);
		file.delete();
		sender.sendMessage(RemoveMessage);
		return true;
	}
	private boolean list(CommandSender sender) {

		if (sender instanceof Player && !sender.hasPermission("lazyutil.filer.list")) {
			sender.sendMessage(PermissionDenied);
			return false;
		}

		String result = "";
		File folder = new File(HomeDirectory);
		String[] list = folder.list();
		for (String file : list) {
			file.replaceFirst(HomeDirectory, "");
			result += file+"\n";
		}
		sender.sendMessage(result);
		return true;
	}

	private boolean help(CommandSender sender) {

		if (sender instanceof Player && !sender.hasPermission("lazyutil.filer.help")) {
			sender.sendMessage(PermissionDenied);
			return false;
		}

		sender.sendMessage(HelpMessage);
		if (sender instanceof Player && sender.hasPermission("lazyutil.filer.download")
				|| !(sender instanceof Player))
			sender.sendMessage("/filer download <filename> <URL>");
		if (sender instanceof Player && sender.hasPermission("lazyutil.filer.remove")
				|| !(sender instanceof Player))
			sender.sendMessage("/filer remove <filename>");
		return true;
	}
}
