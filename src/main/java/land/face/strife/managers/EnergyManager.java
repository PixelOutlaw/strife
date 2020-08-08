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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.Player;

public class EnergyManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Float> energyMap = new HashMap<>();

  public EnergyManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void setEnergyUnsafe(UUID uuid, float amount) {
    energyMap.put(uuid, amount);
  }

  public void changeEnergy(Player player, float amount) {
    changeEnergy(plugin.getStrifeMobManager().getStatMob(player), amount);
  }

  public void changeEnergy(StrifeMob mob, float amount) {
    if (!(mob.getEntity() instanceof Player)) {
      return;
    }
    float energy = getEnergy(mob);
    energy += amount;
    setEnergy(mob, energy);
    updateFoodBar((Player) mob.getEntity(), StatUtil.getMaximumEnergy(mob));
  }

  public float getEnergy(StrifeMob mob) {
    return energyMap.getOrDefault(mob.getEntity().getUniqueId(),
        StatUtil.getMaximumEnergy(mob) * (((Player) mob.getEntity()).getFoodLevel()) / 20);
  }

  private void setEnergy(StrifeMob mob, float newEnergy) {
    newEnergy = Math.min(newEnergy, StatUtil.getMaximumEnergy(mob));
    newEnergy = Math.max(0, newEnergy);
    energyMap.put(mob.getEntity().getUniqueId(), newEnergy);
  }

  private void updateFoodBar(Player player, float maxEnergy) {
    player.setFoodLevel((int) Math.min(20D, 20D * energyMap.get(player.getUniqueId()) / maxEnergy));
  }
}
