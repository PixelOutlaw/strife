package land.face.strife.managers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.DamagePopoff;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.PopoffUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class IndicatorManager {

  private final Set<DamagePopoff> indicators = new HashSet<>();
  private final float randomDamageVSpeed;
  private final float randomDamageHSpeed;
  private final float randomDamageGravity;
  private final float bounceGravity;

  private final Vector slowFloatVector;
  private final Vector fastFloatVector;
  private final Vector bounceVector;

  public IndicatorManager(StrifePlugin plugin) {
    randomDamageHSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.damage-horizontal-speed", 0.2);
    randomDamageVSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.damage-vertical-speed", 0.2);
    randomDamageGravity = (float) plugin.getSettings()
        .getDouble("config.indicators.damage-gravity", 0.1);
    bounceGravity = (float) plugin.getSettings()
        .getDouble("config.indicators.bounce-gravity", 0.2);

    float slowFloatSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.float-slow-speed", 0.2);
    float fastFloatSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.float-fast-speed", 0.2);
    float bounceSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.bounce-speed", 0.1);

    bounceVector = new Vector(0, bounceSpeed, 0);
    slowFloatVector = new Vector(0, slowFloatSpeed, 0);
    fastFloatVector = new Vector(0, fastFloatSpeed, 0);
  }

  public void addIndicator(LivingEntity creator, LivingEntity target, IndicatorStyle type, int life, String text) {
    if (!(creator instanceof Player) || creator == target || target.getWorld() != creator.getWorld()) {
      return;
    }

    Location loc = TargetingUtil.getOriginLocation(target, OriginLocation.BELOW_HEAD);
    if (!loc.getWorld().getName().equals(target.getEyeLocation().getWorld().getName())) {
      return;
    }

    double distance = creator.getLocation().distanceSquared(target.getLocation());
    if (distance > 1024) {
      return;
    }

    Location midway;
    if (distance < 144) {
      midway = creator.getEyeLocation().clone().add(creator.getEyeLocation().clone().subtract(loc).multiply(-0.65));
    } else {
      midway = creator.getEyeLocation().clone().add(creator.getEyeLocation().clone().subtract(loc).toVector()
          .normalize().multiply(-8));
    }

    Vector velocity = null;
    double gravity = 0;
    switch (type) {
      case RANDOM_POPOFF:
        velocity = new Vector(
            -randomDamageHSpeed + 2 * Math.random() * randomDamageHSpeed,
            randomDamageVSpeed / 2 + Math.random() * randomDamageVSpeed,
            -randomDamageHSpeed + 2 * Math.random() * randomDamageHSpeed);
        gravity = randomDamageGravity;
        break;
      case BOUNCE:
        velocity = bounceVector.clone();
        gravity = bounceGravity;
        break;
      case FLOAT_UP_FAST:
        velocity = fastFloatVector.clone();
        break;
      case FLOAT_UP_SLOW:
        velocity = slowFloatVector.clone();
        break;
    }

    DamagePopoff damagePopoff = PopoffUtil.createPopoff((Player) creator, midway, velocity, gravity, life, text);
    indicators.add(damagePopoff);
  }

  public void tickAllIndicators() {
    Iterator<DamagePopoff> iterator = indicators.iterator();
    while (iterator.hasNext()) {
      boolean delete = PopoffUtil.tickDamagePopoff(iterator.next());
      if (delete) {
        iterator.remove();
      }
    }
  }

  public enum IndicatorStyle {
    RANDOM_POPOFF,
    BOUNCE,
    FLOAT_UP_FAST,
    FLOAT_UP_SLOW
  }

}
