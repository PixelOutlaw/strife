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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.BlockEvent;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.BlockTimer;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.LogUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BlockManager {

  private final StrifePlugin plugin;

  private final Map<UUID, BlockTimer> blockTimers = new HashMap<>();

  private static final double MAX_BLOCK_CHANCE = 0.55;
  public static final int BLOCK_TICK = 4;
  private static final float BLOCK_TICK_MULT = (float) BLOCK_TICK / 20;

  private final float FLAT_BLOCK_S;
  private final float PERCENT_BLOCK_S;
  private final float PERCENT_MAX_BLOCK_MIN;
  private final float MELEE_FATIGUE;
  private final float PROJECTILE_FATIGUE;
  private final float PHYSICAL_BLOCK_FATIGUE_MULT;
  private final float PHYSICAL_BLOCK_CHANCE_MULT;
  private final float GUARD_BREAK_POWER;

  public BlockManager(StrifePlugin plugin) {
    this.plugin = plugin;
    FLAT_BLOCK_S = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.flat-block-recovery", 10);
    PERCENT_BLOCK_S = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.percent-block-recovery", 0.01f);
    PERCENT_MAX_BLOCK_MIN = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.negative-block-percent", 0.25);
    MELEE_FATIGUE = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.melee-fatigue", 70);
    PROJECTILE_FATIGUE = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.projectile-fatigue", 45);
    PHYSICAL_BLOCK_FATIGUE_MULT = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.physical-block-fatigue-mult", 0.75);
    PHYSICAL_BLOCK_CHANCE_MULT = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.physical-block-chance-mult", 4.0);
    GUARD_BREAK_POWER = (float) plugin.getSettings()
        .getDouble("config.mechanics.block.guard-break-multiplier", 1.5f);
  }

  public boolean attemptBlock(StrifeMob attacker, StrifeMob defender, float attackMult,
      AttackType attackType, boolean isBlocking, boolean guardBreak) {
    if (rollBlock(defender, attackMult, isBlocking, attackType == AttackType.PROJECTILE, guardBreak)) {
      BlockEvent ev = new BlockEvent(defender, attacker);
      Bukkit.getPluginManager().callEvent(ev);
      if (attacker.getEntity() instanceof Player) {
        plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
            IndicatorStyle.FLOAT_UP_MEDIUM, 4, "彠", 1.0f, 1.0f, 1.0f);
      }
      if (!blockTimers.containsKey(defender.getEntity().getUniqueId())) {
        blockTimers.put(defender.getEntity().getUniqueId(), new BlockTimer(this, defender));
      }
      DamageUtil.doWhenHit(attacker, defender, attackType);
      return true;
    }
    return false;
  }

  public void setGraceTicks(UUID uuid) {
    if (blockTimers.containsKey(uuid)) {
      blockTimers.get(uuid).setGraceTicks(3);
    }
  }

  public void tickBlock(StrifeMob mob) {
    float blockGain = FLAT_BLOCK_S + PERCENT_BLOCK_S * mob.getMaxBlock();
    mob.setBlock(mob.getBlock() + (BLOCK_TICK_MULT * blockGain));
  }

  public void endTasks() {
    for (BlockTimer timer : blockTimers.values()) {
      timer.cancel();
    }
    blockTimers.clear();
  }

  public void clearBlock(LivingEntity entity) {
    clearBlock(entity.getUniqueId());
  }

  public void clearBlock(UUID uuid) {
    if (blockTimers.containsKey(uuid)) {
      LogUtil.printDebug("Cancelled Block - Cleared");
      blockTimers.get(uuid).cancel();
      blockTimers.remove(uuid);
    }
  }

  public boolean rollBlock(StrifeMob mob, float attackPower, boolean physicallyBlocked,
      boolean projectile, boolean guardBreak) {

    if (StatUtil.getStat(mob, StrifeStat.BLOCK) < 1) {
      return false;
    }

    double blockChance = Math.min(mob.getBlock() / 100, MAX_BLOCK_CHANCE);
    if (physicallyBlocked) {
      blockChance *= PHYSICAL_BLOCK_CHANCE_MULT;
    }

    if (StrifePlugin.RNG.nextDouble() > blockChance) {
      return false;
    }

    float fatigue = projectile ? PROJECTILE_FATIGUE : MELEE_FATIGUE;
    if (physicallyBlocked) {
      fatigue *= PHYSICAL_BLOCK_FATIGUE_MULT;
    }
    fatigue *= attackPower;
    if (guardBreak) {
      fatigue *= GUARD_BREAK_POWER;
    }
    mob.setBlock(mob.getBlock() - fatigue);

    if (mob.getBlock() <= 0 && guardBreak) {
      mob.setBlock(-mob.getMaxBlock() * PERCENT_MAX_BLOCK_MIN);
      mob.getEntity().getWorld().playSound(mob.getEntity().getEyeLocation(), Sound.ITEM_SHIELD_BREAK, 1f, 1f);
      return false;
    } else {
      mob.setBlock(Math.max(0.1f, mob.getBlock()));
      mob.getEntity().getWorld().playSound(mob.getEntity().getEyeLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
      return true;
    }
  }
}
