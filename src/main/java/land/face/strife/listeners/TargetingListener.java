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
package land.face.strife.listeners;

import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER;
import static org.bukkit.event.entity.EntityTargetEvent.TargetReason.CUSTOM;
import static org.bukkit.potion.PotionEffectType.BLINDNESS;
import static org.bukkit.potion.PotionEffectType.INVISIBILITY;
import static org.bukkit.potion.PotionEffectType.registerPotionEffectType;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.ProjectileUtil.IgnoreState;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
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

  @EventHandler(priority = EventPriority.LOW)
  public void onProjectileHitFriendly(ProjectileHitEvent event) {
    if (event.isCancelled() || event.getHitEntity() == null) {
      return;
    }
    if (!(event.getHitEntity() instanceof LivingEntity)) {
      return;
    }
    if (event.getHitEntity() == event.getEntity().getShooter()) {
      return;
    }
    if (event.getHitEntity() instanceof ArmorStand && event.getHitEntity().isInvulnerable()) {
      event.setCancelled(true);
      return;
    }
    IgnoreState state = ProjectileUtil.getIgnoreStatus(event.getEntity(), event.getHitEntity().getEntityId());
    switch (state) {
      case IGNORED -> event.setCancelled(true);
      case NONE -> {
        boolean isFriendly = TargetingUtil.isFriendly(plugin.getStrifeMobManager().getStatMob(
            (LivingEntity) event.getEntity().getShooter()), plugin.getStrifeMobManager().getStatMob(
            (LivingEntity) event.getHitEntity()));
        int entityId = event.getHitEntity().getEntityId();
        ProjectileUtil.addIgnoredId(event.getEntity(), isFriendly ? entityId : -entityId);
        if (isFriendly) {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onFishHookInvulnerable(ProjectileHitEvent event) {
    if (event.getEntity() instanceof FishHook && event.getHitEntity() != null &&
        event.getHitEntity().isInvulnerable()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onNoTarget(EntityTargetLivingEntityEvent event) {
    String uniqueId = SpecialStatusUtil.getUniqueId(event.getEntity());
    if (StringUtils.isEmpty(uniqueId)) {
      return;
    }
    if (!plugin.getUniqueEntityManager().getUnique(uniqueId).isCanTarget()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onWeakAggro(EntityTargetLivingEntityEvent event) {
    if (event.isCancelled() || event.getTarget() == null) {
      return;
    }
    if (event.getReason() == CLOSEST_PLAYER || event.getReason() == CUSTOM) {
      return;
    }
    if (SpecialStatusUtil.isWeakAggro(event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void ignoreGuildAllies(EntityTargetLivingEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity) || !(event.getTarget() instanceof Player)) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob((LivingEntity) event.getEntity());
    if (DamageUtil.isGuildAlly(mob, (Player) event.getTarget())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onTargetHighLevel(EntityTargetLivingEntityEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getTarget() instanceof Player && event.getReason() == CLOSEST_PLAYER) {
      if (SpecialStatusUtil.isGuildMob(event.getEntity())) {
        return;
      }
      if (plugin.getStealthManager().isStealthed((Player) event.getTarget())) {
        return;
      }
      int playerLevel = StatUtil.getMobLevel(event.getTarget());
      int mobLevel = StatUtil.getMobLevel((Mob) event.getEntity());
      if (playerLevel - mobLevel > 20) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSneakTarget(EntityTargetLivingEntityEvent event) {
    if (event.getTarget() == null || event.getTarget().getType() != EntityType.PLAYER) {
      return;
    }
    if (event.getReason() == CLOSEST_PLAYER && event.getEntity() instanceof LivingEntity mob) {
      Player player = (Player) event.getTarget();
      if (!plugin.getStealthManager().isStealthed(player)) {
        return;
      }
      float mobLevel = StatUtil.getMobLevel(mob);

      LogUtil.printDebug("Sneak calc for " + player.getName() +
          " from lvl " + mobLevel + " " + mob.getType());

      Location playerLoc = player.getLocation();
      Location entityLoc = mob.getLocation();
      Vector playerDifferenceVector = playerLoc.toVector().subtract(entityLoc.toVector());
      Vector entitySightVector = entityLoc.getDirection();

      Champion champion = plugin.getChampionManager().getChampion(player);
      float angle = entitySightVector.angle(playerDifferenceVector);
      float stealthLevel = champion.getLifeSkillLevel(LifeSkillType.SNEAK);
      float stealthSkill = champion.getEffectiveLifeSkillLevel(LifeSkillType.SNEAK, false);
      double distSquared = Math.min(MAX_DIST_SQUARED, entityLoc.distanceSquared(playerLoc));
      float distanceMult = (MAX_DIST_SQUARED - (float) distSquared) / MAX_DIST_SQUARED;
      float lightMult = (float) Math.max(0.15,
          (1D + 0.2 * (playerLoc.getBlock().getLightLevel() - entityLoc.getBlock()
              .getLightLevel())));

      stealthSkill = Math.max(stealthSkill, 10);

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
      if (angle > SEEN_MAX_ANGLE || mob.hasPotionEffect(BLINDNESS)) {
        awareness = BASE_AWARENESS_UNSEEN + mobLevel * AWARENESS_PER_LV_UNSEEN;
      } else {
        awareness = BASE_AWARENESS_SEEN + mobLevel * AWARENESS_PER_LV_SEEN;
      }
      awareness *= distanceMult;
      awareness *= lightMult;
      awareness -= stealthSkill * SNEAK_EFFECTIVENESS;

      if (random.nextDouble() > awareness / DETECTION_THRESHOLD) {
        event.setCancelled(true);
        if (distSquared <= MAX_EXP_RANGE_SQUARED) {
          float difficultyLevel = Math.min(stealthLevel + 10, mobLevel);
          float xp = plugin.getStealthManager().getSneakActionExp(difficultyLevel, stealthLevel);
          xp *= distanceMult;
          plugin.getSkillExperienceManager().addExperience(player,
              LifeSkillType.SNEAK, xp, false, false);
        }
      } else {
        event.setCancelled(false);
      }
    }
  }
}
