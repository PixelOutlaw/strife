package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.util.ChunkUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.SpecialStatusUtil;
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

  private final List<LivingEntity> entities = new CopyOnWriteArrayList<>();
  private final List<Long> respawnTimes = new CopyOnWriteArrayList<>();

  private final String id;
  private final String uniqueId;
  private final UniqueEntity uniqueEntity;
  private int amount;
  private Location location;
  private double leashRange;
  private long respawnSeconds;
  private long chunkKey;

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

    runTaskTimer(StrifePlugin.getInstance(), SPAWNER_OFFSET, 40L);
    SPAWNER_OFFSET++;
    LogUtil.printDebug("Created Spawner with taskId " + getTaskId());
  }

  @Override
  public void run() {
    for (LivingEntity le : entities) {
      if (le == null || !le.isValid()) {
        entities.remove(le);
        continue;
      }
      double xDist = Math.abs(location.getX() - le.getLocation().getX());
      double zDist = Math.abs(location.getZ() - le.getLocation().getZ());
      if (Math.abs(xDist) + Math.abs(zDist) > leashRange) {
        despawnParticles(le);
        if (StrifePlugin.getInstance().getStrifeMobManager().isTrackedEntity(le)) {
          StrifePlugin.getInstance().getStrifeMobManager().removeStrifeMob(le);
        }
        le.remove();
        LogUtil.printDebug("Cancelled SpawnerTimer with id " + getTaskId() + " due to leash range");
      }
    }
    spawnSpawner(this);
  }

  public static void spawnSpawner(Spawner s) {
      if (s.getUniqueEntity() == null || s.getLocation() == null) {
        return;
      }

      int maxMobs = s.getAmount();
      for (long stamp : s.getRespawnTimes()) {
        if (System.currentTimeMillis() > stamp) {
          s.getRespawnTimes().remove(stamp);
        }
      }

      int existingMobs = s.getRespawnTimes().size() + s.getEntities().size();
      if (existingMobs >= maxMobs) {
        return;
      }

      if (!isChuckLoaded(s)) {
        return;
      }

      int mobDiff = maxMobs - existingMobs;
      while (mobDiff > 0) {

        StrifeMob mob = StrifePlugin.getInstance().getUniqueEntityManager()
            .spawnUnique(s.getUniqueEntity(), s.getLocation());

        if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
          Bukkit.getLogger().warning("Spawner failed to spawn unique! " + s.getId());
          continue;
        }

        if (mob.getMods().size() >= 2) {
          announceSpawnToNearbyPlayers(mob, s.getLocation());
        }

        if (StringUtils.isBlank(s.getUniqueEntity().getMount())
            && mob.getEntity().getVehicle() != null) {
          mob.getEntity().getVehicle().remove();
        }

        SpecialStatusUtil.setDespawnOnUnload(mob.getEntity());
        s.addEntity(mob.getEntity());

        // Random displacement to prevent clumping
        if (s.getUniqueEntity().getDisplaceMultiplier() != 0) {
          Vector vec = new Vector(-1 + Math.random() * 2, 0.1, -1 + Math.random() * 2).normalize();
          vec.multiply(s.getUniqueEntity().getDisplaceMultiplier());
          mob.getEntity().setVelocity(vec);
          mob.getEntity().getLocation().setDirection(mob.getEntity().getVelocity().normalize());
        }

        mobDiff--;
      }
  }

  private static void announceSpawnToNearbyPlayers(StrifeMob mob, Location location) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (location.getWorld() != p.getWorld()) {
        continue;
      }
      Vector diff = location.toVector().subtract(p.getLocation().toVector());
      if (diff.lengthSquared() < 6400) {
        p.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 100, 0.8F);
        MessageUtils.sendMessage(p,
            "&7&o&lWoah!! &f" + mob.getEntity().getCustomName() + "&f has spawned nearby!");
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

  public List<LivingEntity> getEntities() {
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
}
