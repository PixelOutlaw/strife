package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Effect {

  private String name;
  private boolean selfAffect;
  private boolean friendly;
  private double range;
  Map<StrifeAttribute, Double> statMults = new HashMap<>();

  public void execute(AttributedEntity caster, LivingEntity target) {
    if (range < 1) {
      apply(caster, target);
      return;
    }
    for (LivingEntity le : getTargets(caster.getEntity(), target, range)) {
      apply(caster, le);
    }
  }

  public void apply(AttributedEntity caster, LivingEntity target) {

  }

  public List<LivingEntity> getTargets(LivingEntity caster, LivingEntity target, double range) {
    if (target == null) {
      LogUtil.printError("Effect " + name + " cast without a target!");
      return null;
    }
    if (range < 1) {
      List<LivingEntity> targets = new ArrayList<>();
      targets.add(target);
      return targets;
    }
    return selectNearbyTargets(caster, target);
  }

  private List<LivingEntity> selectNearbyTargets(LivingEntity caster, LivingEntity target) {
    List<LivingEntity> targets = new ArrayList<>();
    for (Entity e : target.getNearbyEntities(range, range, range)) {
      if (e instanceof LivingEntity && target.hasLineOfSight(e)) {
        targets.add((LivingEntity) e);
      }
    }
    if (!selfAffect) {
      targets.remove(caster);
    }
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

  public void setSelfAffect(boolean selfHarm) {
    this.selfAffect = selfHarm;
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
