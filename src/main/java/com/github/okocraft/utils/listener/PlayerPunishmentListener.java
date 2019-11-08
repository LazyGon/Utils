package com.github.okocraft.utils.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.alienationgaming.jailworker.config.JailConfig;
import fr.alienationgaming.jailworker.config.Prisoners;
import fr.alienationgaming.jailworker.config.WantedPlayers;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Config;
import com.github.siroshun09.punishmentlistener.event.PunishmentEvent;

public class PlayerPunishmentListener implements Listener {

    private static final Utils plugin = Utils.getInstance();

    public PlayerPunishmentListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPunishment(PunishmentEvent event) {
        if (!event.getType().equals("warn")) {
            return;
        }

        int warns = event.getCurrentWarns();
        List<Integer> warnPointList = Config.getPunishmentPointPerWarns();
        if (warnPointList.isEmpty()) {
            return;
        }
        if (warns > warnPointList.size()) {
            warns = warnPointList.size();
        } else if (warns < 1) {
            warns = 1;
        }
        int punishmentPoint = warnPointList.get(warns - 1);

        List<String> jails = JailConfig.getJails();
        jails.removeIf(jail -> !JailConfig.exist(jail));
        String jailName;
        if (jails.isEmpty()) {
            return;
        } else if (jails.contains(Config.getJailName())) {
            jailName = Config.getJailName();
        } else {
            jailName = jails.get(new Random().nextInt(jails.size()));
        }

        String uuidString = convertUUIDString(event.getUuid());
        OfflinePlayer punishedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));

        if (punishedPlayer.isOnline()) {
            Prisoners.punishPlayer(punishedPlayer.getPlayer(), jailName, null, punishmentPoint, event.getReason());
            return;
        } else {
            WantedPlayers.addWantedPlayer(punishedPlayer, jailName, punishmentPoint, event.getReason());
            return;
        }
    }

    private static String convertUUIDString(String uuidString) {
        return uuidString.replaceAll("^(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})$", "$1-$2-$3-$4-$5");
    }
}