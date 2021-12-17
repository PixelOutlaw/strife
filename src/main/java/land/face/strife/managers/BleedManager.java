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
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.timers.BleedTimer;
import land.face.strife.util.LogUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class BleedManager {

  private final StrifePlugin plugin;

  private Map<UUID, BleedTimer> bleedMap = new ConcurrentHashMap<>();
  private static final ItemStack BLOCK_DATA = new ItemStack(Material.REDSTONE);

  public BleedManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public boolean isBleeding(LivingEntity livingEntity) {
    return getBleedOnEntity(livingEntity) != 0;
  }

  public float getBleedOnEntity(LivingEntity entity) {
    if (bleedMap.containsKey(entity.getUniqueId())) {
      return bleedMap.get(entity.getUniqueId()).getBleed();
    }
    return 0;
  }

  public boolean addBleed(StrifeMob mob, float amount, boolean bypassBarrier) {
    if (!mob.getEntity().isValid()) {
      return false;
    }
    if (!bypassBarrier && mob.getBarrier() > 0) {
      return false;
    }
    mob.getEntity().getWorld().spawnParticle(
        Particle.ITEM_CRACK,
        mob.getEntity().getEyeLocation().clone()
            .add(0, -mob.getEntity().getEyeHeight() / 2, 0),
        10,
        0.0, 0.0, 0.0,
        0.25,
        BLOCK_DATA
    );
    if (bleedMap.containsKey(mob.getEntity().getUniqueId())) {
      bleedMap.get(mob.getEntity().getUniqueId()).bumpBleed(amount);
      return true;
    }
    bleedMap.put(mob.getEntity().getUniqueId(), new BleedTimer(plugin, mob.getEntity(), amount));
    return true;
  }

  public void spawnBleedParticles(LivingEntity entity, double damage) {
    int particleAmount = Math.min(2 + (int) (damage * 10), 40);
    entity.getWorld().spawnParticle(
        Particle.ITEM_CRACK,
        entity.getEyeLocation().clone().add(0, -entity.getEyeHeight() / 2, 0),
        particleAmount,
        0.0, 0.0, 0.0,
        0.1,
        BLOCK_DATA
    );
  }

  public void dealDamage(LivingEntity livingEntity, double amount) {
    if (livingEntity.getHealth() > amount) {
      livingEntity.setHealth(livingEntity.getHealth() - amount);
      return;
    }
    livingEntity.damage(100000);
  }

  public void endBleedTasks() {
    for (BleedTimer timer : bleedMap.values()) {
      timer.cancel();
    }
    bleedMap.clear();
  }

  public void clearBleed(LivingEntity entity) {
    clearBleed(entity.getUniqueId());
  }

  public void clearBleed(UUID uuid) {
    if (bleedMap.containsKey(uuid)) {
      LogUtil.printDebug("Cancelled BleedTimer - Cleared");
      bleedMap.get(uuid).cancel();
      bleedMap.remove(uuid);
    }
  }
}
