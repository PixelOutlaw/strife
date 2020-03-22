package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.data.WorldSpaceEffect;
import land.face.strife.util.LogUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Push extends Effect {

  private double power;
  private double height;
  private boolean cancelFall;
  private boolean clamp;
  private PushType pushType;
  private Vector tempVector;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity().getType() == EntityType.SHULKER) {
      return;
    }
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
      case TEMP_DIRECTION:
        LogUtil.printDebug(tempVector.getX() + " " + tempVector.getY() + " " + tempVector.getZ());
        direction = getEffectVelocity(tempVector, target.getEntity());
        break;
      case WSE_DIRECTION:
        direction = tempVector.clone().setY(0.001).normalize().multiply(power / 10);
        break;
      default:
        return;
    }
    Vector oldVelocity = target.getEntity().getVelocity().clone();
    Vector newVelocity = oldVelocity.clone();
    if (cancelFall) {
      if (oldVelocity.getY() < 0) {
        oldVelocity.setY(0);
      }
      target.getEntity().setFallDistance(0);
    }
    if (clamp) {
      newVelocity.setX(clampRay(oldVelocity.getX(), direction.getX()));
      newVelocity.setY(clampRay(oldVelocity.getY(), height / 10));
      newVelocity.setZ(clampRay(oldVelocity.getZ(), direction.getZ()));
    } else {
      newVelocity.add(direction);
      newVelocity.add(new Vector(0, height / 10, 0));
    }
    target.getEntity().setVelocity(newVelocity);
  }

  private double clampRay(double old, double change) {
    if (change == 0) {
      return old;
    }
    if (change > 0) {
      return Math.min(old + change, change);
    } else {
      return Math.max(old + change, change);
    }
  }

  public void setTempVectorFromWSE(WorldSpaceEffect entity) {
    if (pushType == PushType.WSE_DIRECTION) {
      tempVector = entity.getLocation().getDirection().clone().normalize();
      return;
    }
    if (pushType == PushType.TEMP_DIRECTION) {
      tempVector = entity.getLocation().toVector();
    }
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

  public void setClamp(boolean clamp) {
    this.clamp = clamp;
  }

  public void setPushType(PushType pushType) {
    this.pushType = pushType;
  }

  public enum PushType {
    AWAY_FROM_CASTER,
    CASTER_DIRECTION,
    TEMP_DIRECTION,
    WSE_DIRECTION
  }
}