/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LastAttackTracker;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class AttackSpeedManager {

  private final StrifePlugin plugin;
  private final Map<UUID, LastAttackTracker> lastAttackMap;
  private final float attackCost;
  private final int warnLevel;

  public AttackSpeedManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.lastAttackMap = new HashMap<>();
    attackCost = (float) plugin.getSettings().getDouble("config.mechanics.energy.attack-cost", 5);
    warnLevel = plugin.getSettings().getInt("config.mechanics.energy.warn-level", 10) + 1;
  }

  public void setAttackTime(UUID uuid, long fullAttackMillis) {
    lastAttackMap.get(uuid).setFullAttackMs(fullAttackMillis);
    lastAttackMap.get(uuid).setLastAttackStamp(System.currentTimeMillis());
  }

  public float getAttackMultiplier(StrifeMob attacker) {
    return getAttackMultiplier(attacker, true);
  }

  public float getAttackMultiplier(StrifeMob attacker, boolean resetTime) {
    if (!(attacker.getEntity() instanceof Player)
        || ((Player) attacker.getEntity()).getGameMode() == GameMode.CREATIVE) {
      return 1f;
    }
    if (!lastAttackMap.containsKey(attacker.getEntity().getUniqueId())) {
      lastAttackMap.put(attacker.getEntity().getUniqueId(), new LastAttackTracker(1L, 1L));
    }
    long millisPassed = getMillisPassed(attacker.getEntity().getUniqueId());
    long fullAttackMillis = getFullAttackMillis(attacker.getEntity().getUniqueId());
    if (resetTime) {
      setAttackTime(attacker.getEntity().getUniqueId(), (long) (1000 * StatUtil.getAttackTime(attacker)));
    }

    float attackMult = Math.min(1, (float) millisPassed / fullAttackMillis);

    if (attacker.hasTrait(StrifeTrait.NO_ENERGY_BASICS)) {
      return attackMult;
    }

    float energyCost = 0.35f * attackCost + 0.65f * attackMult * attackCost;
    float energyMult = Math.min(1, attacker.getEnergy() / energyCost);

    if (resetTime && energyMult < 0.9 && ((Player) attacker.getEntity()).getLevel() < warnLevel) {
      TitleUtils.sendTitle((Player) attacker.getEntity(), "", ChatColor.RED + "Low Energy!", 10, 0, 8);
      MessageUtils.sendMessage(attacker.getEntity(),
          "&e[!] Your energy is low! Try &fattacking slower &eor &fdrinking an energy potion&e! Your energy is shown where your hunger bar normally would be!");
    }

    StatUtil.changeEnergy(attacker, -energyCost);

    return attackMult * energyMult;
  }

  private long getFullAttackMillis(UUID uuid) {
    return lastAttackMap.get(uuid).getFullAttackMs();
  }

  private long getMillisPassed(UUID uuid) {
    return System.currentTimeMillis() - lastAttackMap.get(uuid).getLastAttackStamp();
  }
}
