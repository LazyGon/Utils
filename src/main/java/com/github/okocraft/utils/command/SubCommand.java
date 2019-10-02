package com.github.okocraft.utils.command;

import java.util.List;
import java.util.Locale;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    protected static Utils plugin = Utils.getInstance();

    SubCommand() {
    }

    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);

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