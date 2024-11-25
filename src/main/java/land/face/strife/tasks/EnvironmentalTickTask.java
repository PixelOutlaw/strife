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

import land.face.strife.StrifePlugin;
import land.face.strife.util.DOTUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EnvironmentalTickTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  public EnvironmentalTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
    runTaskTimer(plugin, 5L, 10L);
  }

  @Override
  public void run() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.getGameMode() == GameMode.ADVENTURE) {
        Material mat = p.getLocation().getBlock().getType();
        switch (mat) {
          case LAVA -> DOTUtil.doLava(p);
          case WATER, WATER_CAULDRON -> {
            if (p.hasPotionEffect(PotionEffectType.RAID_OMEN) &&
                p.getPotionEffect(PotionEffectType.RAID_OMEN).getAmplifier() < 1) {
              p.removePotionEffect(PotionEffectType.RAID_OMEN);
            }
          }
        }
      }
    }
  }
}
