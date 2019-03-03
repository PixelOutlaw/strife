package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.managers.AttributedEntityManager;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Effect {

  private static final AttributedEntityManager ATTRIBUTED_ENTITY_MANAGER = StrifePlugin.getInstance().getAttributedEntityManager();
  private String name;
  private boolean forceTargetCaster;
  private boolean friendly;
  private double range;
  final Map<StrifeAttribute, Double> statMults = new HashMap<>();
  private final List<Condition> conditions = new ArrayList<>();

  public void execute(AttributedEntity caster, AttributedEntity target) {
    if (forceTargetCaster) {
      target = caster;
    }
    if (!PlayerDataUtil.areConditionsMet(caster, target, conditions)) {
      LogUtil.printDebug("Conditions not met for effect. Failed.");
      return;
    }
    for (LivingEntity le : getTargets(caster.getEntity(), target.getEntity())) {
      LogUtil.printDebug("Applying effect to " + PlayerDataUtil.getName(le));
      apply(caster, ATTRIBUTED_ENTITY_MANAGER.getAttributedEntity(le));
    }
  }

  public void apply(AttributedEntity caster, AttributedEntity target) {

  }

  private List<LivingEntity> getTargets(LivingEntity caster, LivingEntity target) {
    List<LivingEntity> targets = new ArrayList<>();
    if (target == null) {
      LogUtil.printError("Effect " + name + " cast without a target!");
      return targets;
    }
    if (range < 1) {
      LogUtil.printDebug("Effect " + name + " cast on self (range < 1)");
      targets.add(target);
      return targets;
    }
    for (Entity e : target.getNearbyEntities(range, range, range)) {
      if (e instanceof LivingEntity && target.hasLineOfSight(e)) {
        targets.add((LivingEntity) e);
      }
    }
    targets.remove(caster);
    LogUtil.printDebug("Effect " + name + " found " + targets.size() + " targets");
    return targets;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isForceTargetCaster() {
    return forceTargetCaster;
  }

  public void setForceTargetCaster(boolean forceTargetCaster) {
    this.forceTargetCaster = forceTargetCaster;
  }

  public boolean isFriendly() {
    return friendly;
  }

  public void setFriendly(boolean friendly) {
    this.friendly = friendly;
  }

  public double getRange() {
    return range;
  }

  public void setRange(double range) {
    this.range = range;
  }

  public Map<StrifeAttribute, Double> getStatMults() {
    return statMults;
  }

  public void setStatMults(Map<StrifeAttribute, Double> statMults) {
    this.statMults.clear();
    this.statMults.putAll(statMults);
  }

  public void addCondition(Condition condition) {
    if (!conditions.contains(condition)) {
      conditions.add(condition);
    }
  }
}
