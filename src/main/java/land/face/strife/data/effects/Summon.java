package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

public class Summon extends LocationEffect {

  private String uniqueEntity;
  private String soundEffect;
  private int amount;
  private double lifespanSeconds;
  private boolean mount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (int i = 0; i < amount; i++) {
      Location loc = target.getEntity().getLocation();
      applyAtLocation(caster, loc);
    }
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    if (caster.getMinions().size() >= caster.getStat(StrifeStat.MAX_MINIONS)) {
      return;
    }

    StrifeMob summonedEntity = StrifePlugin.getInstance().getUniqueEntityManager()
        .spawnUnique(uniqueEntity, location);

    if (summonedEntity == null || summonedEntity.getEntity() == null) {
      return;
    }

    LivingEntity summon = summonedEntity.getEntity();
    caster.addMinion(summonedEntity);

    StrifePlugin.getInstance().getMinionManager()
        .addMinion(summon, (int) ((lifespanSeconds * 20D) / 11D));

    if (caster.getEntity() instanceof Mob && summon instanceof Mob) {
      ((Mob) summon).setTarget(((Mob) caster.getEntity()).getTarget());
    }

    if (summon instanceof Tameable && caster.getEntity() instanceof Player) {
      ((Tameable) summon).setOwner((Player) caster.getEntity());
    }

    if (soundEffect != null) {
      PlaySound sound = (PlaySound) StrifePlugin.getInstance().getEffectManager()
          .getEffect(soundEffect);
      sound.applyAtLocation(caster, summon.getLocation());
    }

    if (mount) {
      summon.addPassenger(caster.getEntity());
    }
    double maxHealth = summon.getMaxHealth() * (1 + (caster.getStat(StrifeStat.MINION_LIFE) / 100));
    summon.setHealth(maxHealth);
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

  public void setMount(boolean mount) {
    this.mount = mount;
  }

  public void setSoundEffect(String soundEffect) {
    this.soundEffect = soundEffect;
  }
}
