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
package info.faceland.strife.managers;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.RageTimer;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;

public class RageManager {

  private final Map<UUID, RageTimer> rageMap = new HashMap<>();

  public void clearRage(UUID uuid) {
    if (rageMap.containsKey(uuid)) {
      LogUtil.printDebug("Cancelled RageTimer - Cleared");
      rageMap.get(uuid).cancel();
      rageMap.remove(uuid);
    }
  }

  public float getRage(LivingEntity entity) {
    if (rageMap.containsKey(entity.getUniqueId())) {
      return rageMap.get(entity.getUniqueId()).getRage();
    }
    return 0;
  }

  public void addRage(StrifeMob mob, float amount) {
    if (rageMap.containsKey(mob.getEntity().getUniqueId())) {
      rageMap.get(mob.getEntity().getUniqueId()).bumpRage(amount);
      return;
    }
    rageMap.put(mob.getEntity().getUniqueId(), new RageTimer(mob, amount));
  }
}
