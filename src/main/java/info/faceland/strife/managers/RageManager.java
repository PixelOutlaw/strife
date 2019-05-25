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

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.RageData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;

public class RageManager {

  private final AttributeUpdateManager attributeUpdateManager;
  private final Map<UUID, RageData> rageMap = new ConcurrentHashMap<>();
  private static final int RAGE_GRACE_TICKS = 25;

  public RageManager(AttributeUpdateManager attributeUpdateManager) {
    this.attributeUpdateManager = attributeUpdateManager;
  }

  public Map<UUID, RageData> getRageMap() {
    return rageMap;
  }

  public RageData getEntity(LivingEntity entity) {
    return rageMap.get(entity.getUniqueId());
  }

  public void removeEntity(LivingEntity entity) {
    rageMap.remove(entity.getUniqueId());
  }

  public void removeEntity(UUID uuid) {
    rageMap.remove(uuid);
  }

  public double getRage(LivingEntity entity) {
    if (!rageMap.containsKey(entity.getUniqueId())) {
      return 0;
    }
    return rageMap.get(entity.getUniqueId()).getRageStacks();
  }

  public void setRage(AttributedEntity aEntity, double amount) {
    if (!rageMap.containsKey(aEntity.getEntity().getUniqueId())) {
      return;
    }
    rageMap.get(aEntity.getEntity().getUniqueId()).setRageStacks(amount);
    attributeUpdateManager.updateAttackSpeed(aEntity);
  }

  public void addRage(AttributedEntity attributedEntity, double amount) {
    LivingEntity entity = attributedEntity.getEntity();
    if (attributedEntity.getAttribute(StrifeAttribute.MAXIMUM_RAGE) < 1) {
      return;
    }
    if (!rageMap.containsKey(entity.getUniqueId())) {
      rageMap.put(entity.getUniqueId(), new RageData(
          Math.min(amount, attributedEntity.getAttribute(StrifeAttribute.MAXIMUM_RAGE)),
          RAGE_GRACE_TICKS));
      return;
    }

    rageMap.get(entity.getUniqueId()).setRageStacks(
        Math.min(rageMap.get(entity.getUniqueId()).getRageStacks() + amount,
            attributedEntity.getAttribute(StrifeAttribute.MAXIMUM_RAGE)));
    refreshRage(entity);
  }

  public void refreshRage(LivingEntity entity) {
    if (rageMap.containsKey(entity.getUniqueId())) {
      rageMap.get(entity.getUniqueId()).setGraceTicksRemaining(RAGE_GRACE_TICKS);
    }
  }
}
