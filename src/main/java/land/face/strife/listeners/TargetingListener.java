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
package land.face.strife.listeners;

import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER;
import static org.bukkit.potion.PotionEffectType.BLINDNESS;
import static org.bukkit.potion.PotionEffectType.INVISIBILITY;

import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.util.Vector;

public class TargetingListener implements Listener {

  private final StrifePlugin plugin;
  private final Random random;

  private final float DETECTION_THRESHOLD;
  private final float BASE_AWARENESS_UNSEEN;
  private final float BASE_AWARENESS_SEEN;
  private final float AWARENESS_PER_LV_UNSEEN;
  private final float AWARENESS_PER_LV_SEEN;
  private final float SEEN_MAX_ANGLE;
  private final float MAX_EXP_RANGE_SQUARED;
  private final int SNEAK_EFFECTIVENESS;

  private static final float MAX_DIST_SQUARED = 1500;

  public TargetingListener(StrifePlugin plugin) {
    this.plugin = plugin;
    this.random = new Random();

    DETECTION_THRESHOLD = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.detection-threshold");
    BASE_AWARENESS_UNSEEN = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.base-detection-when-unseen");
    BASE_AWARENESS_SEEN = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.base-detection-when-seen");
    AWARENESS_PER_LV_UNSEEN = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.per-lvl-detection-when-unseen");
    AWARENESS_PER_LV_SEEN = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.per-lvl-detection-when-seen");
    SEEN_MAX_ANGLE = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.maximum-head-angle-for-seen");
    MAX_EXP_RANGE_SQUARED = (float) plugin.getSettings()
        .getDouble("config.mechanics.sneak.maximum-sneak-exp-range-squared");
    SNEAK_EFFECTIVENESS = plugin.getSettings()
        .getInt("config.mechanics.sneak.sneak-skill-effectiveness");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void modifyAttackRange(EntityDamageByEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof Mob)) {
      return;
    }
    LivingEntity attacker = DamageUtil.getAttacker(event.getDamager());
    TargetingUtil.expandMobRange(attacker, (Mob) event.getEntity());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onIgnoreHighLevelPlayers(EntityTargetLivingEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getTarget() instanceof Player) || !(event.getEntity() instanceof Mob)
        || event.getReason() != CLOSEST_PLAYER) {
      return;
    }
    if (plugin.getStealthManager().isStealthed(event.getTarget())) {
      return;
    }
    int playerLevel = StatUtil.getMobLevel(event.getTarget());
    int mobLevel = StatUtil.getMobLevel((Mob) event.getEntity());

    if (playerLevel - mobLevel > 20) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onNormalTarget(EntityTargetLivingEntityEvent event) {
    if (event.isCancelled() || !(event.getTarget() instanceof Player) || !(event
        .getEntity() instanceof Mob) || event.getReason() != CLOSEST_PLAYER || SpecialStatusUtil
        .isSneakImmune(event.getEntity())) {
      return;
    }
    Player player = (Player) event.getTarget();
    if (!plugin.getStealthManager().isStealthed(player)) {
      return;
    }

    Mob creature = (Mob) event.getEntity();
    float level = StatUtil.getMobLevel(creature);

    LogUtil.printDebug("Sneak calc for " + player.getName() + " from lvl " + level + " " +
        creature.getType());

    Location playerLoc = player.getLocation();
    Location entityLoc = creature.getLocation();
    Vector playerDifferenceVector = playerLoc.toVector().subtract(entityLoc.toVector());
    Vector entitySightVector = entityLoc.getDirection();

    Champion champion = plugin.getChampionManager().getChampion(player);
    float angle = entitySightVector.angle(playerDifferenceVector);
    float stealthLevel = champion.getLifeSkillLevel(LifeSkillType.SNEAK);
    float stealthSkill = champion.getEffectiveLifeSkillLevel(LifeSkillType.SNEAK, false);
    double distSquared = Math.min(MAX_DIST_SQUARED, entityLoc.distanceSquared(playerLoc));
    float distanceMult = (MAX_DIST_SQUARED - (float) distSquared) / MAX_DIST_SQUARED;
    float lightMult = (float) Math.max(0.15,
        (1D + 0.2 * (playerLoc.getBlock().getLightLevel() - entityLoc.getBlock().getLightLevel())));

    if (player.hasPotionEffect(INVISIBILITY)) {
      stealthSkill += 5 + stealthSkill * 0.1;
    }
    if (!player.isSneaking()) {
      stealthSkill *= 0.75;
    }
    if (player.isSprinting()) {
      stealthSkill *= 0.5;
    }

    float awareness;
    if (angle > SEEN_MAX_ANGLE || creature.hasPotionEffect(BLINDNESS)) {
      awareness = BASE_AWARENESS_UNSEEN + level * AWARENESS_PER_LV_UNSEEN;
    } else {
      awareness = BASE_AWARENESS_SEEN + level * AWARENESS_PER_LV_SEEN;
    }
    awareness *= distanceMult;
    awareness *= lightMult;
    awareness -= stealthSkill * SNEAK_EFFECTIVENESS;

    LogUtil.printDebug(" DIST MULT: " + distanceMult);
    LogUtil.printDebug(" LIGHT MULT: " + lightMult);
    LogUtil.printDebug(" ANGLE: " + angle);
    LogUtil.printDebug(" AWARENESS: " + awareness);

    if (random.nextDouble() > awareness / DETECTION_THRESHOLD) {
      event.setCancelled(true);
      LogUtil.printDebug(" SNEAK-SUCCESS: TRUE");
      if (distSquared <= MAX_EXP_RANGE_SQUARED) {
        float xp = plugin.getStealthManager().getSneakActionExp(level, stealthLevel);
        xp *= distanceMult;
        plugin.getSkillExperienceManager().addExperience(champion, LifeSkillType.SNEAK, xp, false);
        LogUtil.printDebug(" XP-AWARDED: " + xp);
      }
    } else {
      LogUtil.printDebug(" SNEAK-SUCCESS: FALSE");
    }
  }
}
