package info.faceland.strife.data;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.PlaySound;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.managers.EffectManager;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class WorldSpaceEffectEntity {

  private final static EffectManager EFFECT_MANAGER = StrifePlugin.getInstance().getEffectManager();

  private final Map<Integer, List<Effect>> effectSchedule;
  private final int maxTicks;
  private final Vector velocity;
  private final StrifeMob caster;

  private Location location;
  private int currentTick;
  private int lifespan;

  public WorldSpaceEffectEntity(final StrifeMob caster,
      final Map<Integer, List<Effect>> effectSchedule, Location location, final Vector velocity,
      final int maxTicks, int lifespan) {
    this.caster = caster;
    this.effectSchedule = effectSchedule;
    this.velocity = velocity;
    this.maxTicks = maxTicks;
    this.lifespan = lifespan;

    this.location = location;
    this.currentTick = 0;
  }

  public boolean tick() {
    location.add(velocity);
    Block block = location.getBlock();
    if (!(block == null || block.getType().isTransparent())) {
      return false;
    }
    if (effectSchedule.containsKey(currentTick)) {
      List<Effect> effects = effectSchedule.get(currentTick);
      for (Effect effect : effects) {
        if (effect instanceof SpawnParticle && effect.getRange() == 0) {
          ((SpawnParticle) effect).playAtLocation(location);
        } else if (effect instanceof PlaySound && effect.getRange() == 0) {
          ((PlaySound) effect).playAtLocation(location);
        } else {
          EFFECT_MANAGER.executeEffectAtLocation(effect, caster, location);
        }
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
}
