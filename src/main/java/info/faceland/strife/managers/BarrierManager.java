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

import static info.faceland.strife.attributes.StrifeAttribute.BARRIER;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BarrierManager {

  private static final int BASE_BARRIER_TICKS = 15;
  private final Map<UUID, Double> barrierMap = new HashMap<>();
  private final Map<UUID, Integer> tickMap = new HashMap<>();

  public void createBarrierEntry(AttributedEntity attributedEntity) {
    if (!attributedEntity.getEntity().isValid()) {
      return;
    }
    if (attributedEntity.getAttribute(BARRIER) <= 0.1) {
      if (attributedEntity.getEntity() instanceof Player) {
        setPlayerArmor((Player) attributedEntity.getEntity(), 0);
      }
      return;
    }
    if (barrierMap.containsKey(attributedEntity.getEntity().getUniqueId())) {
      updateShieldDisplay(attributedEntity);
      return;
    }
    setEntityBarrier(attributedEntity.getEntity().getUniqueId(),
        attributedEntity.getAttribute(BARRIER));
    updateShieldDisplay(attributedEntity);
  }

  public boolean isBarrierUp(AttributedEntity attributedEntity) {
    createBarrierEntry(attributedEntity);
    UUID uuid = attributedEntity.getEntity().getUniqueId();
    return barrierMap.containsKey(uuid) && barrierMap.get(uuid) > 0;
  }

  public double getCurrentBarrier(AttributedEntity attributedEntity) {
    createBarrierEntry(attributedEntity);
    return barrierMap.getOrDefault(attributedEntity.getEntity().getUniqueId(), 0D);
  }

  public void setEntityBarrier(UUID uuid, double amount) {
    barrierMap.put(uuid, amount);
  }

  public void updateShieldDisplay(AttributedEntity attributedEntity) {
    if (!(attributedEntity.getEntity() instanceof Player)) {
      return;
    }
    if (!barrierMap.containsKey(attributedEntity.getEntity().getUniqueId())) {
      return;
    }
    setPlayerArmor((Player) attributedEntity.getEntity(), 0);
    if (attributedEntity.getAttribute(BARRIER) <= 0.1) {
      setPlayerArmor((Player) attributedEntity.getEntity(), 0);
      return;
    }
    double percent = barrierMap.get(attributedEntity.getEntity().getUniqueId()) / attributedEntity
        .getAttribute(BARRIER);
    setPlayerArmor((Player) attributedEntity.getEntity(), percent);
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

  public double damageBarrier(AttributedEntity attributedEntity, double amount) {
    interruptBarrier(attributedEntity.getEntity());
    if (!isBarrierUp(attributedEntity)) {
      return amount;
    }
    LivingEntity entity = attributedEntity.getEntity();
    double remainingBarrier = barrierMap.get(entity.getUniqueId()) - amount;
    if (remainingBarrier > 0) {
      setEntityBarrier(entity.getUniqueId(), remainingBarrier);
      updateShieldDisplay(attributedEntity);
      return 0;
    }
    setEntityBarrier(entity.getUniqueId(), 0);
    updateShieldDisplay(attributedEntity);
    return Math.abs(remainingBarrier);
  }

  public void restoreBarrier(AttributedEntity attributedEntity, double amount) {
    if (attributedEntity.getAttribute(BARRIER) == 0) {
      return;
    }
    UUID uuid = attributedEntity.getEntity().getUniqueId();
    LogUtil.printDebug("restoreBarrier: " + PlayerDataUtil.getName(attributedEntity.getEntity()));
    LogUtil.printDebug(" starting barrier: " + barrierMap.get(uuid));
    double newBarrierValue = Math
        .min(barrierMap.get(uuid) + amount, attributedEntity.getAttribute(BARRIER));
    setEntityBarrier(uuid, newBarrierValue);
    LogUtil.printDebug(" ending barrier: " + barrierMap.get(uuid));
    updateShieldDisplay(attributedEntity);
  }

  public Map<UUID, Integer> getTickMap() {
    return tickMap;
  }

  public Map<UUID, Double> getBarrierMap() {
    return barrierMap;
  }

  private void setPlayerArmor(Player player, double percent) {
    player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(20 * percent);
  }
}
