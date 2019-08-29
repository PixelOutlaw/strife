package info.faceland.strife.data;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.effects.AreaEffect;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.PlaySound;
import info.faceland.strife.effects.Push;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.managers.EffectManager;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.TargetingUtil;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class WorldSpaceEffectEntity {

  private static EffectManager EFFECT_MANAGER = StrifePlugin.getInstance().getEffectManager();

  private final Map<Integer, List<Effect>> effectSchedule;
  private final int maxTicks;
  private final Vector velocity;
  private final StrifeMob caster;
  private Location location;
  private boolean casterLock;
  private int currentTick;
  private int lifespan;

  public WorldSpaceEffectEntity(final StrifeMob caster,
      final Map<Integer, List<Effect>> effectSchedule, Location location, final boolean casterLock,
      final Vector velocity, final int maxTicks, int lifespan) {
    this.caster = caster;
    this.effectSchedule = effectSchedule;
    this.velocity = velocity;
    this.maxTicks = maxTicks;
    this.lifespan = lifespan;
    this.casterLock = casterLock;
    this.location = location;
    this.currentTick = 0;
  }

  public Vector getVelocity() {
    return velocity;
  }

  public Location getLocation() {
    return location;
  }

  public boolean tick() {
    if (casterLock) {
      location = TargetingUtil.getOriginLocation(caster.getEntity(), OriginLocation.CENTER);
    } else {
      location.add(velocity);
    }
    Block block = location.getBlock();
    if (!(block == null || block.getType().isTransparent())) {
      return false;
    }
    if (effectSchedule.containsKey(currentTick)) {
      List<Effect> effects = effectSchedule.get(currentTick);
      for (Effect effect : effects) {
        if (effect == null) {
          LogUtil.printError("Null WSE effect! Tick:" + currentTick);
          continue;
        }
        if (effect instanceof SpawnParticle) {
          ((SpawnParticle) effect).playAtLocation(location);
          continue;
        }
        if (effect instanceof PlaySound) {
          ((PlaySound) effect).playAtLocation(location);
          continue;
        }
        applyDirectionToPushEffects(effect);
        EFFECT_MANAGER.executeEffectAtLocation(effect, caster, location);
      }
    }
    lifespan--;
    if (lifespan < 0) {
      return false;
    }
    currentTick++;
    if (currentTick > maxTicks) {
      currentTick = 1;
    }
    return true;
  }

  private void applyDirectionToPushEffects(Effect effect) {
    if (!(effect instanceof AreaEffect)) {
      return;
    }
    for (Effect areaEffect : ((AreaEffect) effect).getEffects()) {
      if (areaEffect instanceof Push) {
        ((Push) areaEffect).setTempVectorFromWSE(this);
      }
    }
  }
}
