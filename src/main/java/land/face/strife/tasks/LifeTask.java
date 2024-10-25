package land.face.strife.tasks;

import static org.bukkit.potion.PotionEffectType.POISON;
import static org.bukkit.potion.PotionEffectType.WITHER;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.RestoreData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DOTUtil;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.scheduler.BukkitRunnable;

public class LifeTask extends BukkitRunnable {

  private static final long REGEN_TICK_RATE = 10L;
  private static final float REGEN_PERCENT_PER_SECOND = 0.1F;
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

    float maxLife = (float) mob.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    DamageUtil.restoreHealth(mob.getEntity(), getBonusHealth(), false, false);
    float lifeAmount = StatUtil.getLifeRegen(mob);

    float damage = 0;
    if (mob.getEntity().hasPotionEffect(POISON)) {
      damage += DOTUtil.getPoisonDamage(mob);
      lifeAmount *= 0.33f;
    }
    if (mob.getEntity().getFireTicks() > 0) {
      lifeAmount *= 0.5f;
      if (mob.hasTrait(StrifeTrait.BARRIER_NO_BURN) && mob.getBarrier() > 0.1) {
        // Do nothing
      } else {
        damage += mob.damageBarrier(DOTUtil.getFireDamage(mob));
      }
    }
    if (mob.isBleeding()) {
      damage += DOTUtil.tickBleedDamage(mob);
    }
    if (mob.getFrost() > 99.5 && mob.getFrostGraceTicks() > 2) {
      damage += 5;
    }

    lifeAmount *= tickMultiplier;
    lifeAmount = lifeAmount - damage;

    if (lifeAmount != 0) {
      if (lifeAmount > 0) {
        if (!mob.getEntity().hasPotionEffect(WITHER)) {
          mob.getEntity().setHealth(Math.min(mob.getEntity().getHealth() + lifeAmount, maxLife));
        }
      } else {
        if (!mob.isInvincible()) {
          DamageUtil.dealRawDamage(mob, StrifePlugin.getInstance().getDamageManager().doEnergyAbsorb(mob, -lifeAmount));
        }
      }
    }
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
