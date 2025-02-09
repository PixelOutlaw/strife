package land.face.strife.data;

import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.CreateModelAnimation;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ContinuousModelData extends BukkitRunnable {

  public static final Map<LivingEntity, List<ContinuousModelData>> TASKS = new HashMap<>();

  private CreateModelAnimation anim;
  private ActiveModel model;
  private Dummy<?> dummy;
  private StrifeMob target;
  private int ticksRemaining;
  private int startDeathTick;
  private boolean dying = false;

  @Override
  public void run() {
    ticksRemaining--;
    if (!dying && startDeathTick >= ticksRemaining) {
      doExpiry();
    }
    if (ticksRemaining <= 0) {
      dummy.setRemoved(true);
      cancel();
      return;
    }
    if (anim.isEntityLock()) {
      Location newLoc = TargetingUtil.getOriginLocation(target.getEntity(), anim.getOrigin());
      Vector direction = target.getEntity().getEyeLocation().getDirection();
      float pitch = target.getEntity().getPitch();
      float yaw = target.getEntity().getYaw();
      if (!anim.isRotationLock()) {
        newLoc.setDirection(direction);
        dummy.setLocation(newLoc);
        dummy.setYBodyRot(yaw);
        dummy.setXHeadRot(pitch);
        dummy.setYHeadRot(yaw);
      } else {
        dummy.setLocation(newLoc);
      }
    }
  }

  public static void create(CreateModelAnimation anim, ActiveModel model, Dummy<?> dummy, StrifeMob target,
      float realSpeed, int ticksRemaining, int startDeathTick) {
    ContinuousModelData cmd = new ContinuousModelData();
    cmd.setAnim(anim);
    cmd.setModel(model);
    cmd.setDummy(dummy);
    cmd.setTarget(target);
    cmd.setTicksRemaining(ticksRemaining);
    cmd.setStartDeathTick(startDeathTick);

    if (!anim.getAnimations().isEmpty()) {
      Collections.shuffle(anim.getAnimations());
      model.getAnimationHandler().forceStopAllAnimations();
      model.getAnimationHandler().playAnimation(anim.getAnimations().getFirst(), anim.getLerpIn(), anim.getLerpOut(), realSpeed, true);
    }

    if (anim.getStartAnimation() != null) {
      model.getAnimationHandler().playAnimation(anim.getStartAnimation(), 0, 0, 1, true);
    }

    if (TASKS.containsKey(target.getEntity())) {
      TASKS.get(target.getEntity()).add(cmd);
    } else {
      TASKS.put(target.getEntity(), new ArrayList<>());
      TASKS.get(target.getEntity()).add(cmd);
    }

    cmd.runTaskTimer(StrifePlugin.getInstance(), 0L, 1L);
  }

  private void doExpiry() {
    dying = true;
    if (anim.getEndAnimation() != null) {
      model.getAnimationHandler().playAnimation(anim.getEndAnimation(), 0, 0, 1, true);
    }
    var list = TASKS.get(target.getEntity());
    if (list.size() <= 1) {
      TASKS.remove(target.getEntity());
    } else {
      list.remove(this);
    }
  }

  public static ContinuousModelData fetch(LivingEntity targetEntity, CreateModelAnimation createModelAnimation) {
    if (!createModelAnimation.isRefreshModel()) {
      return null;
    }
    var list = TASKS.get(targetEntity);
    if (list == null || list.isEmpty()) {
      return null;
    }
    for (var l : list) {
      if (l.getAnim() == createModelAnimation) {
        return l;
      }
    }
    return null;
  }

  public void refresh(int ticksRemaining) {
    this.ticksRemaining = ticksRemaining;
  }
}
