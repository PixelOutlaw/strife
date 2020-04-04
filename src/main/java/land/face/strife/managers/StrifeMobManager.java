package land.face.strife.managers;

import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMobManager {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, StrifeMob> trackedEntities = new WeakHashMap<>();

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public int getMobCount() {
    return trackedEntities.size();
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity)) {
      StrifeMob strifeMob;
      if (entity instanceof Player) {
        strifeMob = new StrifeMob(plugin.getChampionManager().getChampion((Player) entity));
      } else {
        strifeMob = new StrifeMob(entity);
      }
      strifeMob.setStats(plugin.getMonsterManager().getBaseStats(entity));
      trackedEntities.put(entity, strifeMob);
    }
    StrifeMob strifeMob = trackedEntities.get(entity);
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

  public void addBuff(LivingEntity entity, String buffId, double durationMultiplier) {
    addBuff(entity, plugin.getBuffManager().getBuffFromId(buffId), durationMultiplier);
  }

  public void addBuff(LivingEntity entity, LoadedBuff loadedBuff, double durationMultiplier) {
    StrifeMob strifeMob = trackedEntities.get(entity);
    Buff buff = plugin.getBuffManager().buildFromLoadedBuff(loadedBuff);
    strifeMob.addBuff(loadedBuff.getId(), buff, loadedBuff.getSeconds() * durationMultiplier);
  }

  public StrifeMob setEntityStats(LivingEntity entity, Map<StrifeStat, Float> statMap) {
    StrifeMob strifeMob = getStatMob(entity);
    strifeMob.setStats(statMap);
    trackedEntities.put(entity, strifeMob);
    return strifeMob;
  }

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity().isValid() && strifeMob.isDespawnOnUnload()) {
        strifeMob.getEntity().remove();
      }
    }
  }

  public void removeEntity(LivingEntity entity) {
    trackedEntities.remove(entity);
  }

  public void doChunkDespawn(LivingEntity entity) {
    if (!isTrackedEntity(entity)) {
      return;
    }
    if (trackedEntities.get(entity).isDespawnOnUnload()) {
      entity.remove();
    }
  }

  public boolean isTrackedEntity(LivingEntity entity) {
    return trackedEntities.containsKey(entity);
  }
}
