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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.util.MoveUtil;
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

  private StrifePlugin plugin;

  private Set<UUID> stealthedPlayers = new HashSet<>();

  private float BASE_SNEAK_EXP = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.base-sneak-exp");
  private float SNEAK_EXP_PER_LEVEL = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.sneak-exp-per-level");
  private float BASE_SNEAK_ATTACK_EXP = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.base-sneak-attack-exp");
  private float SNEAK_ATTACK_EXP_PER_LEVEL = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.sneak-attack-exp-per-level");
  private float BASE_STEALTH_PARTICLES = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.base-particles", 2);
  private float MOVEMENT_PARTICLE_MULT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.movement-particle-penalty", 2);
  private float SPRINT_PARTICLE_MULT = (float) StrifePlugin.getInstance().getSettings()
      .getDouble("config.mechanics.sneak.sprint-particle-penalty", 2);
  private float MAX_STEALTH_PARTICLES = (float) StrifePlugin.getInstance().getSettings()
      .getInt("config.mechanics.sneak.max-stealth-particles", 2);
  private float SNEAK_SKILL_PARTICLE_REDUCTION = (float) StrifePlugin.getInstance().getSettings()
      .getInt("config.mechanics.sneak.stealth-levels-per-removed-particle", 25);

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
    victimLevel = Math.min(sneakLevel + 5, victimLevel);
    float levelPenaltyMult = Math.min(1.0f, (float) Math.pow(victimLevel / sneakLevel, 2));
    float gainedXp = BASE_SNEAK_ATTACK_EXP + victimLevel * SNEAK_ATTACK_EXP_PER_LEVEL;
    if (finishingBlow) {
      gainedXp *= 2;
    }
    return gainedXp * levelPenaltyMult;
  }

  public boolean isStealthed(LivingEntity livingEntity) {
    if (!(livingEntity instanceof Player)) {
      return false;
    }
    return isStealthed((Player) livingEntity);
  }

  public boolean isStealthed(Player player) {
    return stealthedPlayers.contains(player.getUniqueId());
  }

  public void stealthPlayer(Player player) {
    for (Entity e : player.getWorld().getNearbyEntities(
        player.getLocation(), 60, 60, 60, e -> e instanceof Mob)) {
      if (((Mob) e).getTarget() == player) {
        ((Mob) e).setTarget(null);
        plugin.getIndicatorManager().addIndicator(player, (Mob) e, IndicatorStyle.FLOAT_UP_SLOW, 8, "&e&l???");
      }
    }
    player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 90, 0.5, 1, 0.5, 0);
    stealthedPlayers.add(player.getUniqueId());
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.hidePlayer(plugin, player);
    }
  }

  public void unstealthPlayer(Player player) {
    if (!stealthedPlayers.contains(player.getUniqueId())) {
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
    player.spawnParticle(Particle.SMOKE_NORMAL, player.getEyeLocation(), 60, 12, 12, 12, 0);
    double particles = Math.random() * BASE_STEALTH_PARTICLES;
    if (MoveUtil.hasMoved(player)) {
      if (!player.isSneaking()) {
        particles *= MOVEMENT_PARTICLE_MULT;
      }
      if (player.isSprinting()) {
        particles *= SPRINT_PARTICLE_MULT;
      }
    }
    double sneakSkill = PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.SNEAK, false);
    particles -= sneakSkill / SNEAK_SKILL_PARTICLE_REDUCTION;
    particles = Math.min(Math.round(particles), MAX_STEALTH_PARTICLES);
    for (int i = 0; i < particles; i++) {
      Location newLoc = player.getLocation().clone()
          .add(-0.7 + Math.random() * 1.4, Math.random() * 2, -0.7 + Math.random() * 1.4);
      newLoc.getWorld().spawnParticle(Particle.SPELL_MOB, newLoc, 0, 89, 98, 98, 1);
    }
  }
}
