package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.RestoreData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;
  private final List<RestoreData> energyRestore = new ArrayList<>();
  public static final int TICKS_PER = 10;
  public static final float TICK_MULT = 1f / (20f / TICKS_PER);
  public static final Map<Integer, Float> SPRINT_COST_MAP = buildSprintCostMap();

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

    Player player = null;
    if (mob.getEntity().getType() == EntityType.PLAYER) {
      player = (Player) mob.getEntity();
    }

    if (mob.getEnergy() >= mob.getMaxEnergy() && (player != null && !player.isSprinting())) {
      mob.setEnergy(mob.getMaxEnergy());
      getBonusEnergy();
      return;
    }

    float energyChange = 0;
    if (!mob.hasTrait(StrifeTrait.NO_ENERGY_REGEN)) {
      energyChange = mob.getStat(StrifeStat.ENERGY_REGEN);
      energyChange *= TICK_MULT * 0.1f;
      energyChange *= getHungerPotionMult(mob.getEntity());
    } else {
      if (!mob.isInCombat()) {
        energyChange = mob.getStat(StrifeStat.ENERGY_REGEN);
        // Slight penalty, even when out of combat
        energyChange *= TICK_MULT * 0.08f;
        energyChange *= getHungerPotionMult(mob.getEntity());
      }
    }

    if (player != null) {
      if (player.isDead() || player.getGameMode() == GameMode.CREATIVE) {
        return;
      }
      if (player.isSprinting()) {
        if (mob.getEnergy() < 0.5) {
          player.setSprinting(false);
          player.setFoodLevel(4);
        } else {
          energyChange *= StrifePlugin.RUN_COST_PERCENT;
          if (!mob.hasTrait(StrifeTrait.RUNNER)) {
            energyChange -= StrifePlugin.RUN_COST * getAgilityMult(mob);
          }
        }
      } else if (MoveUtil.hasMoved(player)) {
        energyChange *= StrifePlugin.WALK_COST_PERCENT;
        if (!mob.hasTrait(StrifeTrait.RUNNER)) {
          energyChange -= StrifePlugin.WALK_COST * getAgilityMult(mob);
        }
        player.setFoodLevel(19);
      }
    }

    energyChange += getBonusEnergy();
    mob.setEnergy(mob.getEnergy() + energyChange);
  }

  private static float getAgilityMult(StrifeMob mob) {
    int agility = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), LifeSkillType.AGILITY);
    return SPRINT_COST_MAP.get(agility);
  }

  public void addEnergyOverTime(float amount, int ticks) {
    int regenTicks = (int) Math.floor((float) ticks / TICKS_PER);
    float regenValue = amount / regenTicks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(regenValue);
    restoreData.setTicks(regenTicks);
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
      ratio += (float) (0.1 * (livingEntity.getPotionEffect(PotionEffectType.SATURATION).getAmplifier() + 1));
    }
    if (livingEntity.hasPotionEffect(PotionEffectType.HUNGER)) {
      ratio -= (float) (0.1 * (livingEntity.getPotionEffect(PotionEffectType.HUNGER).getAmplifier() + 1));
    }
    return ratio;
  }

  private static Map<Integer, Float> buildSprintCostMap() {
    Map<Integer, Float> cachedSprintCosts = new HashMap<>();
    cachedSprintCosts.put(-5, 1.5f);
    cachedSprintCosts.put(-4, 1.4f);
    cachedSprintCosts.put(-3, 1.3f);
    cachedSprintCosts.put(-2, 1.2f);
    cachedSprintCosts.put(-1, 1.1f);
    cachedSprintCosts.put(0, 1.0f);
    for (int i = 1; i <= 120; i++) {
      cachedSprintCosts.put(i, (float) Math.pow(0.99, i - 1));
    }
    return cachedSprintCosts;
  }

}
