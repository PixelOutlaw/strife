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
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;
  private final List<RestoreData> energyRestore = new ArrayList<>();
  public static final int TICKS_PER = 4;
  public static final float TICK_MULT = 1f / (20f / TICKS_PER);

  public EnergyTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 20L, TICKS_PER);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }

    if (mob.getStat(StrifeStat.ENERGY) < 0.1) {
      return;
    }

    if (mob.getEnergy() >= mob.getMaxEnergy()) {
      mob.setEnergy(mob.getMaxEnergy());
      getBonusEnergy();
      return;
    }

    float energy = 0;
    boolean noRegen = mob.hasTrait(StrifeTrait.NO_ENERGY_REGEN);
    if (!noRegen) {
      energy += TICK_MULT * 0.1f * mob.getStat(StrifeStat.ENERGY_REGEN);
      energy *= getHungerPotionMult(mob.getEntity());
    }

    if (mob.getEntity() instanceof Player) {
      Player player = (Player) mob.getEntity();
      if (player.isDead() || player.getGameMode() == GameMode.CREATIVE) {
        return;
      }

      if (player.getFoodLevel() > 6 && player.isSprinting()) {
        energy *= StrifePlugin.RUN_COST_PERCENT;
        energy -= StrifePlugin.RUN_COST * getAgilityMult(mob);
      } else if (MoveUtil.hasMoved(player)) {
        if (player.isSprinting()) {
          player.setSprinting(false);
        }
        energy *= StrifePlugin.WALK_COST_PERCENT;
        if (!noRegen) {
          energy -= StrifePlugin.WALK_COST * getAgilityMult(mob);
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

  private static float getAgilityMult(StrifeMob mob) {
    float agility = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), LifeSkillType.AGILITY);
    return 50f / (50f + agility);
  }

  public void addEnergyOverTime(float amount, int ticks) {
    amount = (amount * TICKS_PER) / ticks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(amount);
    restoreData.setTicks((int) ((float) ticks / TICKS_PER));
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
