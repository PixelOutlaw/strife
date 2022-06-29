/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.tasks;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.lang.ref.WeakReference;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import land.face.strife.util.LogUtil;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RageTask extends BukkitRunnable {

  private final WeakReference<StrifeMob> parentMob;
  @Getter
  private float rage;
  private int graceTicks;

  @SuppressWarnings("deprecation")
  private static final List<TextComponent> rageLevels = List.of(
      new TextComponent("\uD809\uDC15"),
      new TextComponent("\uD809\uDC16"),
      new TextComponent("\uD809\uDC17"),
      new TextComponent("\uD809\uDC18"),
      new TextComponent("\uD809\uDC19"),
      new TextComponent("\uD809\uDC1A"),
      new TextComponent("\uD809\uDC1B"),
      new TextComponent("\uD809\uDC1C"),
      new TextComponent("\uD809\uDC1D"),
      new TextComponent("\uD809\uDC1E")
  );

  private static final int MAX_GRACE_TICKS = 50;

  public RageTask(StrifeMob mob) {
    this.parentMob = new WeakReference<>(mob);
    this.graceTicks = MAX_GRACE_TICKS;
    runTaskTimer(StrifePlugin.getInstance(), 1L, 4L);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }

    spawnRageParticles(mob.getEntity(), rage);

    graceTicks--;
    if (graceTicks > 0) {
      return;
    }

    float lostTicks = 1 + rage * 0.08f;
    reduceRage(lostTicks);

    if (rage <= 0) {
      LogUtil.printDebug("Rage complete, removing");
      mob.endRageTask();
    }
  }

  public void bumpRage(float amount) {
    StrifeMob mob = parentMob.get();
    float maxRage = mob.getMaxRage();
    rage += amount;
    rage = Math.min(rage, maxRage);
    sendRageGui();
    graceTicks = MAX_GRACE_TICKS;
  }

  public void reduceRage(float amount) {
    rage = Math.max(rage - amount, 0);
    sendRageGui();
  }

  private void sendRageGui() {
    StrifeMob mob = parentMob.get();
    if (mob.getEntity().getType() == EntityType.PLAYER) {
      if (rage <= 0) {
        StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(
            new GUIComponent("rage-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
        return;
      }
      float ratio = rage / mob.getMaxRage();
      int rageStage = (int) Math.floor(9 * ratio);
      StrifePlugin.getInstance().getGuiManager().getGui((Player) mob.getEntity()).update(
          new GUIComponent("rage-bar", rageLevels.get(rageStage), 47, -67, Alignment.CENTER));
    }
  }

  private static void spawnRageParticles(LivingEntity entity, float rageStacks) {
    entity.getWorld().spawnParticle(
        Particle.VILLAGER_ANGRY,
        entity.getEyeLocation(),
        1 + (int) (Math.max(0, rageStacks) / 20),
        0.6, 0.6, 0.6
    );
  }
}
