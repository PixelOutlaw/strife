package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Summon extends Effect {

  private String uniqueEntity;
  private String soundEffect;
  private int amount;
  private double lifespanSeconds;
  private double spawnRange;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (int i = 0; i < amount; i++) {
      if (caster.getEntity() instanceof Player) {
        if (caster.getMinions().size() >= caster.getStat(StrifeStat.MAX_MINIONS)) {
          break;
        }
      }
      Location loc =
          target == null ? caster.getEntity().getLocation() : target.getEntity().getLocation();
      LivingEntity summon = StrifePlugin.getInstance().getUniqueEntityManager()
          .spawnUnique(uniqueEntity, loc);
      if (summon == null) {
        return;
      }
      StrifeMob summonedEntity = StrifePlugin.getInstance()
          .getStrifeMobManager().getStatMob(summon);
      summonedEntity.forceSetStat(StrifeStat.MINION_MULT_INTERNAL,
          caster.getStat(StrifeStat.MINION_DAMAGE));
      caster.getMinions().add(summonedEntity);
      summonedEntity.setDespawnOnUnload(true);
      StrifePlugin.getInstance().getMinionManager()
          .addMinion(summon, (int) ((lifespanSeconds * 20D) / 11D));
      if (soundEffect != null) {
        PlaySound sound = (PlaySound) StrifePlugin.getInstance().getEffectManager()
            .getEffect(soundEffect);
        summon.getWorld().playSound(summon.getLocation(),
            sound.getSound(), sound.getVolume(), sound.getPitch());
      }
    }
  }

  public void setUniqueEntity(String uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setSpawnRange(double spawnRange) {
    this.spawnRange = spawnRange;
  }

  public void setLifespanSeconds(double lifespanSeconds) {
    this.lifespanSeconds = lifespanSeconds;
  }

  public void setSoundEffect(String soundEffect) {
    this.soundEffect = soundEffect;
  }
}
