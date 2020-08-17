package land.face.strife.managers;

import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.FiniteUsesEffect;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMobManager {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, StrifeMob> trackedEntities = new WeakHashMap<>();

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Map<LivingEntity, StrifeMob> getMobs() {
    return trackedEntities;
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (entity == null) {
      return null;
    }
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

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity().isValid() && strifeMob.isDespawnOnUnload()) {
        strifeMob.getEntity().remove();
      }
    }
  }

  public void removeEntity(LivingEntity entity) {
    if (entity.getPassengers().size() > 0 && entity.getPassengers().get(0) instanceof Item) {
      entity.getPassengers().get(0).remove();
    }
    trackedEntities.remove(entity);
  }

  public void doChunkDespawn(LivingEntity entity) {
    if (!isTrackedEntity(entity)) {
      return;
    }
    if (trackedEntities.get(entity).isDespawnOnUnload()) {
      entity.remove();
      removeEntity(entity);
    }
  }

  public boolean isTrackedEntity(LivingEntity entity) {
    return trackedEntities.containsKey(entity);
  }
}
