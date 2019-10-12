package land.face.strife.data.effects;

import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

public class Charm extends Effect {

  private static Random random = new Random();

  private boolean overrideMaster;
  private float lifespanSeconds;
  private float chance;
  private float chancePerLevel;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.isCharmImmune() || !(target.getEntity() instanceof Mob) || target
        .getEntity() instanceof Player) {
      return;
    }
    if (!overrideMaster && target.getMaster() != null) {
      return;
    }
    if (caster.getMinions().size() >= caster.getStat(StrifeStat.MAX_MINIONS)) {
      return;
    }
    if (target.getEntity() instanceof Wolf) {
      if (((Wolf) target.getEntity()).getOwner() != null) {
        return;
      }
      ((Wolf) target.getEntity()).setAngry(true);
    }
    if (!rollCharmChance(caster, target)) {
      return;
    }
    ((Mob) target.getEntity()).setTarget(null);
    target.setMaster(caster.getEntity());
    target.setDespawnOnUnload(true);
    target.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, caster.getStat(StrifeStat.MINION_DAMAGE));
    caster.addMinion(target);
    StrifePlugin.getInstance().getMinionManager()
        .addMinion(target.getEntity(), (int) ((lifespanSeconds * 20D) / 11D));
  }

  private boolean rollCharmChance(StrifeMob caster, StrifeMob target) {
    int levelDiff =
        StatUtil.getMobLevel(caster.getEntity()) - StatUtil.getMobLevel(target.getEntity());
    if (levelDiff >= 0) {
      return chance + levelDiff * chancePerLevel > random.nextDouble();
    }
    return chance - (levelDiff * chance * 0.1) > random.nextDouble();
  }

  public void setLifespanSeconds(float lifespanSeconds) {
    this.lifespanSeconds = lifespanSeconds;
  }

  public void setOverrideMaster(boolean overrideMaster) {
    this.overrideMaster = overrideMaster;
  }

  public void setChance(float chance) {
    this.chance = chance;
  }

  public void setChancePerLevel(float chancePerLevel) {
    this.chancePerLevel = chancePerLevel;
  }
}
