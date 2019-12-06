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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BarrierManager {

  private static final int BASE_BARRIER_TICKS = 40;
  private final Map<UUID, Float> barrierMap = new ConcurrentHashMap<>();
  private final Map<UUID, Integer> tickMap = new ConcurrentHashMap<>();

  public void createBarrierEntry(StrifeMob strifeMob) {
    if (strifeMob.getEntity() == null || !strifeMob.getEntity().isValid()) {
      return;
    }
    if (strifeMob.getStat(StrifeStat.BARRIER) <= 0.1) {
      updateShieldDisplay(strifeMob);
      return;
    }
    if (barrierMap.containsKey(strifeMob.getEntity().getUniqueId())) {
      updateShieldDisplay(strifeMob);
      return;
    }
    setEntityBarrier(strifeMob.getEntity().getUniqueId(),
        strifeMob.getStat(StrifeStat.BARRIER));
    updateShieldDisplay(strifeMob);
  }

  public boolean isBarrierUp(StrifeMob strifeMob) {
    createBarrierEntry(strifeMob);
    UUID uuid = strifeMob.getEntity().getUniqueId();
    return barrierMap.containsKey(uuid) && barrierMap.get(uuid) > 0;
  }

  public float getCurrentBarrier(StrifeMob strifeMob) {
    createBarrierEntry(strifeMob);
    return Math.min(strifeMob.getStat(StrifeStat.BARRIER),
        barrierMap.getOrDefault(strifeMob.getEntity().getUniqueId(), 0f));
  }

  public boolean hasBarrierEntry(LivingEntity livingEntity) {
    return barrierMap.containsKey(livingEntity.getUniqueId());
  }

  public void setEntityBarrier(UUID uuid, float amount) {
    barrierMap.put(uuid, amount);
  }

  public void updateShieldDisplay(StrifeMob strifeMob) {
    if (!(strifeMob.getEntity() instanceof Player)) {
      return;
    }
    if (!barrierMap.containsKey(strifeMob.getEntity().getUniqueId())) {
      setPlayerArmor((Player) strifeMob.getEntity(), 0);
      return;
    }
    if (strifeMob.getStat(StrifeStat.BARRIER) < 0.1) {
      setPlayerArmor((Player) strifeMob.getEntity(), 0);
      return;
    }
    float current = Math.min(strifeMob.getStat(StrifeStat.BARRIER),
        barrierMap.get(strifeMob.getEntity().getUniqueId()));
    double percent = current / strifeMob.getStat(StrifeStat.BARRIER);
    setPlayerArmor((Player) strifeMob.getEntity(), percent);
  }

  public void removeEntity(LivingEntity entity) {
    barrierMap.remove(entity.getUniqueId());
  }

  public void removeEntity(UUID uuid) {
    barrierMap.remove(uuid);
  }

  public void interruptBarrier(LivingEntity entity, int ticks) {
    if (!barrierMap.containsKey(entity.getUniqueId())) {
      return;
    }
    tickMap.put(entity.getUniqueId(), ticks);
  }

  public void interruptBarrier(LivingEntity entity) {
    interruptBarrier(entity, BASE_BARRIER_TICKS);
  }

  public void tickEntity(UUID uuid) {
    if (!tickMap.containsKey(uuid)) {
      return;
    }
    if (tickMap.get(uuid) == 0) {
      tickMap.remove(uuid);
      return;
    }
    tickMap.put(uuid, tickMap.get(uuid) - 1);
  }

  public float damageBarrier(StrifeMob strifeMob, float amount) {
    interruptBarrier(strifeMob.getEntity());
    if (!isBarrierUp(strifeMob)) {
      return amount;
    }
    LivingEntity entity = strifeMob.getEntity();
    float remainingBarrier = getCurrentBarrier(strifeMob) - amount;
    if (remainingBarrier > 0) {
      setEntityBarrier(entity.getUniqueId(), remainingBarrier);
      updateShieldDisplay(strifeMob);
      return 0;
    }
    setEntityBarrier(entity.getUniqueId(), 0);
    updateShieldDisplay(strifeMob);
    return Math.abs(remainingBarrier);
  }

  public void restoreBarrier(StrifeMob strifeMob, float amount) {
    if (strifeMob.getStat(StrifeStat.BARRIER) < 0.1 || amount < 0.001) {
      return;
    }
    UUID uuid = strifeMob.getEntity().getUniqueId();
    float newBarrierValue = Math.min(barrierMap.get(uuid) + amount, strifeMob.getStat(
        StrifeStat.BARRIER));
    setEntityBarrier(uuid, newBarrierValue);
    updateShieldDisplay(strifeMob);
  }

  public Map<UUID, Integer> getTickMap() {
    return tickMap;
  }

  public Map<UUID, Float> getBarrierMap() {
    return barrierMap;
  }

  private void setPlayerArmor(Player player, double percent) {
    for (AttributeModifier mod : player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()) {
      player.getAttribute(Attribute.GENERIC_ARMOR).removeModifier(mod);
    }
    player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(20 * percent);
  }
}
