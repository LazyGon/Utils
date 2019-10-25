package com.github.okocraft.utils.command;

import java.util.Locale;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public abstract class UtilsCommand implements CommandExecutor, TabCompleter {

    protected static Utils plugin = Utils.getInstance();

    UtilsCommand() {
        PluginCommand command = plugin.getCommand(getName());
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            plugin.getLogger().warning("Command " + getName() + " is not registered in plugin.yml!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < getLeastArgsLength()) {
            Messages.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        if (!hasPermission(sender)) {
            Messages.sendMessage(sender, "command.general.no-permission");
            return false;
        }
        
        return true;
    }
    
    abstract int getLeastArgsLength();
    abstract String getUsage();

    String getPermissionNode() {
        return "utils." + getName();
    }

    String getDescription() {
        return Messages.getMessage("command.utils." + getName() + ".description");
    }

    String getName() {
        return getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }

    boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode()) || sender.hasPermission("utils.*");
    }
} 