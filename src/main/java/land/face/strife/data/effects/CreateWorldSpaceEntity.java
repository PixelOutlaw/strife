package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
@Setter
public class CreateWorldSpaceEntity extends LocationEffect {

  private final Map<Integer, List<Effect>> cachedEffectSchedule = new HashMap<>();

  private Map<Integer, List<String>> effectSchedule;
  private List<Effect> expiryEffects = new ArrayList<>();
  private List<Effect> contactEffects = new ArrayList<>();

  private OriginLocation originLocation;
  private int lifespan, maxTicks, maxFallTicks, quantity;
  private float gravity, friction, velocity, maxDisplacement, modelSize, verticalOffset, initialAngle, separationAngle;
  private boolean strictDuration, zeroVerticalAxis, destroyOnContact, restoreSpeedOnGroundContact, destroyOnSharpAngle,
      reachScaling;
  private String modelEffect;
  private Color color;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    apply(caster, TargetingUtil.getOriginLocation(target.getEntity(), originLocation));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    apply(caster, location);
  }

  public void apply(StrifeMob caster, Location location) {
    cacheEffects();
    var baseDirection = caster.getEntity().getEyeLocation().getDirection().clone();
    if (isZeroVerticalAxis()) {
      baseDirection.setY(0.00005);
      baseDirection.normalize();
    }
    if (quantity > 1) {
      float angleIncrement = separationAngle <= 0 ? (6.28319f / quantity) : separationAngle;
      float totalSpread = (quantity - 1) * angleIncrement;
      float currentAngle = initialAngle == -420 ? (float) (Math.random() * 6.28319f) : initialAngle - totalSpread / 2;
      int amount = quantity;
      while (amount > 0) {
        location = location.clone();
        var dir = baseDirection.clone().rotateAroundY(currentAngle);
        location.setDirection(dir);
        currentAngle += angleIncrement;
        amount--;
        getPlugin().getWseManager().createAtTarget(caster, location, this);
      }
    } else {
      location.setDirection(baseDirection);
      getPlugin().getWseManager().createAtTarget(caster, location, this);
    }
  }

  private void cacheEffects() {
    if (cachedEffectSchedule.isEmpty() && !effectSchedule.isEmpty()) {
      for (int i : effectSchedule.keySet()) {
        List<Effect> effectList = new ArrayList<>();
        for (String s : effectSchedule.get(i)) {
          effectList.add(StrifePlugin.getInstance().getEffectManager().getEffect(s));
        }
        cachedEffectSchedule.put(i, effectList);
      }
      effectSchedule.clear();
    }
  }
}