package com.github.okocraft.lazyutils.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class Spawner implements Listener {

    public Spawner(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static boolean giveSpawner(CommandSender sender, String mobName, int amount) {
        if (!(sender instanceof Player))
            return false;

        PlayerInventory inv = ((Player) sender).getInventory();

        if (inv.firstEmpty() == -1)
            return Commands.errorOccurred(sender, "§cインベントリに空きがありません。");
        if (amount > 64 || amount < 1)
            return Commands.errorOccurred(sender, "§c数は1 ~ 64の間にしてください。");

        try {
            if (!EntityType.valueOf(mobName.toUpperCase()).isSpawnable())
                return Commands.errorOccurred(sender, "そのエンティティはスポーン不可能です。");
        } catch (IllegalArgumentException e) {
            return Commands.errorOccurred(sender, "§cそのようなエンティティは存在しません。");
        }

        ItemStack spawner = new ItemStack(Material.SPAWNER);
        ItemMeta spawnerMeta = spawner.getItemMeta();
        spawnerMeta.setLore(
                Arrays.asList("§e" + mobName.substring(0, 1).toUpperCase() + mobName.substring(1).toLowerCase()));
        spawner.setItemMeta(spawnerMeta);
        spawner.setAmount(amount);
        inv.addItem(spawner);

        return true;
    }

    @EventHandler
    private void onSpawnerPlace(BlockPlaceEvent event) {

        ItemStack itemInHand = event.getItemInHand();

        if (!itemInHand.getType().equals(Material.SPAWNER))
            return;

        Block placedBlock = event.getBlock();
        if (!placedBlock.getType().equals(Material.SPAWNER))
            return;

        ItemMeta spawnerMeta = itemInHand.getItemMeta();
        if (!spawnerMeta.hasLore())
            return;

        List<String> spawnerLore = spawnerMeta.getLore();
        if (spawnerLore.size() == 0)
            return;

        String mobType = spawnerLore.get(0).toUpperCase();
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(mobType.replaceAll("§.", ""));
        } catch (IllegalArgumentException e) {
            return;
        }

        CreatureSpawner creatureSpawner = (CreatureSpawner) placedBlock.getState();
        creatureSpawner.setSpawnedType(entityType);
        creatureSpawner.update();
    }

    @EventHandler
    private void spawnerSpawn(SpawnerSpawnEvent e) {
        if (!e.getSpawner().getBlock().getWorld().getName().equalsIgnoreCase("TT"))
            e.setCancelled(true);
    }

    @EventHandler
    private void spawnerChangeDenied(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null)
            return;
        if (!e.getClickedBlock().getType().equals(Material.SPAWNER))
            return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
            return;
        Stream.of(Material.values()).filter(material -> material.getKey().getKey().contains("spawn_egg"))
                .forEach(eggs -> {
                    EntityEquipment equipment = e.getPlayer().getEquipment();
                    if (equipment.getItemInMainHand().getType().equals(eggs)
                            || equipment.getItemInOffHand().getType().equals(eggs)) {
                        e.setCancelled(true);
                        return;
                    }
                });

    }
}