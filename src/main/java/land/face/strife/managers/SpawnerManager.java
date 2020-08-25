package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.Spawner;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.SpecialStatusUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SpawnerManager {

  private StrifePlugin plugin;

  private final Map<String, Spawner> spawnerMap;

  public SpawnerManager(StrifePlugin plugin) {
    this.plugin = plugin;
    spawnerMap = new HashMap<>();
  }

  public Map<String, Spawner> getSpawnerMap() {
    return spawnerMap;
  }

  public void setSpawnerMap(Map<String, Spawner> spawnerMap) {
    this.spawnerMap.clear();
    this.spawnerMap.putAll(spawnerMap);
  }

  public void addSpawner(String id, Spawner spawner) {
    this.spawnerMap.put(id, spawner);
  }

  public void removeSpawner(String id) {
    this.spawnerMap.remove(id);
  }

  public void addRespawnTime(LivingEntity livingEntity) {
    for (Spawner spawner : spawnerMap.values()) {
      spawner.addRespawnTimeIfApplicable(livingEntity);
    }
  }

  public void spawnSpawners() {
    for (Spawner s : spawnerMap.values()) {
      if (s.getUniqueEntity() == null || s.getLocation() == null) {
        continue;
      }

      int maxMobs = s.getAmount();
      for (long stamp : s.getRespawnTimes()) {
        if (System.currentTimeMillis() > stamp) {
          s.getRespawnTimes().remove(stamp);
        }
      }

      int existingMobs = s.getRespawnTimes().size() + s.getEntities().size();
      if (existingMobs >= maxMobs) {
        continue;
      }

      if (!isChuckLoaded(s)) {
        continue;
      }

      int mobDiff = maxMobs - existingMobs;
      while (mobDiff > 0) {

        StrifeMob mob = plugin.getUniqueEntityManager()
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
  }

  private void announceSpawnToNearbyPlayers(StrifeMob mob, Location location) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (location.getWorld() != p.getWorld()) {
        continue;
      }
      Vector diff = location.toVector().subtract(p.getLocation().toVector());
      if (diff.lengthSquared() < 6400) {
        p.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 100, 0.5F);
        MessageUtils.sendMessage(p,
            "&7&o&lWoah!! &f" + mob.getEntity().getCustomName() + "&f has spawned nearby!");
      }
    }
  }

  public void cancelAll() {
    for (Spawner spawner : spawnerMap.values()) {
      spawner.cancel();
    }
  }

  private boolean isChuckLoaded(Spawner spawner) {
    return spawner.getLocation().getWorld().isChunkLoaded(spawner.getChunkX(), spawner.getChunkZ());
  }
}
