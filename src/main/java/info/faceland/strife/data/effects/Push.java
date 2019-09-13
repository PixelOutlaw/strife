package info.faceland.strife.data.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.WorldSpaceEffectEntity;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Push extends Effect {

  private double power;
  private double height;
  private boolean cancelFall;
  private PushType pushType;
  private Vector tempVector;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Vector direction;
    switch (pushType) {
      case AWAY_FROM_CASTER:
        direction = getEffectVelocity(caster.getEntity().getLocation().toVector(),
            target.getEntity());
        break;
      case CASTER_DIRECTION:
        Vector casterDir = caster.getEntity().getLocation().getDirection();
        if (casterDir.getX() == 0 && casterDir.getZ() == 0) {
          direction = new Vector(power / 10, 0, 0);
        } else {
          direction = casterDir.setY(0).normalize().multiply(power / 10);
        }
        break;
      case WSE_LOCATION:
        LogUtil.printDebug(tempVector.getX() + " " + tempVector.getY() + " " + tempVector.getZ());
        direction = getEffectVelocity(tempVector, target.getEntity());
        break;
      case WSE_DIRECTION:
        direction = tempVector.clone().setY(0.001).normalize().multiply(power / 10);
        break;
      default:
        return;
    }
    Vector newVelocity = target.getEntity().getVelocity();
    if (cancelFall) {
      if (newVelocity.getY() < 0) {
        newVelocity.setY(0);
      }
      target.getEntity().setFallDistance(0);
    }
    newVelocity.add(direction);
    newVelocity.add(new Vector(0, height / 10, 0));
    target.getEntity().setVelocity(newVelocity);
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

  private Vector getEffectVelocity(Vector originLocation, Entity to) {
    if (originLocation.equals(to.getLocation().toVector())) {
      return new Vector(0, power / 10, 0);
    }
    return to.getLocation().toVector().subtract(originLocation).setY(0.001).normalize()
        .multiply(power / 10);
  }

  public void setPower(double power) {
    this.power = power;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public void setCancelFall(boolean cancelFall) {
    this.cancelFall = cancelFall;
  }

  public void setPushType(PushType pushType) {
    this.pushType = pushType;
  }

  public enum PushType {
    AWAY_FROM_CASTER,
    CASTER_DIRECTION,
    WSE_LOCATION,
    WSE_DIRECTION
  }
}