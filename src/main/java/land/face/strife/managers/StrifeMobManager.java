package land.face.strife.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMobManager {

  private final StrifePlugin plugin;
  private final Map<UUID, StrifeMob> trackedEntities = new ConcurrentHashMap<>();

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      StrifeMob strifeMob;
      if (entity instanceof Player) {
        strifeMob = new StrifeMob(plugin.getChampionManager().getChampion((Player) entity));
      } else {
        strifeMob = new StrifeMob(entity);
      }
      strifeMob.setStats(plugin.getMonsterManager().getBaseStats(entity));
      trackedEntities.put(entity.getUniqueId(), strifeMob);
    }
    StrifeMob strifeMob = trackedEntities.get(entity.getUniqueId());
    strifeMob.setLivingEntity(entity);
    plugin.getBarrierManager().createBarrierEntry(strifeMob);
    return strifeMob;
  }

  public void addFiniteEffect(StrifeMob mob, LoreAbility loreAbility, int uses, int maxDuration) {
    for (FiniteUsesEffect finiteUsesEffect : mob.getTempEffects()) {
      if (finiteUsesEffect.getExpiration() > System.currentTimeMillis()) {
        mob.getTempEffects().remove(finiteUsesEffect);
        continue;
      }
      if (finiteUsesEffect.getLoreAbility() == loreAbility) {
        finiteUsesEffect.setExpiration(System.currentTimeMillis() + maxDuration);
        finiteUsesEffect.setUses(Math.max(finiteUsesEffect.getUses(), uses));
        return;
      }
    }
    FiniteUsesEffect finiteUsesEffect = new FiniteUsesEffect();
    finiteUsesEffect.setExpiration(System.currentTimeMillis() + maxDuration);
    finiteUsesEffect.setUses(uses);
    mob.getTempEffects().add(finiteUsesEffect);
  }

  public void addBuff(UUID uuid, String buffId, double durationMultiplier) {
    addBuff(uuid, plugin.getBuffManager().getBuffFromId(buffId), durationMultiplier);
  }

  public void addBuff(UUID uuid, LoadedBuff loadedBuff, double durationMultiplier) {
    StrifeMob strifeMob = trackedEntities.get(uuid);
    Buff buff = plugin.getBuffManager().buildFromLoadedBuff(loadedBuff);
    strifeMob.addBuff(loadedBuff.getId(), buff, loadedBuff.getSeconds() * durationMultiplier);
  }

  public int removeInvalidEntities() {
    int initialSize = trackedEntities.size();
    for (UUID uuid : trackedEntities.keySet()) {
      LivingEntity le = trackedEntities.get(uuid).getEntity();
      if (le != null && le.isValid()) {
        continue;
      }
      remove(uuid);
      plugin.getCounterManager().clearCounters(uuid);
    }
    return initialSize - trackedEntities.size();
  }

  public StrifeMob setEntityStats(LivingEntity entity, Map<StrifeStat, Float> statMap) {
    StrifeMob strifeMob = getStatMob(entity);
    strifeMob.setStats(statMap);
    trackedEntities.put(entity.getUniqueId(), strifeMob);
    return strifeMob;
  }

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity().isValid() && strifeMob.isDespawnOnUnload()) {
        remove(strifeMob.getEntity().getUniqueId());
      }
    }
  }

  public void doChunkDespawn(LivingEntity entity) {
    if (!isTrackedEntity(entity)) {
      return;
    }
    if (trackedEntities.get(entity.getUniqueId()).isDespawnOnUnload()) {
      remove(entity.getUniqueId());
    }
  }

  public StrifeMob getMobUnsafe(UUID uuid) {
    return trackedEntities.getOrDefault(uuid, null);
  }

  public void removeMob(LivingEntity entity) {
    remove(entity.getUniqueId());
  }

  public void removeMob(UUID uuid) {
    remove(uuid);
  }

  public void remove(UUID uuid) {
    StrifeMob mob = trackedEntities.get(uuid);
    if (mob != null) {
      if (mob.getEntity().isValid() && !(mob.getEntity() instanceof Player)) {
        mob.getEntity().remove();
      }
      mob.killAllTasks();
      trackedEntities.remove(uuid);
    }
  }

  public boolean isTrackedEntity(Entity entity) {
    return trackedEntities.containsKey(entity.getUniqueId());
  }
}
