package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class Summon extends Effect {

  private String uniqueEntity;
  private String soundEffect;
  private int amount;
  private double lifespanSeconds;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (int i = 0; i < amount; i++) {
      Location loc = target.getEntity().getLocation();
      summonAtLocation(caster, loc);
    }
  }

  public void summonAtLocation(StrifeMob caster, Location location) {
    if (caster.getMinions().size() >= caster.getStat(StrifeStat.MAX_MINIONS)) {
      return;
    }
    StrifeMob summonedEntity = StrifePlugin.getInstance().getUniqueEntityManager()
        .spawnUnique(uniqueEntity, location);
    if (summonedEntity == null || summonedEntity.getEntity() == null) {
      return;
    }
    LivingEntity summon = summonedEntity.getEntity();
    summon.setMaxHealth(summon.getMaxHealth() * (1 + (caster.getStat(StrifeStat.MINION_LIFE) / 100)));
    summon.setHealth(summon.getMaxHealth());
    summonedEntity.forceSetStat(
        StrifeStat.MINION_MULT_INTERNAL, caster.getStat(StrifeStat.MINION_DAMAGE));
    summonedEntity.setDespawnOnUnload(true);
    caster.addMinion(summonedEntity);

    StrifePlugin.getInstance().getMinionManager()
        .addMinion(summon, (int) ((lifespanSeconds * 20D) / 11D));

    if (caster.getEntity() instanceof Mob && summon instanceof Mob) {
      ((Mob)summon).setTarget(((Mob) caster.getEntity()).getTarget());
    }

    if (soundEffect != null) {
      PlaySound sound = (PlaySound) StrifePlugin.getInstance().getEffectManager().getEffect(soundEffect);
      sound.playAtLocation(summon.getLocation());
    }
  }

  public void setUniqueEntity(String uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setLifespanSeconds(double lifespanSeconds) {
    this.lifespanSeconds = lifespanSeconds;
  }

  public void setSoundEffect(String soundEffect) {
    this.soundEffect = soundEffect;
  }
}
