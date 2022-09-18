package land.face.strife.tasks;

import static org.bukkit.potion.PotionEffectType.POISON;
import static org.bukkit.potion.PotionEffectType.REGENERATION;
import static org.bukkit.potion.PotionEffectType.WITHER;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.RestoreData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeTask extends BukkitRunnable {

  private static final long REGEN_TICK_RATE = 10L;
  private static final float REGEN_PERCENT_PER_SECOND = 0.1F;
  private static final float POTION_REGEN_FLAT_PER_LEVEL = 2f;
  private static final float POTION_REGEN_PERCENT_PER_LEVEL = 0.05f;
  private final WeakReference<StrifeMob> parentMob;
  private final List<RestoreData> lifeRestore = new ArrayList<>();

  private final float tickMultiplier;

  public LifeTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 5L, REGEN_TICK_RATE);
    tickMultiplier = (1 / (20f / REGEN_TICK_RATE)) * REGEN_PERCENT_PER_SECOND;
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }

    double maxLife = mob.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();

    if (mob.getEntity().getHealth() >= maxLife) {
      getBonusHealth();
      return;
    }

    PlayerDataUtil.restoreHealth(mob.getEntity(), getBonusHealth());

    float lifeAmount = StatUtil.getStat(mob, StrifeStat.REGENERATION);
    if (mob.getEntity().getType() == EntityType.PLAYER && !mob.isInCombat()) {
      lifeAmount *= 1.5;
      lifeAmount += 4;
    }

    if (mob.getEntity().hasPotionEffect(REGENERATION)) {
      int potionIntensity = mob.getEntity().getPotionEffect(REGENERATION).getAmplifier() + 1;
      lifeAmount += potionIntensity * POTION_REGEN_FLAT_PER_LEVEL;
      lifeAmount += potionIntensity * maxLife * POTION_REGEN_PERCENT_PER_LEVEL;
    }
    if (mob.getEntity().hasPotionEffect(WITHER)) {
      lifeAmount *= 0.33f;
    }
    if (mob.getEntity().hasPotionEffect(POISON)) {
      lifeAmount *= 0.33f;
    }
    if (mob.getEntity().getFireTicks() > 0) {
      lifeAmount *= 0.4f;
    }
    lifeAmount *= tickMultiplier;

    mob.getEntity().setHealth(Math.min(mob.getEntity().getHealth() + lifeAmount, maxLife));
  }

  public void addHealingOverTime(float amount, int ticks) {
    int regenTicks = (int) Math.floor((float) ticks / REGEN_TICK_RATE);
    float regenValue = amount / regenTicks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(regenValue);
    restoreData.setTicks(regenTicks);
    lifeRestore.add(restoreData);
  }

  private float getBonusHealth() {
    if (lifeRestore.isEmpty()) {
      return 0;
    }
    float amount = 0;
    Iterator<RestoreData> iterator = lifeRestore.iterator();
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

}
