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
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class StealthManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Long> stealthedPlayers = new HashMap<>();

  private final int DISABLE_MS = StrifePlugin.getInstance().getSettings()
      .getInt("config.mechanics.sneak.disable-duration");
  private final float BASE_SNEAK_EXP = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.base-sneak-exp");
  private final float SNEAK_EXP_PER_LEVEL = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.sneak-exp-per-level");
  private final float BASE_SNEAK_ATTACK_EXP = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.base-sneak-attack-exp");
  private final float SNEAK_ATTACK_EXP_PER_LEVEL = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.sneak-attack-exp-per-level");
  private final float BASE_STEALTH_PARTICLES = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.base-particles", 2);
  private final float MOVEMENT_PARTICLE_MULT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.movement-particle-penalty", 2);
  private final float SPRINT_PARTICLE_MULT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.sprint-particle-penalty", 2);
  private final float MAX_STEALTH_PARTICLES = (float) StrifePlugin.getInstance().getSettings()
      .getInt("config.mechanics.sneak.max-stealth-particles", 2);
  private final float SNEAK_SKILL_PARTICLE_REDUCTION = (float) StrifePlugin.getInstance()
      .getSettings().getInt("config.mechanics.sneak.stealth-levels-per-removed-particle", 25);

  public StealthManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  /*
  public boolean isSneakAngle(LivingEntity target, Vector direction) {
    if (TargetingUtil.getMobTarget(target) != null) {
      return false;
    }
    Vector entitySightVector = target.getLocation().getDirection();
    float angle = entitySightVector.angle(direction);
    return angle > 0.6;
  }
  */

  public float getSneakActionExp(float enemyLevel, float stealthLevel) {
    if (stealthLevel != 99 && enemyLevel - stealthLevel > 20) {
      return 0;
    }
    float levelPenaltyMult = Math.min(1.0f, (float) Math.pow(enemyLevel / stealthLevel, 1.5));
    return (BASE_SNEAK_EXP + enemyLevel * SNEAK_EXP_PER_LEVEL) * levelPenaltyMult;
  }

  public float getSneakAttackExp(LivingEntity victim, float sneakLevel, boolean finishingBlow) {
    float victimLevel = StatUtil.getMobLevel(victim);
    float levelDiff = victimLevel - sneakLevel;
    float levelPenaltyMult = 1;
    if (levelDiff < 0) {
      levelPenaltyMult = Math.max(0f, 1f - (-levelDiff * 0.1f));
    } else {
      victimLevel = Math.min(victimLevel, sneakLevel + 8);
    }
    float gainedXp = BASE_SNEAK_ATTACK_EXP + victimLevel * SNEAK_ATTACK_EXP_PER_LEVEL;
    gainedXp *= levelPenaltyMult;

    if (finishingBlow) {
      gainedXp *= 2.5F;
    }
    return gainedXp * levelPenaltyMult;
  }

  public boolean isStealthed(Player player) {
    return stealthedPlayers.containsKey(player.getUniqueId());
  }

  public boolean canSneakAttack(Player player) {
    return stealthedPlayers.containsKey(player.getUniqueId())
        && stealthedPlayers.get(player.getUniqueId()) < System.currentTimeMillis();
  }

  public void stealthPlayer(Player player) {
    for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 70, 70, 70)) {
      if (e instanceof Player) {
        plugin.getIndicatorManager()
            .addIndicator(player, (LivingEntity) e, IndicatorStyle.BOUNCE, 6, "<bold>???", 1.0f, 1.0f, 1.0f);
      } else if (e instanceof Mob && ((Mob) e).getTarget() == player) {
        ((Mob) e).setTarget(null);
        plugin.getIndicatorManager()
            .addIndicator(player, (Mob) e, IndicatorStyle.BOUNCE, 6, "<bold>???", 1.0f, 1.0f, 1.0f);
      }
    }
    player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 90, 0.5, 1, 0.5, 0);
    stealthedPlayers.put(player.getUniqueId(), System.currentTimeMillis() + DISABLE_MS);
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.hidePlayer(plugin, player);
    }
  }

  public void unstealthPlayer(Player player) {
    if (!stealthedPlayers.containsKey(player.getUniqueId())) {
      return;
    }
    player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 90, 0.5, 1, 0.5, 0);
    stealthedPlayers.remove(player.getUniqueId());
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.showPlayer(plugin, player);
    }
  }

  public void doStealthParticles(Player player) {
    if (!isStealthed(player)) {
      return;
    }
    player.spawnParticle(Particle.ASH, player.getEyeLocation(), 60, 12, 12, 12, 0);
    player.spawnParticle(Particle.SMOKE_NORMAL, player.getEyeLocation(), 40, 7, 7, 7, 0);
    double particles = StrifePlugin.RNG.nextFloat() * BASE_STEALTH_PARTICLES;
    if (MoveUtil.hasMoved(player)) {
      if (!player.isSneaking()) {
        particles *= MOVEMENT_PARTICLE_MULT;
      }
      if (player.isSprinting()) {
        particles *= SPRINT_PARTICLE_MULT;
      }
    }
    double sneakSkill = PlayerDataUtil.getSkillLevels(player, LifeSkillType.SNEAK, false).getLevelWithBonus();
    particles -= sneakSkill / SNEAK_SKILL_PARTICLE_REDUCTION;
    particles = Math.min(Math.round(particles), MAX_STEALTH_PARTICLES);
    for (int i = 0; i < particles; i++) {
      Location newLoc = player.getLocation().clone()
          .add(-0.7 + StrifePlugin.RNG.nextFloat() * 1.4, StrifePlugin.RNG.nextFloat() * 2, -0.7 + StrifePlugin.RNG.nextFloat() * 1.4);
      newLoc.getWorld().spawnParticle(Particle.SPELL_MOB, newLoc, 0, 89, 98, 98, 1);
    }
  }
}
