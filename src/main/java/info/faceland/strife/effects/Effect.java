package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Effect {

  private String name;
  private boolean selfHarm;
  private boolean friendly;

  double flatValue;
  double range;
  Map<StrifeAttribute, Double> statMults;

  public void execute(AttributedEntity caster, LivingEntity target) {

  }

  public List<LivingEntity> getTargets(LivingEntity caster, LivingEntity target, double range) {
    if (target == null) {
      StrifePlugin.getInstance().getLogger().severe("Effect " + name + " cast without a target!");
      return null;
    }
    if (range == 0) {
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
    if (!selfHarm) {
      targets.remove(caster);
    }
    return targets;
  }
}
