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

import static org.bukkit.potion.PotionEffectType.POISON;
import static org.bukkit.potion.PotionEffectType.REGENERATION;
import static org.bukkit.potion.PotionEffectType.WITHER;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegenTask extends BukkitRunnable {

  private final StrifePlugin plugin;
  public static int REGEN_TICK_RATE = 10;
  private static float REGEN_PERCENT_PER_SECOND = 0.1f;
  private static float POTION_REGEN_FLAT_PER_LEVEL = 5f;
  private static float POTION_REGEN_PERCENT_PER_LEVEL = 0.05f;

  public RegenTask(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.getHealth() <= 0 || player.isDead()) {
        continue;
      }
      float tickMult = (1 / (20f / REGEN_TICK_RATE)) * REGEN_PERCENT_PER_SECOND;
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
      if (plugin.getBarrierManager().hasBarrierEntry(player)) {
        plugin.getBarrierManager()
            .restoreBarrier(mob, mob.getStat(StrifeStat.BARRIER_REGEN) * tickMult);
      }
      double playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
      if (player.getHealth() >= playerMaxHealth) {
        continue;
      }
      float lifeAmount = StatUtil.getRegen(mob);
      if (player.hasPotionEffect(REGENERATION)) {
        int potionIntensity = player.getPotionEffect(REGENERATION).getAmplifier() + 1;
        lifeAmount += potionIntensity * POTION_REGEN_FLAT_PER_LEVEL;
        lifeAmount += potionIntensity * playerMaxHealth * POTION_REGEN_PERCENT_PER_LEVEL;
      }
      if (player.hasPotionEffect(WITHER) || player.hasPotionEffect(POISON)) {
        lifeAmount *= 0.1f;
      }
      if (player.getFireTicks() > 0) {
        lifeAmount *= 0.4f;
      }
      lifeAmount *= tickMult;

      if (player.getFoodLevel() <= 6) {
        lifeAmount *= player.getFoodLevel() / 6F;
      }
      player.setHealth(Math.min(player.getHealth() + lifeAmount, playerMaxHealth));
    }
  }
}
