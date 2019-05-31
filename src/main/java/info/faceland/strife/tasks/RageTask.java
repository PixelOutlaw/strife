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
package info.faceland.strife.tasks;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.managers.AttributedEntityManager;
import info.faceland.strife.data.RageData;
import info.faceland.strife.managers.RageManager;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RageTask extends BukkitRunnable {

  private final RageManager rageManager;
  private final AttributedEntityManager attributedEntityManager;

  public RageTask(RageManager rageManager, AttributedEntityManager attributedEntityManager) {
    this.rageManager = rageManager;
    this.attributedEntityManager = attributedEntityManager;
  }

  @Override
  public void run() {
    for (UUID uuid : rageManager.getRageMap().keySet()) {
      LivingEntity entity = (LivingEntity) Bukkit.getEntity(uuid);

      if (entity == null || !entity.isValid()) {
        rageManager.removeEntity(uuid);
        continue;
      }

      RageData data = rageManager.getEntity(entity);

      if (data.getRageStacks() >= 5) {
        spawnRageParticles(entity, data.getRageStacks());
      }
      if (data.getGraceTicksRemaining() > 0) {
        data.setGraceTicksRemaining(data.getGraceTicksRemaining() - 1);
        continue;
      }

      rageManager.setRage(attributedEntityManager.getAttributedEntity(entity), data.getRageStacks() - 5);
      String msg = TextUtils.color("&cRage Remaining: " + (int) Math.max(data.getRageStacks(), 0));
      MessageUtils.sendActionBar((Player) entity, msg);

      if (data.getRageStacks() <= 0) {
        rageManager.removeEntity(entity);
      }
    }
  }

  private void spawnRageParticles(LivingEntity entity, double rageStacks) {
    entity.getWorld().spawnParticle(
        Particle.VILLAGER_ANGRY,
        entity.getEyeLocation(),
        1 + (int) (rageStacks / 20),
        0.6, 0.6, 0.6
    );
  }

}
