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
package info.faceland.strife.listeners;

import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData.LifeSkillType;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.StatUtil;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.util.Vector;

public class TargetingListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  private static final float DETECT_AWARENESS = 10000;
  private static final float BASE_AWARENESS = 500;
  private static final float BASE_VISIBLE_AWARENESS = 1500;
  private static final float AWARENESS_PER_LEVEL = 30;
  private static final float AWARENESS_VISIBLE_PER_LEVEL = 80;
  private static final float VISION_DETECTION_MAX_ANGLE = 0.3f;
  private static final float MAX_DIST_SQUARED = 1500;
  private static final float MAX_SNEAK_DIST_SQUARED = 144;
  private static final int SNEAK_SKILL_EFFECTIVENESS = 75;

  public TargetingListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random();
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onBossRetarget(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Creature)) {
      return;
    }
    if (!(e.getDamager() instanceof LivingEntity || e.getDamager() instanceof Projectile)) {
      return;
    }
    if (!plugin.getUniqueEntityManager().isUnique((LivingEntity) e.getEntity())) {
      return;
    }
    if (random.nextDouble() > 0.75) {
      if (e.getDamager() instanceof Projectile) {
        ((Creature) e.getEntity())
            .setTarget((LivingEntity) ((Projectile) e.getDamager()).getShooter());
      } else {
        ((Creature) e.getEntity()).setTarget((LivingEntity) e.getDamager());
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onNormalTarget(EntityTargetLivingEntityEvent event) {
    if (event.isCancelled() || !(event.getTarget() instanceof Player) || !(event
        .getEntity() instanceof Creature) || event.getReason() != CLOSEST_PLAYER || event
        .getEntity().hasMetadata("BOSS")) {
      return;
    }
    if (plugin.getSneakManager().isUnstealthed(event.getTarget().getUniqueId())) {
      return;
    }
    Player player = (Player) event.getTarget();
    if (!player.isSneaking()) {
      return;
    }

    Champion champion = plugin.getChampionManager().getChampion(player);
    float level = StatUtil.getMobLevel((LivingEntity) event.getEntity());

    LogUtil.printDebug("Sneak calc for " + player.getName() + " from lvl " + level + " " +
        event.getEntity().getType());

    Location playerLoc = event.getTarget().getLocation();
    Location entityLoc = event.getEntity().getLocation();
    Vector playerDifferenceVector = playerLoc.toVector().subtract(entityLoc.toVector());
    Vector entitySightVector = entityLoc.getDirection();

    float angle = entitySightVector.angle(playerDifferenceVector);
    float sneakSkill = champion.getSneakSkill(true);
    double distSquared = Math.min(MAX_DIST_SQUARED,
        event.getEntity().getLocation().distanceSquared(event.getTarget().getLocation()));
    float distanceMult = (MAX_DIST_SQUARED - (float) distSquared) / MAX_DIST_SQUARED;
    float lightMult = 1.0f - (float) Math.min(0.85,
        0.2f * playerLoc.getBlock().getLightLevel() - entityLoc.getBlock().getLightLevel());

    float awareness;
    if (angle > VISION_DETECTION_MAX_ANGLE) {
      awareness = BASE_AWARENESS + level * AWARENESS_PER_LEVEL;
    } else {
      awareness = BASE_VISIBLE_AWARENESS + level * AWARENESS_VISIBLE_PER_LEVEL;
    }
    awareness *= distanceMult;
    awareness *= lightMult;
    awareness -= sneakSkill * SNEAK_SKILL_EFFECTIVENESS;

    LogUtil.printDebug(" DIST MULT: " + distanceMult);
    LogUtil.printDebug(" LIGHT MULT: " + lightMult);
    LogUtil.printDebug(" ANGLE: " + angle);
    LogUtil.printDebug(" AWARENESS: " + awareness);

    if (random.nextDouble() > Math.max(0, awareness) / DETECT_AWARENESS) {
      event.setCancelled(true);
      LogUtil.printDebug(" SNEAK-SUCCESS: TRUE");
      if (distSquared <= MAX_DIST_SQUARED) {
        float xp = plugin.getSneakManager().getSneakActionExp(level, sneakSkill, distanceMult);
        plugin.getSkillExperienceManager().addExperience(champion, LifeSkillType.SNEAK, xp, false);
        LogUtil.printDebug(" XP-AWARDED: " + xp);
      }
    } else {
      LogUtil.printDebug(" SNEAK-SUCCESS: FALSE");
    }
  }
}
