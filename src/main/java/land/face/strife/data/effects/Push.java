package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.LogUtil;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Push extends Effect {

  private double power;
  private double height;
  private boolean cancelFall;
  private boolean clamp;
  @Setter
  private boolean uncheckedHeight;
  private PushType pushType;
  private Vector tempVector;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity().getType() == EntityType.SHULKER || !target.getEntity().hasGravity()) {
      return;
    }
    if (!isFriendly() && StringUtils.isNotBlank(target.getUniqueEntityId())) {
      if (getPlugin().getUniqueEntityManager().getUnique(target.getUniqueEntityId()).isPushImmune()) {
        return;
      }
    }
    Vector direction;
    switch (pushType) {
      case AWAY_FROM_CASTER:
        direction = getEffectVelocity(caster.getEntity().getLocation().toVector(), target.getEntity());
        break;
      case CASTER_DIRECTION:
        direction = caster.getEntity().getLocation().getDirection();
        if (!uncheckedHeight) {
          direction.setY(0.001);
        }
        direction.normalize().multiply(power / 10);
        break;
      case TEMP_DIRECTION:
        LogUtil.printDebug(tempVector.getX() + " " + tempVector.getY() + " " + tempVector.getZ());
        direction = getEffectVelocity(tempVector, target.getEntity());
        break;
      case WSE_DIRECTION:
        direction = tempVector.clone();
        if (!uncheckedHeight) {
          direction.setY(0.001);
        }
        direction.normalize().multiply(power / 10);
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
    if (uncheckedHeight) {
      if (clamp) {
        newVelocity.setX(clampRay(oldVelocity.getX(), direction.getX()));
        newVelocity.setY(clampRay(oldVelocity.getY(), direction.getY()));
        newVelocity.setZ(clampRay(oldVelocity.getZ(), direction.getZ()));
      } else {
        newVelocity.add(direction);
      }
    } else {
      if (clamp) {
        newVelocity.setX(clampRay(oldVelocity.getX(), direction.getX()));
        newVelocity.setY(clampRay(oldVelocity.getY(), height / 10));
        newVelocity.setZ(clampRay(oldVelocity.getZ(), direction.getZ()));
      } else {
        newVelocity.add(direction);
        newVelocity.add(new Vector(0, height / 10, 0));
      }
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

  public void setTempVector(Vector vector) {
    tempVector = vector;
  }

  public PushType getPushType() {
    return pushType;
  }

  private Vector getEffectVelocity(Vector originLocation, Entity to) {
    if (originLocation.equals(to.getLocation().toVector())) {
      return new Vector(0, power / 10, 0);
    }
    Vector velocity = to.getLocation().toVector().subtract(originLocation);
    if (!uncheckedHeight) {
      velocity.setY(0.001);
    }
    return velocity.normalize().multiply(power / 10);
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