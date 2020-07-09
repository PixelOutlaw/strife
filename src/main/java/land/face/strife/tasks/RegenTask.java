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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.data.RestoreData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegenTask extends BukkitRunnable {

  public static int REGEN_TICK_RATE = 4;

  private final StrifePlugin plugin;
  private static float REGEN_PERCENT_PER_SECOND = 0.1f;
  private static float POTION_REGEN_FLAT_PER_LEVEL = 2f;
  private static float POTION_REGEN_PERCENT_PER_LEVEL = 0.05f;

  private Map<UUID, List<RestoreData>> lifeRestore = new HashMap<>();

  public RegenTask(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.getHealth() <= 0 || player.isDead() || player.getGameMode() == GameMode.CREATIVE) {
        continue;
      }
      float tickMult = (1 / (20f / REGEN_TICK_RATE)) * REGEN_PERCENT_PER_SECOND;
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);

      PlayerDataUtil.restoreHealth(player, getBonusHealth(player.getUniqueId()));

      plugin.getBarrierManager()
          .restoreBarrier(mob, mob.getStat(StrifeStat.BARRIER_REGEN) * tickMult);
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

      player.setHealth(Math.min(player.getHealth() + lifeAmount, playerMaxHealth));
    }
  }

  public void addHealing(UUID uuid, float amount, int ticks) {
    if (!lifeRestore.containsKey(uuid)) {
      lifeRestore.put(uuid, new CopyOnWriteArrayList<>());
    }
    amount = (amount * REGEN_TICK_RATE) / ticks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(amount);
    restoreData.setTicks(ticks / REGEN_TICK_RATE);
    lifeRestore.get(uuid).add(restoreData);
  }

  private float getBonusHealth(UUID uuid) {
    if (!lifeRestore.containsKey(uuid)) {
      return 0;
    }
    float amount = 0;
    for (RestoreData data : lifeRestore.get(uuid)) {
      amount += data.getAmount();
      if (data.getTicks() == 0) {
        lifeRestore.get(uuid).remove(data);
      } else {
        data.setTicks(data.getTicks() - 1);
      }
    }
    return amount;
  }
}
