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
      System.out.println("1");
      return;
    }
    if (!overrideMaster && target.getMaster() != null) {
      System.out.println("2");
      return;
    }
    if (caster.getMinions().size() >= caster.getStat(StrifeStat.MAX_MINIONS)) {
      System.out.println("3");
      return;
    }
    if (target.getEntity() instanceof Wolf) {
      if (((Wolf) target.getEntity()).getOwner() != null) {
        System.out.println("4");
        return;
      }
      ((Wolf) target.getEntity()).setAngry(true);
    }
    if (!rollCharmChance(caster, target)) {
      System.out.println("5");
      return;
    }
    ((Mob) target.getEntity()).setTarget(null);
    caster.addMinion(target);
    target.getFactions().clear();
    StrifePlugin.getInstance().getMinionManager()
        .addMinion(target.getEntity(), (int) ((lifespanSeconds * 20D) / 11D));
    StrifePlugin.getInstance().getSpawnerManager().addRespawnTime(target.getEntity());
  }

  private boolean rollCharmChance(StrifeMob caster, StrifeMob target) {
    int levelDiff = StatUtil.getMobLevel(caster.getEntity()) - StatUtil.getMobLevel(target.getEntity());
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
