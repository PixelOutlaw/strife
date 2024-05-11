package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.events.SpawnerSpawnEvent;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.patch.ReturnHomeGoal;
import land.face.strife.util.DateTimeUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.SpecialStatusUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Spawner extends BukkitRunnable {

  public static int SPAWNER_OFFSET = 0;

  @Getter private final Set<Mob> trackedMobs = new HashSet<>();
  @Getter private final List<Long> respawnTimes = new ArrayList<>();

  @Getter private final String id;
  @Getter private final String uniqueId;
  @Getter private final UniqueEntity uniqueEntity;

  @Getter @Setter private int amount;
  @Getter @Setter private int level = -1;
  @Getter private Location location;
  @Getter private double leashRange;
  @Getter @Setter private long respawnSeconds;
  @Getter private String chunkKey;
  @Getter @Setter private int startTime;
  @Getter @Setter private int endTime;

  @Getter @Setter private long snoozeTime = System.currentTimeMillis();

  private transient float squaredLeashRange;

  public Spawner(String id, UniqueEntity uniqueEntity, String uniqueId, int amount,
      Location location, int respawnSeconds, double leashRange) {
    this.id = id;
    this.uniqueId = uniqueId;
    this.uniqueEntity = uniqueEntity;
    this.amount = amount;
    this.location = location;
    this.respawnSeconds = respawnSeconds;
    this.leashRange = leashRange;
    squaredLeashRange = (float) Math.pow(leashRange, 2);
    chunkKey = ChunkUtil.buildChunkKey(location.getChunk());

    runTaskTimer(StrifePlugin.getInstance(), (20L * 10) + (SPAWNER_OFFSET % 100), 100L);
    SPAWNER_OFFSET++;
    LogUtil.printDebug("Created Spawner with taskId " + getTaskId());
  }

  @Override
  public void run() {
    Iterator<Mob> iterator = trackedMobs.iterator();
    while (iterator.hasNext()) {
      Mob mob = iterator.next();
      if (mob == null || !mob.isValid()) {
        iterator.remove();
        continue;
      }
      if (mob.getTarget() != null && location.distanceSquared(mob.getLocation()) > squaredLeashRange) {
        despawnParticles(mob);
        Set<Player> players = new HashSet<>(mob.getLocation().getNearbyEntitiesByType(Player.class, 15));
        for (Player p : players) {
          StrifePlugin.getInstance().getIndicatorManager().addIndicator(p, mob,
              IndicatorStyle.FLOAT_UP_MEDIUM, 30, "<red><bold>OUT OF RANGE", 1.0f, 1.0f, 1.0f);
        }
        if (StrifePlugin.getInstance().getStrifeMobManager().isTrackedEntity(mob)) {
          StrifePlugin.getInstance().getStrifeMobManager().removeStrifeMob(mob);
        }
        mob.remove();
        iterator.remove();
        LogUtil.printDebug("Cancelled SpawnerTimer with id " + getTaskId() + " due to leash range");
      }
    }
    if (!ChunkUtil.isChuckLoaded(chunkKey)) {
      return;
    }
    activateSpawner(this);
  }

  public static void activateSpawner(Spawner spawner) {
    Bukkit.getScheduler().runTaskAsynchronously(StrifePlugin.getInstance(), () -> {
      if (spawner.getUniqueEntity() == null || spawner.getLocation() == null) {
        return;
      }
      if (!DateTimeUtil.isWorldTimeInRange(spawner.getLocation().getWorld(),
          spawner.getStartTime(), spawner.getEndTime())) {
        return;
      }
      if (System.currentTimeMillis() < spawner.getSnoozeTime()) {
        return;
      }
      spawner.getRespawnTimes().removeIf(aLong -> System.currentTimeMillis() > aLong);
      int totalMobSlotsInUse = spawner.getRespawnTimes().size() + spawner.getTrackedMobs().size();
      int maxMobs = spawner.getAmount();
      if (totalMobSlotsInUse >= maxMobs) {
        return;
      }
      if (spawner.getUniqueEntity().isVagabondAllowed()) {
        if (spawner.getUniqueEntity().getBaseLevel() > StrifePlugin.getInstance().getVagabondManager().getMinLevel()) {
          if (StrifePlugin.getInstance().getVagabondManager().getSpawnChance() > StrifePlugin.RNG.nextFloat()) {
            spawnVagabond(spawner);
            return;
          }
        }
      }
      spawnMob(spawner);
    });
  }

  private static void spawnMob(Spawner spawner) {
    Bukkit.getScheduler().runTask(StrifePlugin.getInstance(), () -> {
      if (!ChunkUtil.isChuckLoaded(spawner.getChunkKey())) {
        return;
      }
      StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(
          (LivingEntity) StrifePlugin.getInstance().getUniqueEntityManager()
              .spawnUnique(spawner.getUniqueEntity(), spawner.getLocation()));
      SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(mob, spawner);
      Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);
      if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
        //Bukkit.getLogger().warning("Spawner failed to spawn unique! " + spawner.getId());
        return;
      }
      mob.setSpawner(spawner);
      if (spawner.getLevel() > 0) {
        SpecialStatusUtil.setMobLevel(mob.getEntity(), spawner.getLevel());
      }

      if (StringUtils.isBlank(spawner.getUniqueEntity().getMount())
          && mob.getEntity().getVehicle() != null) {
        mob.getEntity().getVehicle().remove();
      }

      ChunkUtil.setDespawnOnUnload(mob.getEntity());
      spawner.addEntity(mob.getEntity());

      if (mob.getEntity() == null || !mob.getEntity().isValid()) {
        spawner.setSnoozeTime(System.currentTimeMillis() + 10000);
        return;
      }
      if (mob.getMods().size() >= 2) {
        announceSpawnToNearbyPlayers(mob, spawner.getLocation());
      }
      // Random displacement to prevent clumping
      if (spawner.getUniqueEntity().getDisplaceMultiplier() != 0) {
        Vector vec = new Vector(-1 + StrifePlugin.RNG.nextFloat() * 2, 0.1,
            -1 + StrifePlugin.RNG.nextFloat() * 2).normalize();
        vec.multiply(spawner.getUniqueEntity().getDisplaceMultiplier());
        mob.getEntity().setVelocity(vec);
        mob.getEntity().getLocation().setDirection(mob.getEntity().getVelocity().normalize());
      }
      ReturnHomeGoal returnHomeGoal = new ReturnHomeGoal((Mob) mob.getEntity(), spawner.squaredLeashRange * 0.75);
      if (!Bukkit.getMobGoals().hasGoal((Mob) mob.getEntity(), returnHomeGoal.getKey())) {
        Bukkit.getMobGoals().addGoal((Mob) mob.getEntity(), 0, returnHomeGoal);
      }
    });
  }

  private static void spawnVagabond(Spawner spawner) {
    int level = spawner.getUniqueEntity().getBaseLevel();
    Bukkit.getScheduler().runTask(StrifePlugin.getInstance(), () -> {
      StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(
          (LivingEntity) StrifePlugin.getInstance().getVagabondManager().spawnVagabond(level, spawner.getLocation()));
      if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
        //Bukkit.getLogger().warning("Spawner failed to spawn vagabond! " + spawner.getId());
        return;
      }
      spawner.addEntity(mob.getEntity());
    });
  }

  private static void announceSpawnToNearbyPlayers(StrifeMob mob, Location location) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (location.getWorld() != p.getWorld()) {
        continue;
      }
      Vector diff = location.toVector().subtract(p.getLocation().toVector());
      if (diff.lengthSquared() < 6400) {
        p.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 100, 0.8F);
        TextComponent text = new TextComponent(StringExtensionsKt.chatColorize(
            "&7&o&lWoah!! &f" + mob.getEntity().getCustomName() + "&f has spawned nearby!\n "));
        TextComponent waypointButton = new TextComponent("[Click For Waypoint]");
        waypointButton.setColor(ChatColor.AQUA);
        waypointButton.setClickEvent(new ClickEvent(Action.RUN_COMMAND,
            "/waypointer point " + location.getX() + " " + (location.getY() + 2) + " " + location.getZ() + " "
                + location.getWorld().getName() + " Mob_Spawn"));

        p.spigot().sendMessage(new ComponentBuilder(text).append(waypointButton).create());
      }
    }
  }

  public void addRespawnTimeIfApplicable(LivingEntity livingEntity) {
    if (trackedMobs.contains(livingEntity)) {
      trackedMobs.remove(livingEntity);
      respawnTimes.add(System.currentTimeMillis() + getRespawnMillis());
    }
  }

  private static void despawnParticles(LivingEntity livingEntity) {
    double height = livingEntity.getHeight() / 2;
    double width = livingEntity.getWidth() / 2;
    Location loc = livingEntity.getLocation().clone().add(0, height, 0);
    livingEntity.getWorld().spawnParticle(Particle.CLOUD, loc, (int) (20 * (height + width)), width,
        height, width, 0);
  }

  public void setLocation(Location location) {
    this.location = location;
    chunkKey = ChunkUtil.buildChunkKey(location.getChunk());
  }

  public void setLeashRange(double leashRange) {
    this.leashRange = leashRange;
    squaredLeashRange = (float) Math.pow(leashRange, 2);
  }

  public long getRespawnMillis() {
    return respawnSeconds * 1000L;
  }

  public void addEntity(LivingEntity trackedEntity) {
    if (trackedEntity instanceof Mob) {
      trackedMobs.add((Mob) trackedEntity);
    } else {
      Bukkit.getLogger().warning("[Strife] Tried to add " + trackedEntity.getType() + " to spawner, but its not a mob");
    }
  }
}
