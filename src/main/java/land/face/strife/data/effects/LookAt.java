package land.face.strife.data.effects;

import com.ticxo.modelengine.api.ModelEngineAPI;
import io.papermc.paper.entity.TeleportFlag.Relative;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class LookAt extends Effect {

  @Setter
  private boolean casterToTarget;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    LivingEntity fromTarget = casterToTarget ? caster.getEntity() : target.getEntity();
    LivingEntity toTarget = casterToTarget ? target.getEntity() : caster.getEntity();
    forceLook(fromTarget, toTarget);
  }

  public static void forceLook(LivingEntity fromTarget, LivingEntity toTarget) {
    if (fromTarget == toTarget) {
      return;
    }
    if (fromTarget instanceof Mob) {
      ((Mob) fromTarget).lookAt(toTarget);
    }
    Location newLoc = fromTarget.getLocation().clone();
    newLoc.setDirection(toTarget.getLocation().subtract(fromTarget.getLocation()).toVector().normalize());
    fromTarget.teleport(newLoc, TeleportCause.PLUGIN, Relative.VELOCITY_ROTATION);
    var me = ModelEngineAPI.getModeledEntity(fromTarget);
    if (me == null || me.getModels().isEmpty()) {
      return;
    }
    me.getBase().getBodyRotationController().setYBodyRot(fromTarget.getLocation().getYaw());
    me.getBase().getBodyRotationController().setYHeadRot(fromTarget.getLocation().getYaw());
    me.getBase().getBodyRotationController().setXHeadRot(fromTarget.getLocation().getPitch());
  }
}
