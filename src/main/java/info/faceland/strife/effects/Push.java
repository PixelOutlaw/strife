package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Push extends Effect {

  private double power;
  private double height;
  private boolean zeroVelocity;
  private PushType pushType;
  private Vector tempVector;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Vector direction;
    switch (pushType) {
      case AWAY_FROM_CASTER:
        direction = getEffectVelocity(caster.getEntity(), target.getEntity());
        break;
      case CASTER_DIRECTION:
        direction = caster.getEntity().getEyeLocation().getDirection().setY(0).normalize()
            .multiply(power / 10);
        break;
      case BACKWARDS:
        direction = target.getEntity().getEyeLocation().getDirection().setY(0).normalize()
            .multiply(-1).multiply(power / 10);
        break;
      case WSE_LOCATION:
        direction = target.getEntity().getVelocity();
        direction.add(target.getEntity().getLocation().toVector().subtract(tempVector).setY(0)
            .normalize().multiply(power / 10));
        break;
      case WSE_DIRECTION:
        direction = tempVector.clone().setY(0).normalize().multiply(power);
        break;
      default:
        return;
    }
    if (zeroVelocity) {
      target.getEntity().setVelocity(new Vector());
      target.getEntity().setFallDistance(0);
    }
    direction.add(new Vector(0, height / 10, 0));
    target.getEntity().setVelocity(direction);
  }

  public void setTempVectorFromWSE(WorldSpaceEffectEntity entity) {
    if (pushType == PushType.WSE_DIRECTION) {
      tempVector = entity.getVelocity().normalize();
      return;
    }
    if (pushType == PushType.WSE_LOCATION) {
      tempVector = entity.getLocation().toVector();
      return;
    }
    throw new IllegalArgumentException("Can only set temp vector with a WSE knockback type");
  }

  private Vector getEffectVelocity(Entity from, Entity to) {
    return to.getLocation().toVector().subtract(from.getLocation().toVector()).setY(0)
        .normalize().multiply(power / 10);
  }

  public void setPower(double power) {
    this.power = power;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public void setZeroVelocity(boolean zeroVelocity) {
    this.zeroVelocity = zeroVelocity;
  }

  public void setPushType(PushType pushType) {
    this.pushType = pushType;
  }

  public enum PushType {
    AWAY_FROM_CASTER,
    CASTER_DIRECTION,
    BACKWARDS,
    WSE_LOCATION,
    WSE_DIRECTION
  }
}