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
package land.face.strife.timers;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.util.List;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.BorderEffectUtil;
import land.face.strife.util.LogUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RageTimer extends BukkitRunnable {

  private final StrifeMob mob;
  private final UUID mobUuid;
  private final float tintIntensity;
  private float rageRemaining;
  private int graceTicks;
  private int invalidTicks = 0;
  private int heartbeat = 0;
  private final double maxTintPercentPerRage = 0.005;
  private final double baseTint = 0.2;

  private static final List<TextComponent> rageLevels = List.of(
      new TextComponent("\uD809\uDC15"),
      new TextComponent("\uD809\uDC16"),
      new TextComponent("\uD809\uDC17"),
      new TextComponent("\uD809\uDC18"),
      new TextComponent("\uD809\uDC19"));

  private static final int MAX_GRACE_TICKS = 50;

  public RageTimer(StrifeMob mob, float rageRemaining) {
    this.mob = mob;
    this.mobUuid = mob.getEntity().getUniqueId();
    this.rageRemaining = rageRemaining;
    this.graceTicks = MAX_GRACE_TICKS;
    tintIntensity = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.rage-tint-intensity", 1);
    LogUtil.printDebug("New RageTimer created for " + mobUuid);
    runTaskTimer(StrifePlugin.getInstance(), 0L, 4L);
    float maxRage = mob.getStat(StrifeStat.MAXIMUM_RAGE);
    double percent = (baseTint + maxRage * maxTintPercentPerRage) * (rageRemaining / maxRage);
    sendBorder((Player) mob.getEntity(), percent, 5000);
    sendRageGui();
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

    float maxRage = mob.getStat(StrifeStat.MAXIMUM_RAGE);
    spawnRageParticles(mob.getEntity(), rageRemaining);

    graceTicks--;
    if (graceTicks > 0) {
      sendBorder();
      return;
    } else if (graceTicks == 0) {
      double percent = (baseTint + maxRage * maxTintPercentPerRage) * (rageRemaining / maxRage);
      sendBorder((Player) mob.getEntity(), percent, 6000);
    }

    float lostTicks = 1 + rageRemaining * 0.12f;
    rageRemaining -= lostTicks;

    if (rageRemaining <= 0) {
      LogUtil.printDebug("Rage complete, removing");
      StrifePlugin.getInstance().getRageManager().clearRage(mobUuid);
    }
    sendRageGui();
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
    float maxRage = mob.getStat(StrifeStat.MAXIMUM_RAGE);
    double percent = (baseTint + maxRage * maxTintPercentPerRage) * (rageRemaining / maxRage);
    sendBorder((Player) mob.getEntity(), percent, 2000);
  }

  public void bumpRage(float amount) {
    float maxRage = mob.getStat(StrifeStat.MAXIMUM_RAGE);
    rageRemaining = Math.max(0, Math.min(rageRemaining + amount, maxRage));
    sendRageGui();
    double percent = (baseTint + maxRage * maxTintPercentPerRage) * (rageRemaining / maxRage);
    sendBorder((Player) mob.getEntity(), percent, 4000);
    heartbeat = 0;
    if (amount >= 0) {
      graceTicks = MAX_GRACE_TICKS;
    }
  }

  private void sendRageGui() {
    if (rageRemaining <= 0) {
      StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(
          new GUIComponent("rage-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
    } else if (mob.getEntity().getType() == EntityType.PLAYER) {
      int rageStage = (int) (4 * rageRemaining / mob.getMaxRage());
      if (mob.getEntity().getType() == EntityType.PLAYER) {
        StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(
            new GUIComponent("rage-bar", rageLevels.get(rageStage), 22, -140, Alignment.CENTER));
      }
    }
  }

  public float getRage() {
    return rageRemaining;
  }

  private static void sendBorder(Player player, double percentage, int delay) {
    BorderEffectUtil.sendBorder(player, percentage , delay);
  }

  private static void spawnRageParticles(LivingEntity entity, float rageStacks) {
    entity.getWorld().spawnParticle(
        Particle.VILLAGER_ANGRY,
        entity.getEyeLocation(),
        1 + (int) (rageStacks / 20),
        0.6, 0.6, 0.6
    );
  }
}
