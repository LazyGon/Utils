package com.github.okocraft.utils.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.okocraft.utils.Utils;
import com.github.okocraft.utils.config.Config;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.TranslatableComponent;

public class PvPArea implements Listener {

    private static final Utils plugin = Utils.getInstance();
    private static final Map<String, String> deathMessageMap = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("death.fell.accident.ladder", "(.*) fell off a ladder");
            put("death.fell.accident.vines", "(.*) fell off some vines");
            put("death.fell.accident.water", "(.*) fell out of the water");
            put("death.fell.accident.generic", "(.*) fell from a high place");
            put("death.fell.killer", "(.*) was doomed to fall");
            put("death.fell.assist", "(.*) was doomed to fall by (.*)");
            put("death.fell.assist.item", "(.*) was doomed to fall by (.*) using (.*)");
            put("death.fell.finish", "(.*) fell too far and was finished by (.*)");
            put("death.fell.finish.item", "(.*) fell too far and was finished by (.*) using (.*)");
            put("death.attack.lightningBolt", "(.*) was struck by lightning");
            put("death.attack.lightningBolt.player", "(.*) was struck by lightning whilst fighting (.*)");
            put("death.attack.inFire", "(.*) went up in flames");
            put("death.attack.inFire.player", "(.*) walked into fire whilst fighting (.*)");
            put("death.attack.onFire", "(.*) burned to death");
            put("death.attack.onFire.player", "(.*) was burnt to a crisp whilst fighting (.*)");
            put("death.attack.lava", "(.*) tried to swim in lava");
            put("death.attack.lava.player", "(.*) tried to swim in lava to escape (.*)");
            put("death.attack.hotFloor", "(.*) discovered the floor was lava");
            put("death.attack.hotFloor.player", "(.*) walked into danger zone due to (.*)");
            put("death.attack.inWall", "(.*) suffocated in a wall");
            put("death.attack.inWall.player", "(.*) suffocated in a wall whilst fighting (.*)");
            put("death.attack.cramming", "(.*) was squished too much");
            put("death.attack.cramming.player", "(.*) was squashed by (.*)");
            put("death.attack.drown", "(.*) drowned");
            put("death.attack.drown.player", "(.*) drowned whilst trying to escape (.*)");
            put("death.attack.starve", "(.*) starved to death");
            put("death.attack.starve.player", "(.*) starved to death whilst fighting (.*)");
            put("death.attack.cactus", "(.*) was pricked to death");
            put("death.attack.cactus.player", "(.*) walked into a cactus whilst trying to escape (.*)");
            put("death.attack.generic", "(.*) died");
            put("death.attack.generic.player", "(.*) died because of (.*)");
            put("death.attack.explosion", "(.*) blew up");
            put("death.attack.explosion.player", "(.*) was blown up by (.*)");
            put("death.attack.explosion.player.item", "(.*) was blown up by (.*) using (.*)");
            put("death.attack.magic", "(.*) was killed by magic");
            put("death.attack.even_more_magic", "(.*) was killed by even more magic");
            put("death.attack.message_too_long",
                    "Actually, message was too long to deliver fully. Sorry! Here's stripped version: (.*)");
            put("death.attack.wither", "(.*) withered away");
            put("death.attack.wither.player", "(.*) withered away whilst fighting (.*)");
            put("death.attack.anvil", "(.*) was squashed by a falling anvil");
            put("death.attack.anvil.player", "(.*) was squashed by a falling anvil whilst fighting (.*)");
            put("death.attack.fallingBlock", "(.*) was squashed by a falling block");
            put("death.attack.fallingBlock.player", "(.*) was squashed by a falling block whilst fighting (.*)");
            put("death.attack.mob", "(.*) was slain by (.*)");
            put("death.attack.mob.item", "(.*) was slain by (.*) using (.*)");
            put("death.attack.player", "(.*) was slain by (.*)");
            put("death.attack.player.item", "(.*) was slain by (.*) using (.*)");
            put("death.attack.arrow", "(.*) was shot by (.*)");
            put("death.attack.arrow.item", "(.*) was shot by (.*) using (.*)");
            put("death.attack.fireball", "(.*) was fireballed by (.*)");
            put("death.attack.fireball.item", "(.*) was fireballed by (.*) using (.*)");
            put("death.attack.thrown", "(.*) was pummeled by (.*)");
            put("death.attack.thrown.item", "(.*) was pummeled by (.*) using (.*)");
            put("death.attack.indirectMagic", "(.*) was killed by (.*) using magic");
            put("death.attack.indirectMagic.item", "(.*) was killed by (.*) using (.*)");
            put("death.attack.thorns", "(.*) was killed trying to hurt (.*)");
            put("death.attack.thorns.item", "(.*) was killed by (.*) trying to hurt (.*)");
            put("death.attack.trident", "(.*) was impaled by (.*)");
            put("death.attack.trident.item", "(.*) was impaled by (.*) with (.*)");
            put("death.attack.fall", "(.*) hit the ground too hard");
            put("death.attack.fall.player", "(.*) hit the ground too hard whilst trying to escape (.*)");
            put("death.attack.outOfWorld", "(.*) fell out of the world");
            put("death.attack.outOfWorld.player", "(.*) didn't want to live in the same world as (.*)");
            put("death.attack.dragonBreath", "(.*) was roasted in dragon breath");
            put("death.attack.dragonBreath.player", "(.*) was roasted in dragon breath by (.*)");
            put("death.attack.flyIntoWall", "(.*) experienced kinetic energy");
            put("death.attack.flyIntoWall.player", "(.*) experienced kinetic energy whilst trying to escape (.*)");
            put("death.attack.fireworks", "(.*) went off with a bang");
            put("death.attack.fireworks.player", "(.*) went off with a bang whilst fighting (.*)");
            put("death.attack.netherBed.message", "(.*) was killed by (.*)");
            put("death.attack.netherBed.link", "Intentional Game Design");
        }
    };

    private static final Set<Material> wepons = new HashSet<>() {
        private static final long serialVersionUID = 1L;

        {
            add(Material.TURTLE_HELMET);
            add(Material.BOW);
            add(Material.IRON_SWORD);
            add(Material.WOODEN_SWORD);
            add(Material.STONE_SWORD);
            add(Material.DIAMOND_SWORD);
            add(Material.GOLDEN_SWORD);
            add(Material.IRON_AXE);
            add(Material.WOODEN_AXE);
            add(Material.STONE_AXE);
            add(Material.DIAMOND_AXE);
            add(Material.GOLDEN_AXE);
            add(Material.LEATHER_HELMET);
            add(Material.CHAINMAIL_HELMET);
            add(Material.IRON_HELMET);
            add(Material.DIAMOND_HELMET);
            add(Material.GOLDEN_HELMET);
            add(Material.LEATHER_CHESTPLATE);
            add(Material.CHAINMAIL_CHESTPLATE);
            add(Material.IRON_CHESTPLATE);
            add(Material.DIAMOND_CHESTPLATE);
            add(Material.GOLDEN_CHESTPLATE);
            add(Material.LEATHER_LEGGINGS);
            add(Material.CHAINMAIL_LEGGINGS);
            add(Material.IRON_LEGGINGS);
            add(Material.DIAMOND_LEGGINGS);
            add(Material.GOLDEN_LEGGINGS);
            add(Material.LEATHER_BOOTS);
            add(Material.CHAINMAIL_BOOTS);
            add(Material.IRON_BOOTS);
            add(Material.DIAMOND_BOOTS);
            add(Material.GOLDEN_BOOTS);
            add(Material.SHIELD);
            add(Material.TRIDENT);
            add(Material.CROSSBOW);
        }
    };

    private final class WESelectionGetter {

        private final Player player;
        private Location pos1;
        private Location pos2;

        private WESelectionGetter(Player player) throws IllegalArgumentException {
            this.player = player;
            Region region = getWorldEditSelection();
            if (region == null) {
                throw new IllegalArgumentException("Select region");
            }

            World world = Bukkit.getWorld(region.getWorld().getName());
            BlockVector3 max = region.getMaximumPoint();
            BlockVector3 min = region.getMinimumPoint();
            pos1 = new Location(world, max.getX(), max.getY(), max.getZ());
            pos2 = new Location(world, min.getX(), min.getY(), min.getZ());

            RegionManager rc = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(pos1.getWorld()));
            BlockVector3 pos1Vector = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
            BlockVector3 pos2Vector = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());
            ProtectedRegion checkedRegion = new ProtectedCuboidRegion("test", pos1Vector, pos2Vector);
            List<ProtectedRegion> intersectingRegions = checkedRegion.getIntersectingRegions(rc.getRegions().values());
            if (intersectingRegions.isEmpty()) {
                throw new IllegalArgumentException("Region must be in your own worldguard protection");
            }
            for (ProtectedRegion intersecting : intersectingRegions) {
                if (!intersecting.getOwners().contains(player.getUniqueId())) {
                    throw new IllegalArgumentException("Region must be in your own worldguard protection");
                }
            }
        }

        private Location getPos1() {
            return pos1;
        }

        private Location getPos2() {
            return pos2;
        }

        private Region getWorldEditSelection() {
            if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                return null;
            }
            com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
            LocalSession playerSession = WorldEdit.getInstance().getSessionManager().get(wePlayer);
            if (playerSession.getSelectionWorld() == null) {
                return null;
            }

            try {
                return playerSession.getSelection(playerSession.getSelectionWorld());
            } catch (IncompleteRegionException e) {
                return null;
            }
        }
    }

    private static final Map<CommandSender, PvPArea> maps = new HashMap<>();

    private boolean smashMode = Config.isDefaultSmashModeEnabled();
    private boolean itemUnlimited = Config.isDefaultItemUnlimitedEnabled();
    private final Player owner;
    private final Location pos1;
    private final Location pos2;
    private Location respawnPoint;

    public PvPArea(Location pos1, Location pos2, Location respawnPoint) throws IllegalArgumentException {
        this.pos1 = pos1;
        this.pos2 = pos2;
        
        if (pos1 == null || pos2 == null) {
            throw new IllegalArgumentException("pos1 or pos2 is null");
        }
        if (pos1.getWorld() == null || pos2.getWorld() == null) {
            throw new IllegalArgumentException("The worlds of pos1 or pos2 is null");
        }
        if (pos1.getWorld() != pos2.getWorld()) {
            throw new IllegalArgumentException("The worlds is different between pos1 and pos2");
        }
        
        this.owner = null;
        this.respawnPoint = respawnPoint;

        maps.put(owner, this);
        startListener();
    }

    public PvPArea(Player creater) throws IllegalArgumentException {
        if (maps.containsKey(creater)) {
            throw new IllegalArgumentException("The player has already created PvP area");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            throw new NullPointerException("WorldEdit is not installed");
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            throw new NullPointerException("WorldGuard is not installed");
        }

        WESelectionGetter getter = new WESelectionGetter(creater);
        this.pos1 = getter.getPos1();
        this.pos2 = getter.getPos2();
        if (Config.getPvPAreaDisabledWorlds().contains(pos1.getWorld().getName())) {
            throw new IllegalArgumentException("PvP area is disabled in this world");
        }
        this.owner = creater;
        this.respawnPoint = creater.getLocation();

        maps.put(owner, this);
        startListener();
    }

    public void startListener() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void stopListener() {
        if (maps.containsKey(owner)) {
            HandlerList.unregisterAll(maps.get(owner));
            maps.remove(owner);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == owner) {
            stopListener();
        }
    }

    /**
     * @return the itemUnlimited
     */
    public boolean isItemUnlimited() {
        return itemUnlimited;
    }

    /**
     * @param itemUnlimited the itemUnlimited to set
     */
    public void setItemUnlimited(boolean itemUnlimited) {
        this.itemUnlimited = itemUnlimited;
    }

    /**
     * @return the smashMode
     */
    public boolean isSmashMode() {
        return smashMode;
    }

    /**
     * @param smashMode the smashMode to set
     */
    public void setSmashMode(boolean smashMode) {
        this.smashMode = smashMode;
    }

    /**
     * @return the respawnPoint
     */
    public Location getRespawnPoint() {
        return respawnPoint;
    }

    /**
     * @param respawnPoint the respawnPoint to set
     */
    public void setRespawnPoint(Location respawnPoint) {
        this.respawnPoint = respawnPoint;
    }

    /**
     * @return the owner
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * @return the pos1
     */
    public Location getPos1() {
        return pos1;
    }

    /**
     * @return the pos2
     */
    public Location getPos2() {
        return pos2;
    }

    /**
     * @param player key
     * @return get instance of player's pvp area.
     */
    public static PvPArea getInstance(Player player) {
        return maps.get(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathLowest(PlayerDeathEvent event) {
        if (!isInPvPArea(event.getEntity()) || event.getDeathMessage() == null) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        boolean hasEnchantment = false;
        if (killer != null) {
            hasEnchantment = !killer.getEquipment().getItemInMainHand().getEnchantments().isEmpty();
        }
        TranslatableComponent deathMessage = translateDeathMessage(event.getDeathMessage(), hasEnchantment);

        Bukkit.getOnlinePlayers().stream().filter(player -> isInPvPArea(player))
                .forEach(player -> player.spigot().sendMessage(deathMessage));

        new BukkitRunnable() {

            @Override
            public void run() {
                event.getEntity().spigot().respawn();
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (isInPvPArea(event.getPlayer())) {
            event.setRespawnLocation(respawnPoint);
        }
    }

    @EventHandler
    public void onPlayerItemDamaged(PlayerItemDamageEvent event) {
        if (!itemUnlimited || !isInPvPArea(event.getPlayer())) {
            return;
        }

        if (wepons.contains(event.getItem().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player && event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (!isInPvPArea(player) || !isInPvPArea((Player) event.getDamager())) {
            return;
        }

        if (!smashMode) {
            return;
        }

        event.setCancelled(true);
        player.damage(event.getDamage());

        Vector knockback = player.getLocation().toVector().subtract(event.getDamager().getLocation().toVector());
        double power = Math.sqrt(event.getDamage() * 0.75);

        knockback = knockback.normalize().multiply(power).add(new Vector(0, power / 3, 0));
        player.setVelocity(knockback);

        new BukkitRunnable() {
            long startTime = System.currentTimeMillis();

            @Override
            public void run() {
                player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);

                if (player.getVelocity().getY() < 0 || player.isDead()
                        || startTime + 1000 < System.currentTimeMillis()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private boolean isInPvPArea(Player player) {
        return isInside(pos1, pos2, player.getLocation());
    }

    /**
     * {@code target}がpos1、pos2の範囲内にあるか調べる。
     * 
     * @param pos1
     * @param pos2
     * @param target
     * @return targetがpos1, pos2の中にあればtrue さもなくばfalse
     */
    public static boolean isInside(Location pos1, Location pos2, Location target) {
        if (!isInsideIgnoreY(pos1, pos2, target)) {
            return false;
        }

        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minY = Math.min(pos1.getY(), pos2.getY());

        return target.getY() <= maxY && target.getY() >= minY;
    }

    /**
     * {@code target}がpos1、pos2の範囲内にあるか調べる。ただし、Y座標は無視する。
     * 
     * @param pos1
     * @param pos2
     * @param target
     * @return targetがpos1, pos2の中にあればtrue さもなくばfalse
     */
    public static boolean isInsideIgnoreY(Location pos1, Location pos2, Location target) {
        if (pos1.getWorld() == null || pos2.getWorld() == null || target.getWorld() == null) {
            return false;
        }
        if (pos1.getWorld() != pos2.getWorld()) {
            return false;
        }
        if (pos1.getWorld() != target.getWorld()) {
            return false;
        }

        final double x1 = Math.min(pos1.getX(), pos2.getX());
        final double z1 = Math.min(pos1.getZ(), pos2.getZ());
        final double x2 = Math.max(pos1.getX(), pos2.getX());
        final double z2 = Math.max(pos1.getZ(), pos2.getZ());

        final double x = target.getX();
        final double z = target.getZ();
        return (x >= x1) && (x <= x2) && (z >= z1) && (z <= z2);
    }

    /**
     * deathMessageに対応するdeathMessageKeyを取得する。全く同じ値をもつdeathMessageKeyがある場合はどのkeyを取得するかは明確に定義されない。
     * 
     * @param deathMessage
     * @return deathMessageKey
     */
    private static String getDeathMessageKey(String deathMessage) {
        Optional<Map.Entry<String, String>> optionalDeathMessageIndex = deathMessageMap.entrySet().parallelStream()
                .filter(entry -> deathMessage.matches(entry.getValue())).sequential()
                .max((e1, e2) -> e1.getValue().length() - e2.getValue().length());
        if (!optionalDeathMessageIndex.isPresent()) {
            return "";
        } else {
            return optionalDeathMessageIndex.get().getKey();
        }
    }

    /**
     * en_USで記述されたdeathMessageから、対応するdeathMessageKeyに当てはまる引数を取り出す。
     * 
     * @param deathMessageKey
     * @param deathMessage
     * @param hasEnchantedItem
     * @return 引数の順番と要素番号が対応した文字列のリスト
     */
    private static List<String> getDeathMessageArgs(String deathMessageKey, String deathMessage,
            boolean hasEnchantedItem) {
        String deathMessageRegex = deathMessageMap.getOrDefault(deathMessageKey, "");
        if (deathMessageRegex.equals("")) {
            return new ArrayList<>();
        }

        Pattern pattern = Pattern.compile(deathMessageRegex);
        Matcher matcher = pattern.matcher(deathMessage);
        if (matcher.find()) {
            List<String> result = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                result.add(
                        matcher.group(i).replaceAll("\\[(.*)\\]", hasEnchantedItem ? "§b[§o$1§b]§r" : "§f[§r$1§f]§r"));
            }
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * deathMessageをTranslatableComponentに変換して返す。エンチャントアイテムを持っているかによって殺害時のアイテムの枠の色が変わる。
     * 
     * @param deathMessage
     * @param hasEnchantedItem
     * @return 対応するキルログのTranslatableComponent
     */
    public static TranslatableComponent translateDeathMessage(String deathMessage, boolean hasEnchantedItem) {
        String deathMessageKey = getDeathMessageKey(deathMessage);
        List<String> deathMessageArgs = getDeathMessageArgs(deathMessageKey, deathMessage, hasEnchantedItem);
        return new TranslatableComponent(deathMessageKey, deathMessageArgs.toArray());
        /**
         * title: playername second: Type: minecraft:player third: uuid
         */
    }
}
