package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.RestoreData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;
  private final List<RestoreData> energyRestore = new ArrayList<>();

  public EnergyTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 20L, 1L);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null) {
      cancel();
      return;
    }

    if (mob.getEnergy() >= StatUtil.getMaximumEnergy(mob)) {
      return;
    }

    float energy = 0;
    boolean noRegen = mob.hasTrait(StrifeTrait.NO_ENERGY_REGEN);
    if (!noRegen) {
      energy += 0.005f * mob.getStat(StrifeStat.ENERGY_REGEN);
      energy *= getHungerPotionMult(mob.getEntity());
    }

    if (mob.getEntity() instanceof Player) {
      Player player = (Player) mob.getEntity();
      if (player.isDead() || player.getGameMode() == GameMode.CREATIVE) {
        return;
      }

      double agility = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), LifeSkillType.AGILITY);
      double agilityMult = 50.0 / (50 + agility);

      if (player.getFoodLevel() > 6 && player.isSprinting()) {
        energy *= StrifePlugin.RUN_COST_PERCENT;
        energy -= StrifePlugin.RUN_COST * agilityMult;
      } else if (MoveUtil.hasMoved(player)) {
        if (player.isSprinting()) {
          player.setSprinting(false);
        }
        energy *= StrifePlugin.WALK_COST_PERCENT;
        if (!noRegen) {
          energy -= StrifePlugin.WALK_COST * agilityMult;
        }
      } else {
        if (player.isSprinting()) {
          player.setSprinting(false);
        }
      }
    }

    energy += getBonusEnergy();
    mob.setEnergy(mob.getEnergy() + energy);
  }

  public void addEnergyOverTime(float amount, int ticks) {
    amount = amount / ticks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(amount);
    restoreData.setTicks(ticks);
    energyRestore.add(restoreData);
  }

  private float getBonusEnergy() {
    if (energyRestore.isEmpty()) {
      return 0;
    }
    float amount = 0;
    Iterator<RestoreData> iterator = energyRestore.iterator();
    while (iterator.hasNext()) {
      RestoreData data = iterator.next();
      amount += data.getAmount();
      if (data.getTicks() == 0) {
        iterator.remove();
      } else {
        data.setTicks(data.getTicks() - 1);
      }
    }
    return amount;
  }

  private float getHungerPotionMult(LivingEntity livingEntity) {
    float ratio = 1;
    if (livingEntity.hasPotionEffect(PotionEffectType.SATURATION)) {
      ratio += 0.1 * (livingEntity.getPotionEffect(PotionEffectType.SATURATION).getAmplifier() + 1);
    }
    if (livingEntity.hasPotionEffect(PotionEffectType.HUNGER)) {
      ratio -= 0.1 * (livingEntity.getPotionEffect(PotionEffectType.HUNGER).getAmplifier() + 1);
    }
    return ratio;
  }

}
