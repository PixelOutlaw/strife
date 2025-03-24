package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@Setter
public class Push extends Effect {

  private PushType pushType;
  private double power, height, weightRatio, verticalMultiplier;
  private boolean zeroFall, horizontalClamp, verticalClamp, cancelHorizontal, applyToCaster, useTrueDirection;

  public static Location TEMP_ORIGIN;
  public static Vector TEMP_DIRECTION;

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
    Vector dir = switch (pushType) {
      case AWAY_FROM_CASTER -> getLocationDiff(caster.getEntity().getLocation().toVector(), target.getEntity());
      case TARGET_DIRECTION -> target.getEntity().getEyeLocation().getDirection();
      case AWAY_FROM_CENTER -> getLocationDiff(TEMP_ORIGIN.toVector(), target.getEntity());
      case CASTER_DIRECTION -> caster.getEntity().getEyeLocation().getDirection();
      case TEMP_DIRECTION -> TEMP_DIRECTION.clone();
    };

    Vector push = dir.clone();
    if (useTrueDirection) {
      var powerMult = power / 10;
      push.multiply(powerMult);
      push.setY(push.getY() + height / 10);
    } else {
      var horizontalMult = power / 10;
      push.setX(push.getX() * horizontalMult);
      push.setZ(push.getZ() * horizontalMult);
      push.setY(height / 10);
    }

    double weightMult;
    if (applyToCaster) {
      weightMult = 200f / (100f + caster.getStat(StrifeStat.WEIGHT));
      double difference = 1 - weightMult;
      weightMult = 1 + difference * weightRatio;
    } else {
      weightMult = 200f / (100f + target.getStat(StrifeStat.WEIGHT));
      double difference = 1 - weightMult;
      weightMult = 1 + difference * weightRatio;
    }
    push.multiply(weightMult);

    Vector currentVelocity = MoveUtil.getVelocity(target.getEntity());

    if (cancelHorizontal) {
      currentVelocity.setX(0);
      currentVelocity.setZ(0);
    }

    if (zeroFall && currentVelocity.getY() < 0) {
      currentVelocity.setY(0);
      target.getEntity().setFallDistance(0);
    }

    push.setY(push.getY() * verticalMultiplier);

    Vector newVelocity = currentVelocity.clone();
    newVelocity.add(push);


    if (verticalClamp) {
      newVelocity.setY(clampRay(currentVelocity.getY(), push.getY()));
    }

    if (horizontalClamp) {
      newVelocity.setX(clampRay(currentVelocity.getX(), push.getX()));
      newVelocity.setZ(clampRay(currentVelocity.getZ(), push.getZ()));
    }


    if (applyToCaster) {
      caster.getEntity().setVelocity(newVelocity);
      if (caster.isPlayer()) {
        MoveUtil.setVelocity((Player) caster.getEntity(), newVelocity);
      } else {
        var lookLoc = caster.getEntity().getEyeLocation().clone();
        lookLoc.add(newVelocity.clone().normalize().multiply(5));
        lookLoc.setDirection(newVelocity.clone().normalize());
        ((Mob) caster.getEntity()).lookAt(lookLoc);
        ((Mob) caster.getEntity()).getPathfinder().stopPathfinding();
      }
    } else {
      target.getEntity().setVelocity(newVelocity);
      if (target.isPlayer()) {
        MoveUtil.setVelocity((Player) target.getEntity(), newVelocity);
      } else {
        var lookLoc = target.getEntity().getEyeLocation().clone();
        lookLoc.add(newVelocity.clone().normalize().multiply(5));
        lookLoc.setDirection(newVelocity.clone().normalize());
        ((Mob) target.getEntity()).lookAt(lookLoc);
        ((Mob) target.getEntity()).getPathfinder().stopPathfinding();
      }
    }
  }

  private Vector getLocationDiff(Vector originLocation, Entity to) {
    if (originLocation.equals(to.getLocation().toVector())) {
      return new Vector(0, 1, 0);
    }
    return to.getLocation().toVector().subtract(originLocation).normalize();
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

  public enum PushType {
    AWAY_FROM_CASTER,
    TARGET_DIRECTION,
    AWAY_FROM_CENTER,
    CASTER_DIRECTION,
    TEMP_DIRECTION
  }
}