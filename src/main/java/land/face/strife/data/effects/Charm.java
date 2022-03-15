package land.face.strife.data.effects;

import java.util.Random;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.MinionTask;
import land.face.strife.util.StatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

public class Charm extends Effect {

  private static final Random random = new Random();

  private boolean overrideMaster;
  private float lifespanSeconds;
  private float chance;
  private float chancePerLevel;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.isCharmImmune() || !(target.getEntity() instanceof Mob)
        || target.getEntity() instanceof Player) {
      return;
    }
    if (!overrideMaster && target.getMaster() != null || !rollCharmChance(caster, target)) {
      return;
    }

    if (target.getEntity() instanceof Wolf) {
      if (((Wolf) target.getEntity()).getOwner() != null) {
        return;
      }
      ((Wolf) target.getEntity()).setAngry(true);
    }

    float lifespan = lifespanSeconds * (1 + (caster.getStat(StrifeStat.EFFECT_DURATION) / 100));
    caster.addMinion(target, (int) lifespan);

    float damageRatio = 1 + caster.getStat(StrifeStat.MINION_DAMAGE) / 1000;
    float lifeRatio = 1 + caster.getStat(StrifeStat.MINION_LIFE) / 1000;

    target.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, damageRatio);
    double maxHealth = target.getEntity().getMaxHealth() * lifeRatio;
    target.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    target.getEntity().setHealth(maxHealth);

    getPlugin().getSpawnerManager().addRespawnTime(target.getEntity());

    MinionTask.expireMinions(caster);
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
