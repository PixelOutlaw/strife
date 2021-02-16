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
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeTask extends BukkitRunnable {

  private static final long REGEN_TICK_RATE = 3L;
  private static final float REGEN_PERCENT_PER_SECOND = 0.1F;
  private static final float POTION_REGEN_FLAT_PER_LEVEL = 2f;
  private static final float POTION_REGEN_PERCENT_PER_LEVEL = 0.05f;

  private final WeakReference<StrifeMob> parentMob;
  private final List<RestoreData> lifeRestore = new ArrayList<>();

  public LifeTask(StrifeMob parentMob) {
    this.parentMob = new WeakReference<>(parentMob);
    this.runTaskTimer(StrifePlugin.getInstance(), 20L, REGEN_TICK_RATE);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null) {
      cancel();
      return;
    }
    if (!mob.getEntity().isValid() || mob.getEntity().getHealth() <= 0) {
      lifeRestore.clear();
      return;
    }
    double maxLife = mob.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();

    if (mob.getEntity().getHealth() >= maxLife) {
      getBonusHealth();
      return;
    }

    float tickMultiplier = (1 / (20f / REGEN_TICK_RATE)) * REGEN_PERCENT_PER_SECOND;

    PlayerDataUtil.restoreHealth(mob.getEntity(), getBonusHealth());

    float lifeAmount = StatUtil.getRegen(mob);

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
    amount = (amount * REGEN_TICK_RATE) / ticks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(amount);
    restoreData.setTicks((int) ((float) ticks / REGEN_TICK_RATE));
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
