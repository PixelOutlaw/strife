package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.events.SpawnerSpawnEvent;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
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
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Spawner extends BukkitRunnable {

  public static int SPAWNER_OFFSET = 0;

  private final Set<LivingEntity> entities = new HashSet<>();
  private final List<Long> respawnTimes = new ArrayList<>();

  private final String id;
  private final String uniqueId;
  private final UniqueEntity uniqueEntity;

  private int amount;
  @Getter @Setter
  private int level = -1;
  private Location location;
  private double leashRange;
  private long respawnSeconds;
  private long chunkKey;
  private int startTime;
  private int endTime;

  private long snoozeTime = System.currentTimeMillis();

  public Spawner(String id, UniqueEntity uniqueEntity, String uniqueId, int amount,
      Location location, int respawnSeconds, double leashRange) {
    this.id = id;
    this.uniqueId = uniqueId;
    this.uniqueEntity = uniqueEntity;
    this.amount = amount;
    this.location = location;
    this.respawnSeconds = respawnSeconds;
    this.leashRange = leashRange;
    chunkKey = location.getChunk().getChunkKey();

    runTaskTimer(StrifePlugin.getInstance(), SPAWNER_OFFSET % 20, 40L);
    SPAWNER_OFFSET++;
    LogUtil.printDebug("Created Spawner with taskId " + getTaskId());
  }

  @Override
  public void run() {
    Iterator<LivingEntity> iterator = entities.iterator();
    while (iterator.hasNext()) {
      LivingEntity le = iterator.next();
      if (le == null || !le.isValid()) {
        iterator.remove();
        continue;
      }
      double xDist = Math.abs(location.getX() - le.getLocation().getX());
      double zDist = Math.abs(location.getZ() - le.getLocation().getZ());
      if (Math.abs(xDist) + Math.abs(zDist) > leashRange) {
        despawnParticles(le);
        Set<Player> players = new HashSet<>(le.getLocation()
            .getNearbyEntitiesByType(Player.class, 15));
        for (Player p : players) {
          StrifePlugin.getInstance().getIndicatorManager().addIndicator(p, le,
              IndicatorStyle.FLOAT_UP_SLOW, 30, "&4&lOUT OF RANGE");
        }
        if (StrifePlugin.getInstance().getStrifeMobManager().isTrackedEntity(le)) {
          StrifePlugin.getInstance().getStrifeMobManager().removeStrifeMob(le);
        }
        le.remove();
        iterator.remove();
        LogUtil.printDebug("Cancelled SpawnerTimer with id " + getTaskId() + " due to leash range");
      }
    }
    spawnSpawner(this);
  }

  public static void spawnSpawner(Spawner spawner) {
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

    int totalMobSlotsInUse = spawner.getRespawnTimes().size() + spawner.getEntities().size();
    int maxMobs = spawner.getAmount();
    if (totalMobSlotsInUse >= maxMobs) {
      return;
    }
    if (!isChuckLoaded(spawner)) {
      return;
    }

    if (spawner.getUniqueEntity().isVagabondAllowed()) {
      if (spawner.getUniqueEntity().getBaseLevel() > StrifePlugin.getInstance()
          .getVagabondManager().getMinimumLevel()) {
        if (StrifePlugin.getInstance().getVagabondManager().getSpawnChance() > Math.random()) {
          int level = spawner.getUniqueEntity().getBaseLevel();
          StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(
              (LivingEntity) StrifePlugin.getInstance().getVagabondManager().spawnVagabond(level,
                  spawner.getLocation()));
          if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
            Bukkit.getLogger().warning("Spawner failed to spawn vagabond! " + spawner.getId());
            return;
          }
          spawner.addEntity(mob.getEntity());
          return;
        }
      }
    }

    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(
        (LivingEntity) StrifePlugin.getInstance().getUniqueEntityManager()
            .spawnUnique(spawner.getUniqueEntity(), spawner.getLocation()));

    SpawnerSpawnEvent spawnerSpawnEvent = new SpawnerSpawnEvent(mob, spawner);
    Bukkit.getPluginManager().callEvent(spawnerSpawnEvent);

    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      Bukkit.getLogger().warning("Spawner failed to spawn unique! " + spawner.getId());
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

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      if (mob.getEntity() == null || !mob.getEntity().isValid()) {
        spawner.setSnoozeTime(System.currentTimeMillis() + 10000);
      } else {
        if (mob.getMods().size() >= 2) {
          announceSpawnToNearbyPlayers(mob, spawner.getLocation());
        }
        // Random displacement to prevent clumping
        if (spawner.getUniqueEntity().getDisplaceMultiplier() != 0) {
          Vector vec = new Vector(-1 + Math.random() * 2, 0.1, -1 + Math.random() * 2).normalize();
          vec.multiply(spawner.getUniqueEntity().getDisplaceMultiplier());
          mob.getEntity().setVelocity(vec);
          mob.getEntity().getLocation().setDirection(mob.getEntity().getVelocity().normalize());
        }
      }
    }, 1L);
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
            "&7&o&lWoah!! &f" + mob.getEntity().getCustomName() + "&f has spawned nearby! "));
        TextComponent waypointButton = new TextComponent("<º>");
        waypointButton.setColor(ChatColor.AQUA);
        waypointButton.setClickEvent(
            new ClickEvent(Action.RUN_COMMAND,
                "/waypointer point " + location.getX() + " " + (location.getY() + 2) + " "
                    + location.getZ() + " " + location.getWorld().getName() + " Mob_Spawn"));

        p.spigot().sendMessage(
            new ComponentBuilder(text)
                .append(waypointButton)
                .create()
        );
        /*
        MessageUtils.sendMessage(p,
            "&7&o&lWoah!! &f" + mob.getEntity().getCustomName() + "&f has spawned nearby! º");
            */
      }
    }
  }

  private static boolean isChuckLoaded(Spawner spawner) {
    String chunkId = spawner.getLocation().getWorld().getName() + spawner.getChunkKey();
    return ChunkUtil.isChuckLoaded(chunkId);
  }

  public void addRespawnTimeIfApplicable(LivingEntity livingEntity) {
    if (entities.contains(livingEntity)) {
      entities.remove(livingEntity);
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

  public String getId() {
    return id;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public UniqueEntity getUniqueEntity() {
    return uniqueEntity;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
    chunkKey = location.getChunk().getChunkKey();
  }

  public double getLeashRange() {
    return leashRange;
  }

  public void setLeashRange(double leashRange) {
    this.leashRange = leashRange;
  }

  public long getRespawnMillis() {
    return respawnSeconds * 1000L;
  }

  public long getRespawnSeconds() {
    return respawnSeconds;
  }

  public void setRespawnSeconds(long respawnSeconds) {
    this.respawnSeconds = respawnSeconds;
  }

  public Set<LivingEntity> getEntities() {
    return entities;
  }

  public void addEntity(LivingEntity trackedEntity) {
    entities.add(trackedEntity);
  }

  public List<Long> getRespawnTimes() {
    return respawnTimes;
  }

  public long getChunkKey() {
    return chunkKey;
  }

  public long getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(long snoozeTime) {
    this.snoozeTime = snoozeTime;
  }

  public int getStartTime() {
    return startTime;
  }

  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }
}
