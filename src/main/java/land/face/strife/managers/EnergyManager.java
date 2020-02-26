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
package land.face.strife.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.Player;

public class EnergyManager {

  private StrifePlugin plugin;

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
    updateFoodBar((Player) mob.getEntity(), mob.getStat(StrifeStat.ENERGY));
  }

  public float getEnergy(StrifeMob mob) {
    return energyMap.getOrDefault(mob.getEntity().getUniqueId(),
        (mob.getStat(StrifeStat.ENERGY) * ((Player) mob.getEntity()).getFoodLevel()) / 20);
  }

  private void setEnergy(StrifeMob mob, float newEnergy) {
    newEnergy = Math.min(newEnergy, mob.getStat(StrifeStat.ENERGY));
    newEnergy = Math.max(0, newEnergy);
    energyMap.put(mob.getEntity().getUniqueId(), newEnergy);
  }

  private void updateFoodBar(Player player, float maxEnergy) {
    player.setFoodLevel((int) Math.min(20D, 20D * energyMap.get(player.getUniqueId()) / maxEnergy));
  }
}
