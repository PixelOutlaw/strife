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

import static land.face.strife.util.UberConfig.BASE_SNEAK_ATTACK_EXP;
import static land.face.strife.util.UberConfig.BASE_SNEAK_EXP;
import static land.face.strife.util.UberConfig.BASE_STEALTH_PARTICLES;
import static land.face.strife.util.UberConfig.MAX_STEALTH_PARTICLES;
import static land.face.strife.util.UberConfig.MOVEMENT_PARTICLE_MULT;
import static land.face.strife.util.UberConfig.SNEAK_ATTACK_EXP_PER_LEVEL;
import static land.face.strife.util.UberConfig.SNEAK_DISABLE_MS;
import static land.face.strife.util.UberConfig.SNEAK_EXP_PER_LEVEL;
import static land.face.strife.util.UberConfig.SNEAK_SKILL_PARTICLE_REDUCTION;
import static land.face.strife.util.UberConfig.SPRINT_PARTICLE_MULT;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StealthData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.EveryTickTask;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StealthManager {

  private final StrifePlugin plugin;
  @Getter
  private final Map<UUID, StealthData> stealthedPlayers = new HashMap<>();

  public static final List<GUIComponent> STEALTH_IND = List.of(
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岓")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岔")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岕")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岖")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岗")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岘")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岙")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岚")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岛")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岜")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岝")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(GuiManager.adventureIsRetarded("岞")), 45, 0, Alignment.CENTER)
  );
  private static final int STEALTH_LEN = STEALTH_IND.size();

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

  public boolean isStealthed(LivingEntity le) {
    if (le instanceof Player) {
      return isStealthed((Player) le);
    }
    return false;
  }

  public boolean canSneakAttack(Player player) {
    return stealthedPlayers.containsKey(player.getUniqueId())
        && stealthedPlayers.get(player.getUniqueId()).getReadyTime() < System.currentTimeMillis();
  }

  public void stealthPlayer(Player player, float seconds) {
    var mob = plugin.getStrifeMobManager().getStatMob(player);
    for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), 70, 70, 70)) {
      if (e instanceof Player) {
        plugin.getIndicatorManager().addIndicator(mob, (LivingEntity) e, IndicatorStyle.BOUNCE,
            6, "<bold>???", 1.0f, 1.0f, 1.0f, true);
      } else if (e instanceof Mob && ((Mob) e).getTarget() == player) {
        ((Mob) e).setTarget(null);
        plugin.getIndicatorManager().addIndicator(mob, (Mob) e, IndicatorStyle.BOUNCE,
            6, "<bold>???", 1.0f, 1.0f, 1.0f, true);
      }
    }
    player.spawnParticle(Particle.SMOKE, player.getLocation(), 90, 0.5, 1, 0.5, 0);

    var rank = SkillRank.getRank(mob, LifeSkillType.SNEAK);
    var skillLevel = PlayerDataUtil.getSkillLevels(mob, LifeSkillType.SNEAK, true);
    int ticks = (int) (seconds * 20f / 3f);
    stealthedPlayers.put(player.getUniqueId(), new StealthData(
        ticks,
        ticks,
        System.currentTimeMillis() + SNEAK_DISABLE_MS,
        rank,
        skillLevel.getLevel(),
        1 + mob.getStat(StrifeStat.STEALTH_BONUS) / 100,
        SkillRank.isRank(rank, SkillRank.MASTER)
    ));
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.hidePlayer(plugin, player);
    }
    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticks * 3, 0, false, false, true));
  }

  public void unstealthPlayer(Player player, boolean fromHit) {
    if (!stealthedPlayers.containsKey(player.getUniqueId())) {
      return;
    }
    if (fromHit) {
      var data = stealthedPlayers.get(player.getUniqueId());
      if (data.isCanBlock()) {
        data.setCanBlock(false);
        return;
      }
    }
    player.removePotionEffect(PotionEffectType.INVISIBILITY);
    player.spawnParticle(Particle.SMOKE, player.getLocation(), 90, 0.5, 1, 0.5, 0);
    stealthedPlayers.remove(player.getUniqueId());
    plugin.getGuiManager().updateComponent(player, EveryTickTask.EMPTY_ATTACK);
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.showPlayer(plugin, player);
    }
  }

  public void doStealthParticles(Player player) {
    if (!isStealthed(player)) {
      return;
    }
    var data = stealthedPlayers.get(player.getUniqueId());

    if (data.getTicks() < 1) {
      unstealthPlayer(player, false);
      return;
    }

    var percent = (float) data.getTicks() / data.getMaxTicks();
    int attackProgress = Math.min((int) Math.floor(percent * STEALTH_LEN), STEALTH_LEN - 1);
    plugin.getGuiManager().updateComponent(player, STEALTH_IND.get(attackProgress));

    data.setTicks(data.getTicks() - 1);

    player.spawnParticle(Particle.SMOKE, player.getEyeLocation(), 120, 12, 12, 12, 0);
    double particles = StrifePlugin.RNG.nextFloat() * BASE_STEALTH_PARTICLES;
    if (MoveUtil.hasMoved(player)) {
      if (!player.isSneaking()) {
        particles *= MOVEMENT_PARTICLE_MULT;
      }
      if (player.isSprinting()) {
        particles *= SPRINT_PARTICLE_MULT;
      }
    }
    if (SkillRank.isRank(data.getInitialRank(), SkillRank.JOURNEYMAN)) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5, 0, true, false));
    } else {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 5, 1, true, false));
    }
    double sneakSkill = SkillRank.isRank(data.getInitialRank(), SkillRank.APPRENTICE) ? data.getInitialLevel() : 1;
    sneakSkill *= data.getInitialStealthBonus();
    particles -= sneakSkill / SNEAK_SKILL_PARTICLE_REDUCTION;
    int intParticles = (int) particles;
    if (intParticles > 0) {
      intParticles = (int) Math.min(particles, MAX_STEALTH_PARTICLES);
      float lightLevel = player.getLocation().getBlock().getLightLevel() / 10f;
      float spread = 0.25f + 0.25f * lightLevel;
      float positionVariance = 3.0f - (1.85f * lightLevel);
      float xPos = (-positionVariance / 2 + StrifePlugin.RNG.nextFloat() * positionVariance);
      float yPos = (StrifePlugin.RNG.nextFloat() * 1.5f);
      float zPos = (-positionVariance / 2 + StrifePlugin.RNG.nextFloat() * positionVariance);
      Location newLoc = player.getLocation().clone().add(xPos, yPos, zPos);
      newLoc.getWorld().spawnParticle(Particle.ENTITY_EFFECT, newLoc, intParticles, spread, spread, spread, Color.GRAY);
    }
  }
}
