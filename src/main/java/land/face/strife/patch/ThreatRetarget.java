package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

public class ThreatRetarget implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(StrifePlugin.getInstance(), "threat_retarget");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final WeakReference<StrifeMob> strifeMob;
  private final WeakReference<Mob> mob;
  private long retargetTimestamp = 0;

  public ThreatRetarget(Mob mob) {
    this.mob = new WeakReference<>(mob);
    this.strifeMob = new WeakReference<>(StrifePlugin.getInstance().getStrifeMobManager().getStatMob(mob));
  }

  @Override
  public boolean shouldActivate() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return false;
    }
    if (mob.getTarget() == null) {
      return false;
    }
    if (retargetTimestamp > System.currentTimeMillis()) {
      return false;
    }
    retargetTimestamp = System.currentTimeMillis() + 2000;
    LivingEntity target = getHighestThreat();
    if (target == null) {
      return false;
    }
    return true;
  }

  @Override
  public boolean shouldStayActive() {
    return false;
  }

  @Override
  public void start() {
    // Nothing
  }

  @Override
  public void stop() {
    // Nothing
  }

  @Override
  public void tick() {
    // Nothing
  }

  @Override
  public @NotNull GoalKey<Mob> getKey() {
    return goalKey;
  }

  @Override
  public @NotNull EnumSet<GoalType> getTypes() {
    return EnumSet.of(GoalType.TARGET);
  }

  private LivingEntity getHighestThreat() {
    Mob mob = this.mob.get();
    StrifeMob strifeMob = this.strifeMob.get();
    if (mob == null || strifeMob == null || mob.getTarget() == null) {
      return null;
    }
    double range = mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue();
    Collection<LivingEntity> nearbyTargets = mob.getWorld()
        .getNearbyEntitiesByType(LivingEntity.class, mob.getLocation(), range);
    if (nearbyTargets.isEmpty()) {
      return null;
    }
    UUID currentTarget = mob.getTarget().getUniqueId();
    UUID targetUuid = null;
    float currentVal = 0f;
    for (var e : strifeMob.getThreatTargets().entrySet()) {
      if (e.getValue() > currentVal) {
        targetUuid = e.getKey();
        currentVal = e.getValue();
      }
    }
    if (targetUuid == null || currentTarget.equals(targetUuid)) {
      return null;
    }
    LivingEntity targetEntity = null;
    for (LivingEntity le : nearbyTargets) {
      if (le.getUniqueId().equals(targetUuid)) {
        targetEntity = le;
        break;
      }
    }
    if (targetEntity == null) {
      return null;
    }
    EntityTargetLivingEntityEvent ev = new EntityTargetLivingEntityEvent(mob,
        targetEntity, TargetReason.TARGET_ATTACKED_NEARBY_ENTITY);
    Bukkit.getPluginManager().callEvent(ev);
    if (ev.isCancelled()) {
      return null;
    }
    return targetEntity;
  }
}
