package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.okocraft.utils.config.Messages;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

public class HighJump extends UtilsCommand {

    HighJump(){
    }

    private static final Map<Player, Long> cooldowns = new HashMap<>();

    private class FallDamageDisabler implements Listener {

        private final Player player;
        private boolean registered;
    
        FallDamageDisabler(Player player) {
            this.player = player;
            register();
        }
    
        @EventHandler(priority = EventPriority.LOW)
        public void onFall(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player) || event.getEntity() != player) {
                return;
            }
            
            event.setCancelled(true);
            unregister();
        }
    
        public boolean isRegistered() {
            return registered;
        }
    
        public void register() {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            registered = true;
        }
    
        public void unregister() {
            HandlerList.unregisterAll(this);
            registered = false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!super.onCommand(sender, command, label, args)) {
            return false;
        }

        Player player;
        if (sender instanceof Player && args.length == 0) {
            player = (Player) sender;
        } else if (args.length == 1) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Messages.sendMessage(sender, "command.general.error.player-is-not-online");
                return false;
            }
        } else {
            Messages.sendMessage(sender, "command.general.error.specify-player");
            return false;
        }

        if (!sender.hasPermission("utils.highjump.other")) {
            Messages.sendMessage(sender, "command.general.error.no-permission");
            return false;
        }

        long cooldown = cooldowns.getOrDefault(player, 0L);
        long current = System.currentTimeMillis();

        if (cooldown > current) {
            Messages.sendMessage(sender, "command.high-jump.error.in-cooldown");
            return false;
        }

        Messages.sendMessage(sender, "command.high-jump.info.high-jump");
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 76), true);

        FallDamageDisabler fallDamageDisabler = new FallDamageDisabler(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (fallDamageDisabler.isRegistered()) {
                    fallDamageDisabler.unregister();
                }
            }
        }.runTaskLater(plugin, 145L);

        cooldown = current + 1000 * 7;
        cooldowns.put(player, cooldown);
        for (Player key : new HashSet<Player>(cooldowns.keySet())) {
            if (cooldowns.get(key) < current) {
                cooldowns.remove(key);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("utils.high-jump.other")) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[0], players, new ArrayList<>());
        }

        return List.of();
    }

    @Override
    int getLeastArgsLength() {
        return 0;
    }

    @Override
    String getUsage() {
        return "/highjump [player]";
    }
}