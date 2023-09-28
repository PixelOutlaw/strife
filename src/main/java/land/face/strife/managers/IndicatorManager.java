package land.face.strife.managers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.DamagePopoff;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.PopoffUtil;
import land.face.strife.util.TargetingUtil;
import net.md_5.bungee.api.ChatColor;
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
  private final Vector midFloatVector;
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
    float midFloatSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.float-mid-speed", 0.2);
    float fastFloatSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.float-fast-speed", 0.2);
    float bounceSpeed = (float) plugin.getSettings()
        .getDouble("config.indicators.bounce-speed", 0.1);

    bounceVector = new Vector(0, bounceSpeed, 0);
    slowFloatVector = new Vector(0, slowFloatSpeed, 0);
    midFloatVector = new Vector(0, midFloatSpeed, 0);
    fastFloatVector = new Vector(0, fastFloatSpeed, 0);
  }

  public void addIndicator(LivingEntity viewer, Location location, IndicatorStyle type, int life, String text,
      float startScale, float midScale, float endScale) {
    if (!(viewer instanceof Player) || location.getWorld() != viewer.getWorld()) {
      return;
    }

    double distance = viewer.getLocation().distanceSquared(location);
    if (distance > 1024) {
      return;
    }
    text = ChatColor.stripColor(text);
    Vector diff = viewer.getEyeLocation().toVector().subtract(location.toVector());
    Location midway;
    if (distance < 64) {
      diff.multiply(-0.5);
    } else {
      diff.normalize();
      diff.multiply(-4);
    }
    midway = viewer.getEyeLocation().clone().add(diff);

    Vector velocity = null;
    double gravity = 0;
    switch (type) {
      case RANDOM_POPOFF -> {
        velocity = new Vector(
            -randomDamageHSpeed + 2 * Math.random() * randomDamageHSpeed,
            randomDamageVSpeed / 2 + Math.random() * randomDamageVSpeed,
            -randomDamageHSpeed + 2 * Math.random() * randomDamageHSpeed);
        gravity = randomDamageGravity;
      }
      case BOUNCE -> {
        velocity = bounceVector.clone();
        gravity = bounceGravity;
      }
      case FLOAT_UP_FAST -> velocity = fastFloatVector.clone();
      case FLOAT_UP_MEDIUM -> velocity = midFloatVector.clone();
      case FLOAT_UP_SLOW -> velocity = slowFloatVector.clone();
    }

    DamagePopoff damagePopoff = PopoffUtil.createPopoff((Player) viewer,
        midway, velocity, gravity, life, text, startScale, midScale, endScale);
    indicators.add(damagePopoff);
  }

  public void addIndicator(LivingEntity creator, LivingEntity target, IndicatorStyle type, int life, String text,
      float startScale, float midScale, float endScale) {
    Location loc = TargetingUtil.getOriginLocation(target, OriginLocation.BELOW_HEAD);
    if (!loc.getWorld().getName().equals(target.getEyeLocation().getWorld().getName())) {
      return;
    }
    addIndicator(creator, loc, type, life, text, startScale, midScale, endScale);
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
    FLOAT_UP_MEDIUM,
    FLOAT_UP_SLOW
  }

}
