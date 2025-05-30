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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.mount.controller.MountControllerTypes;
import io.papermc.paper.entity.TeleportFlag.Relative;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.announce.ModernAnnouncerPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

public class RecallTimer extends BukkitRunnable {

  private static final long TICK_RATE = 2L;
  private static final int BASE_TIME = (int) ((20 * 10) / TICK_RATE);
  public static Map<UUID, Long> ANTI_ABUSE_COOLDOWN = new HashMap<>();
  public static Map<UUID, Long> COOLDOWNS = new HashMap<>();
  public static Map<UUID, RecallTimer> TASKS = new HashMap<>();

  private StrifeMob mob;
  private UUID uuid;
  private Dummy<?> dummy;
  private ActiveModel activeModel;
  private ModeledEntity modeledEntity;

  private int secondsRemaining = 10;
  private int remainingTime;

  public RecallTimer(StrifeMob mob) {
    this.mob = mob;
    Player player = (Player) mob.getEntity();
    uuid = player.getUniqueId();

    remainingTime = BASE_TIME;
    var animationSpeed = 0.89f;
    if (mob.hasTrait(StrifeTrait.FLIGHTY)) {
      remainingTime /= 2;
      animationSpeed *= 2f;
      secondsRemaining = 5;
    }

    dummy = new Dummy<>();
    dummy.setLocation(player.getLocation().clone().add(0, 0.1, 0));
    modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
    if (modeledEntity == null) {
      dummy.setRemoved(true);
      Bukkit.getLogger().warning("Failed to create recall entity");
    } else {
      activeModel = ModelEngineAPI.createActiveModel("fx_recall");
      modeledEntity.setSaved(false);
      modeledEntity.addModel(activeModel, true);
      dummy.getBodyRotationController().setRotationDuration(0);
      dummy.getBodyRotationController().setRotationDelay(0);
      dummy.setWalking(false);
      dummy.getBodyRotationController().setRotationDuration(0);
      dummy.getBodyRotationController().setRotationDelay(0);
      dummy.setYBodyRot(player.getYaw());
      dummy.setYHeadRot(player.getYaw());
      activeModel.getMountManager().ifPresent(mountManager -> {
        player.setSprinting(false);
        mountManager.setCanDrive(true);
        mountManager.setCanRide(true);
        mountManager.mountDriver(player, MountControllerTypes.WALKING);
      });
      activeModel.setScale(2.1);
      activeModel.getAnimationHandler().forceStopAllAnimations();
      activeModel.getAnimationHandler().playAnimation("spawn", 0, 0, animationSpeed, true);
    }
    runTaskTimer(StrifePlugin.getInstance(), 0L, TICK_RATE);
    TASKS.put(player.getUniqueId(), this);
    ModernAnnouncerPlugin.sendAnnouncement(Set.of(player), "recall", "recall");
  }

  @Override
  public void run() {
    if (!mob.getEntity().isValid() || dummy == null) {
      cancelTask();
      return;
    }
    var player = (Player) mob.getEntity();
    if (!player.isOnline() || player != activeModel.getMountManager().get().getDriver()) {
      cancelTask();
      return;
    }
    var newSecondsRemaining = (remainingTime / 10);
    if (newSecondsRemaining != secondsRemaining) {
      player.sendTitle("", FaceColor.LAVENDER + "Teleporting in " + FaceColor.CYAN +
          (newSecondsRemaining + 1) + FaceColor.LAVENDER + " seconds...", 0, 40, 0);
      secondsRemaining = newSecondsRemaining;
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.5f, 0.5f);
    }
    if (remainingTime == 0) {
      player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 1.5f, 2f);
      player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 30, 2, 2, 2, 0.2);
      activeModel.getMountManager().get().dismountAll();
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "forceescape " + player.getName());
        player.sendTitle("", FaceColor.LAVENDER + "WHOOSH!!", 0, 10,10);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.5f, 2f);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 30, 2, 2, 2, 0.2);
      }, 0L);
      TASKS.remove(uuid);
      COOLDOWNS.put(player.getUniqueId(), System.currentTimeMillis() + 1000 * 60 * 5);
      cancelTask();
    } else {
      player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 2, 2, 2, 2, 0);
    }
    remainingTime--;
  }

  private void cancelTask() {
    if (!isCancelled()) {
      cancel();
    }
    if (activeModel != null) {
      activeModel.getMountManager().get().dismountAll();
      activeModel.getAnimationHandler().playAnimation("death", 0, 0, 1, true);
    }
    if (remainingTime != 0) {
      ((Player) mob.getEntity()).sendTitle("", FaceColor.RED + "Teleport Cancelled!", 0, 20, 10);
      mob.getEntity().teleport(dummy.getLocation(), TeleportCause.PLUGIN, Relative.VELOCITY_ROTATION);
      ModernAnnouncerPlugin.sendAnnouncement(Set.of((Player) mob.getEntity()), "recall", "recall-cancel");
    }
    if (dummy != null) {
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
        dummy.setRemoved(true);
        dummy = null;
      }, 20L);
    }
    mob = null;
    activeModel = null;
    modeledEntity = null;
    TASKS.remove(uuid);
    uuid = null;
  }

  public static void cancelRecall(Player player) {
    var task = TASKS.remove(player.getUniqueId());
    if (task != null) {
      task.cancelTask();
    }
  }
}
