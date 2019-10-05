package com.github.okocraft.utils.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.github.okocraft.utils.config.Config;
import com.github.okocraft.utils.config.Messages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

public class Spawner extends SubCommand implements Listener {

    private static final NamespacedKey key = new NamespacedKey(plugin, "spawners");

    Spawner() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        String mobType = spawnerMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (mobType == null) {

            if (!spawnerMeta.hasLore()) {
                return;
            }

            List<String> spawnerLore = spawnerMeta.getLore();
            if (spawnerLore.size() == 0) {
                return;
            }

            mobType = spawnerLore.get(0).toUpperCase(Locale.ROOT).replaceAll("§.", "");
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(mobType);
        } catch (IllegalArgumentException e) {
            return;
        }

        CreatureSpawner creatureSpawner = (CreatureSpawner) placedBlock.getState();
        creatureSpawner.setSpawnedType(entityType);
        creatureSpawner.update();

        Messages.sendMessage(event.getPlayer(), "listener.spawner.tips");
    }

    @EventHandler
    private void spawnerSpawn(SpawnerSpawnEvent event) {
        Block spawner = event.getSpawner().getBlock();
        if (!Config.getSpawnerAllowedWorlds().contains(spawner.getWorld())) {
            event.setCancelled(true);
            return;
        }

        if (!spawner.isBlockPowered() || spawner.isBlockIndirectlyPowered()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    private void isSpawnerPowered(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getType() != Material.SPAWNER) {
            return;
        }

        if (block.isBlockPowered() || block.isBlockIndirectlyPowered()) {
            Messages.sendMessage(event.getPlayer(), "listener.spawner.spawner-is-stopped");
        } else {
            Messages.sendMessage(event.getPlayer(), "listener.spawner.spawner-is-not-stopped");
        }
    }

    @EventHandler
    private void spawnerChangeDenied(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        if (!event.getClickedBlock().getType().equals(Material.SPAWNER)) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        EntityEquipment equipment = event.getPlayer().getEquipment();
        if (equipment.getItemInMainHand().getType().name().contains("spawn_egg")
                || equipment.getItemInOffHand().getType().name().contains("spawn_egg")) {
            event.setCancelled(true);
            return;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, "command.general.error.player-only");
            return false;
        }

        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
            if (!type.isSpawnable()) {
                throw new IllegalArgumentException("The entity is not spawnable");
            }
        } catch (IllegalArgumentException e) {
            Messages.sendMessage(sender, "command.utils.spawner.error.invalid-mob-type");
            return false;
        }

        PlayerInventory inv = ((Player) sender).getInventory();

        int amount = 1;
        if (args.length > 2) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        if (amount < 1) {
            amount = 1;
        }

        ItemStack spawner = new ItemStack(Material.SPAWNER);
        ItemMeta spawnerMeta = spawner.getItemMeta();
        spawnerMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.name());
        spawnerMeta.setDisplayName("§e" + type.getKey().getKey() + " のスポナー");
        spawner.setItemMeta(spawnerMeta);
        spawner.setAmount(amount);
        if (!inv.addItem(spawner).isEmpty()) {
            Messages.sendMessage(sender, "command.utils.spawner.inventory-space-is-not-enough");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> mobTypes = Arrays.stream(EntityType.values()).filter(EntityType::isSpawnable).map(EntityType::name).collect(Collectors.toList());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], mobTypes, result);
        }

        if (!mobTypes.contains(args[1])) {
            return result;
        }

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], List.of("1", "2", "4", "8", "16", "32", "64"), result);
        }

        return result;
    }

    @Override
    int getLeastArgsLength() {
        return 2;
    }

    @Override
    String getUsage() {
        return "/utils spawner <mob-type> [amount]";
    }
}