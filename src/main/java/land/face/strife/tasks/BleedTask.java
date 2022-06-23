/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.LogUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BleedTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;
  private float amount;

  private static final ItemStack BLOCK_DATA = new ItemStack(Material.REDSTONE);

  private static float flatBleedPerTick = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.base-bleed-damage", 1);
  private static float percentBleedPerTick = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.config.mechanics.percent-bleed-damage", 0.04);

  public BleedTask(StrifeMob mob, float amount) {
    this.parentMob = new WeakReference<>(mob);
    this.amount = amount;
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(),
        Sound.ENTITY_SHEEP_SHEAR, 1f, 0.6f);
    runTaskTimer(StrifePlugin.getInstance(), 1L, 4L);
  }

  @Override
  public void run() {
    if (!parentMob.get().getEntity().isValid()) {
      parentMob.get().clearBleed();
      return;
    }

    float bleed = flatBleedPerTick + amount * percentBleedPerTick;

    spawnBleedParticles(parentMob.get().getEntity(), bleed);
    DamageUtil.dealRawDamage(parentMob.get(), bleed);

    amount -= bleed;

    if (amount <= 0) {
      LogUtil.printDebug("Bleed complete, removing");
      parentMob.get().clearBleed();
    }
  }

  public float getBleed() {
    return amount;
  }

  public void bumpBleed(float amount) {
    this.amount += amount;
    parentMob.get().getEntity().getWorld().playSound(parentMob.get().getEntity().getLocation(),
        Sound.ENTITY_SHEEP_SHEAR, 1f, 0.6f);
  }

  public static void spawnBleedParticles(LivingEntity entity, double damage) {
    int particleAmount = Math.min(2 + (int) (damage * 10), 40);
    entity.getWorld().spawnParticle(
        Particle.ITEM_CRACK,
        entity.getEyeLocation().clone().add(0, -entity.getEyeHeight() / 2, 0),
        particleAmount,
        0.0, 0.0, 0.0,
        0.1,
        BLOCK_DATA
    );
  }
}
