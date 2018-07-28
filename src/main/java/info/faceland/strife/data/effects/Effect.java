package info.faceland.strife.data.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.EntityStatCache;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Effect {

  final EntityStatCache entityStatCache;

  private String name;
  private boolean selfAffect;
  private boolean friendly;
  private double range;
  Map<StrifeAttribute, Double> statMults = new HashMap<>();

  public Effect() {
    this.entityStatCache = StrifePlugin.getInstance().getEntityStatCache();
  }

  public void execute(AttributedEntity caster, LivingEntity target) {
    if (range < 1) {
      LogUtil.printDebug("Applying effect to " + target.getCustomName());
      apply(caster, target);
      return;
    }
    for (LivingEntity le : getTargets(caster.getEntity(), target)) {
      LogUtil.printDebug("Applying effect to " + le.getCustomName());
      apply(caster, le);
    }
  }

  public void apply(AttributedEntity caster, LivingEntity target) {

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

  public boolean isSelfAffect() {
    return selfAffect;
  }

  public void setSelfAffect(boolean selfAffect) {
    this.selfAffect = selfAffect;
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
    this.statMults = statMults;
  }
}
