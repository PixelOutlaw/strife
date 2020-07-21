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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.data.RestoreData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.MoveUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyRegenTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  private float walkCostFlat;
  private float walkCostPercent;
  private float runCostFlat;
  private float runCostPercent;
  private float agilityExp;
  private Map<UUID, List<RestoreData>> energyRestore = new HashMap<>();

  public EnergyRegenTask(StrifePlugin plugin) {
    this.plugin = plugin;
    walkCostFlat = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.walk-cost-flat", 3) / 20;
    walkCostPercent = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.walk-regen-percent", 0.75);
    runCostFlat = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.run-cost-flat", 10) / 20;
    runCostPercent = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.run-regen-percent", 0.25);
    agilityExp = (float) plugin.getSettings()
        .getDouble("config.mechanics.energy.agility-xp", 10) / 20;
  }

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.isDead() || player.getGameMode() == GameMode.CREATIVE) {
        continue;
      }
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);

      double agility = PlayerDataUtil.getLifeSkillLevel(mob.getChampion(), LifeSkillType.AGILITY);
      double agilityMult = 50.0 / (50 + agility);

      float energy = 0;
      energy += 0.005f * mob.getStat(StrifeStat.ENERGY_REGEN);
      energy *= getHungerPotionMult(mob.getEntity());

      if (player.getFoodLevel() > 6 && player.isSprinting()) {
        energy *= runCostPercent;
        energy -= runCostFlat * agilityMult;
      } else if (MoveUtil.hasMoved(player)) {
        if (player.isSprinting()) {
          player.setSprinting(false);
        }
        energy *= walkCostPercent;
        energy -= walkCostFlat * agilityMult;
      } else {
        if (player.isSprinting()) {
          player.setSprinting(false);
        }
      }

      energy += getBonusEnergy(player.getUniqueId());
      plugin.getEnergyManager().changeEnergy(mob, energy);
    }
  }

  public void addEnergy(UUID uuid, float amount, int ticks) {
    if (!energyRestore.containsKey(uuid)) {
      energyRestore.put(uuid, new CopyOnWriteArrayList<>());
    }
    amount /= ticks;
    RestoreData restoreData = new RestoreData();
    restoreData.setAmount(amount);
    restoreData.setTicks(ticks);
    energyRestore.get(uuid).add(restoreData);
  }

  private float getBonusEnergy(UUID uuid) {
    if (!energyRestore.containsKey(uuid)) {
      return 0;
    }
    float amount = 0;
    for (RestoreData data : energyRestore.get(uuid)) {
      amount += data.getAmount();
      if (data.getTicks() == 0) {
        energyRestore.get(uuid).remove(data);
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
