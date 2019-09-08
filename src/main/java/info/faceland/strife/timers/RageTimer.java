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
package info.faceland.strife.timers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.BorderEffectUtil;
import info.faceland.strife.util.LogUtil;
import java.util.UUID;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RageTimer extends BukkitRunnable {

  private final StrifeMob mob;
  private final UUID mobUuid;
  private final float tintIntensity;
  private float ticksRemaining;
  private int graceTicks;
  private int invalidTicks = 0;
  private int heartbeat = 0;

  private static int MAX_GRACE_TICKS = 50;

  public RageTimer(StrifeMob mob, float ticksRemaining) {
    this.mob = mob;
    this.mobUuid = mob.getEntity().getUniqueId();
    this.ticksRemaining = ticksRemaining;
    this.graceTicks = MAX_GRACE_TICKS;
    tintIntensity = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.rage-tint-intensity", 1);
    LogUtil.printDebug("New RageTimer created for " + mobUuid);
    runTaskTimer(StrifePlugin.getInstance(), 0L, 4L);
    sendBorder(ticksRemaining / mob.getStat(StrifeStat.MAXIMUM_RAGE));
  }

  @Override
  public void run() {
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      invalidTicks++;
      if (invalidTicks > 50) {
        LogUtil.printDebug("RageTimer cancelled due to invalid entity");
        StrifePlugin.getInstance().getRageManager().clearRage(mobUuid);
      }
      return;
    }

    spawnRageParticles(mob.getEntity(), ticksRemaining);

    graceTicks--;
    if (graceTicks > 0) {
      sendBorder();
      return;
    }

    float lostTicks = 1 + ticksRemaining * 0.12f;
    ticksRemaining -= lostTicks;

    if (mob.getEntity() instanceof Player) {
      MessageUtils.sendActionBar((Player) mob.getEntity(),
          TextUtils.color("&cRage Remaining: " + (int) Math.max(ticksRemaining, 0)));
    }

    if (ticksRemaining <= 0) {
      LogUtil.printDebug("Rage complete, removing");
      StrifePlugin.getInstance().getRageManager().clearRage(mobUuid);
    }
  }

  private void sendBorder() {
    if (tintIntensity < 0.001 || !(mob.getEntity() instanceof Player)) {
      return;
    }
    heartbeat++;
    if (heartbeat < 8) {
      return;
    }
    heartbeat = 1;
    sendBorder(ticksRemaining / mob.getStat(StrifeStat.MAXIMUM_RAGE));
  }

  private void sendBorder(float percent) {
    BorderEffectUtil.sendBorder((Player) mob.getEntity(), percent * tintIntensity, 8000);
  }

  public void bumpRage(float amount) {
    ticksRemaining = Math.min(ticksRemaining + amount, mob.getStat(StrifeStat.MAXIMUM_RAGE));
    sendBorder(ticksRemaining / mob.getStat(StrifeStat.MAXIMUM_RAGE));
    heartbeat = 0;
    graceTicks = MAX_GRACE_TICKS;
  }

  public float getRage() {
    return ticksRemaining;
  }

  private void spawnRageParticles(LivingEntity entity, float rageStacks) {
    entity.getWorld().spawnParticle(
        Particle.VILLAGER_ANGRY,
        entity.getEyeLocation(),
        1 + (int) (rageStacks / 20),
        0.6, 0.6, 0.6
    );
  }
}
