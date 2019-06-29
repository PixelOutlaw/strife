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

import static info.faceland.strife.util.StatUtil.getAttackTime;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.LastAttackTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class AttackSpeedManager {

  private final Map<UUID, LastAttackTracker> lastAttackMap;

  public AttackSpeedManager() {
    this.lastAttackMap = new HashMap<>();
  }

  public void setAttackTime(UUID uuid, long fullAttackMillis) {
    lastAttackMap.get(uuid).setFullAttackMs(fullAttackMillis);
    lastAttackMap.get(uuid).setLastAttackStamp(System.currentTimeMillis());
  }

  public double getAttackMultiplier(AttributedEntity attacker) {
    return getAttackMultiplier(attacker, true);
  }

  public double getAttackMultiplier(AttributedEntity attacker, boolean resetTime) {
    if (!(attacker.getEntity() instanceof Player)) {
      return 1.0;
    }
    if (!lastAttackMap.containsKey(attacker.getEntity().getUniqueId())) {
      lastAttackMap.put(attacker.getEntity().getUniqueId(), new LastAttackTracker(1L, 1L));
    }
    long millisPassed = getMillisPassed(attacker.getEntity().getUniqueId());
    long fullAttackMillis = getFullAttackMillis(attacker.getEntity().getUniqueId());
    if (resetTime) {
      setAttackTime(attacker.getEntity().getUniqueId(), (long) (1000 * getAttackTime(attacker)));
    }
    if (millisPassed > fullAttackMillis) {
      return 1.0;
    }
    return (double) millisPassed / fullAttackMillis;
  }

  private long getFullAttackMillis(UUID uuid) {
    return lastAttackMap.get(uuid).getFullAttackMs();
  }

  private long getMillisPassed(UUID uuid) {
    return System.currentTimeMillis() - lastAttackMap.get(uuid).getLastAttackStamp();
  }
}
