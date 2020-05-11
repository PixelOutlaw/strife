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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class StrifeMobTracker extends BukkitRunnable {

  private final StrifePlugin plugin;

  public StrifeMobTracker(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    int size = plugin.getStrifeMobManager().getMobs().size();
    LogUtil.printInfo("Current StrifeMobs: " + size);
    int valid = 0;
    int random = 0;
    Map<String, Integer> unique = new HashMap<>();
    for (LivingEntity le : plugin.getStrifeMobManager().getMobs().keySet()) {
      if (le.isValid()) {
        valid += 1;
      } else {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
          if (plugin.getStrifeMobManager().isTrackedEntity(le)) {
            plugin.getStrifeMobManager().getMobs().remove(le);
          }
        }, 200L);
      }
      StrifeMob mob = plugin.getStrifeMobManager().getMobs().get(le);
      if (StringUtils.isNotBlank(mob.getUniqueEntityId())) {
        unique.put(mob.getUniqueEntityId(), unique.getOrDefault(mob.getUniqueEntityId(), 0) + 1);
      } else {
        random += 1;
      }
    }
    LogUtil.printDebug("-- Expired entity keys: " + (size - valid));
    LogUtil.printDebug("-- Randomized mobs: " + random);
    LogUtil.printDebug("-- Unique Map:");
    for (String uniqueId : unique.keySet()) {
      LogUtil.printDebug("---- Unique:" + uniqueId + " amount:" + unique.get(uniqueId));
    }
  }
}
