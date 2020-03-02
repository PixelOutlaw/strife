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

import static land.face.strife.util.DamageUtil.buildMissIndicator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BlockData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.LogUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockManager {

  private final Map<UUID, BlockData> blockDataMap = new HashMap<>();
  private final Random random = new Random();

  private static final ItemStack BLOCK_DATA = new ItemStack(Material.COARSE_DIRT);

  private static final double FLAT_BLOCK_S = 10;
  private static final double PERCENT_BLOCK_S = 0.05;
  private static final long DEFAULT_BLOCK_MILLIS = 10000;
  private static final double MAX_BLOCK_CHANCE = 0.6;

  public boolean isAttackBlocked(StrifeMob attacker, StrifeMob defender, float attackMult,
      AttackType attackType, boolean isBlocking) {
    if (rollBlock(defender, isBlocking)) {
      blockFatigue(defender.getEntity().getUniqueId(), attackMult, isBlocking);
      bumpRunes(defender);
      DamageUtil.doReflectedDamage(defender, attacker, attackType);
      DamageUtil.doBlock(attacker, defender);
      if (attacker.getEntity() instanceof Player) {
        StrifePlugin.getInstance().getIndicatorManager().addIndicator(attacker.getEntity(),
            defender.getEntity(), buildMissIndicator((Player) attacker.getEntity()), "&e&lBlocked!");
      }
      return true;
    }
    return false;
  }

  public boolean rollBlock(StrifeMob strifeMob, boolean isBlocking) {
    if (strifeMob.getStat(StrifeStat.BLOCK) < 1) {
      return false;
    }
    UUID uuid = strifeMob.getEntity().getUniqueId();
    updateStoredBlock(strifeMob);
    double blockChance = Math.min(blockDataMap.get(uuid).getStoredBlock() / 100, MAX_BLOCK_CHANCE);
    if (isBlocking) {
      blockChance *= 2;
    }
    LogUtil.printDebug("Block chance: " + blockChance);
    return random.nextDouble() < blockChance;
  }

  public long getMillisSinceBlock(UUID uuid) {
    return System.currentTimeMillis() - blockDataMap.get(uuid).getLastHit();
  }

  public double getSecondsSinceBlock(UUID uuid) {
    return ((double) getMillisSinceBlock(uuid)) / 1000;
  }

  public int getEarthRunes(UUID uuid) {
    if (!blockDataMap.containsKey(uuid)) {
      return 0;
    }
    return blockDataMap.get(uuid).getRunes();
  }

  public void setEarthRunes(StrifeMob mob, int runes) {
    int maxRunes = Math.round(mob.getStat(StrifeStat.MAX_EARTH_RUNES));
    if (maxRunes == 0) {
      blockDataMap.remove(mob.getEntity().getUniqueId());
      return;
    }
    UUID uuid = mob.getEntity().getUniqueId();
    if (!blockDataMap.containsKey(uuid)) {
      if (maxRunes < 1) {
        return;
      }
      BlockData data = new BlockData(0, mob.getStat(StrifeStat.BLOCK));
      blockDataMap.put(mob.getEntity().getUniqueId(), data);
    }
    blockDataMap.get(uuid).setRunes(Math.max(Math.min(runes, maxRunes), 0));
  }

  public void bumpRunes(StrifeMob mob) {
    if (mob.getStat(StrifeStat.EARTH_DAMAGE) < 1) {
      return;
    }
    int runes = StrifePlugin.getInstance().getBlockManager()
        .getEarthRunes(mob.getEntity().getUniqueId());
    StrifePlugin.getInstance().getBlockManager().setEarthRunes(mob, runes + 1);
  }

  public int consumeEarthRunes(StrifeMob attacker, LivingEntity defender) {
    int runes = getEarthRunes(attacker.getEntity().getUniqueId());
    setEarthRunes(attacker, 0);
    if (runes == 0) {
      return 0;
    }
    defender.getWorld().playSound(defender.getEyeLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);
    defender.getWorld().spawnParticle(
        Particle.ITEM_CRACK,
        defender.getLocation().clone().add(0, defender.getEyeHeight() / 2, 0),
        20 + 20 * runes, 0, 0, 0, 0.2f,
        BLOCK_DATA
    );
    return runes;
  }

  private void updateStoredBlock(StrifeMob strifeMob) {
    UUID uuid = strifeMob.getEntity().getUniqueId();
    if (blockDataMap.get(uuid) == null) {
      BlockData data = new BlockData(System.currentTimeMillis() - DEFAULT_BLOCK_MILLIS, 0);
      blockDataMap.put(uuid, data);
    }
    double maximumBlock = strifeMob.getStat(StrifeStat.BLOCK);
    double block = blockDataMap.get(uuid).getStoredBlock();
    double restoredBlock = FLAT_BLOCK_S + PERCENT_BLOCK_S * maximumBlock;
    if (strifeMob.getStat(StrifeStat.BLOCK_RECOVERY) > 1) {
      restoredBlock *= 1 + strifeMob.getStat(StrifeStat.BLOCK_RECOVERY) / 100;
    }

    block += restoredBlock * getSecondsSinceBlock(uuid);
    blockDataMap.get(uuid).setStoredBlock(Math.min(block, maximumBlock));
    blockDataMap.get(uuid).setLastHit(System.currentTimeMillis());
    LogUtil.printDebug("New block before clamp: " + block);
  }

  public void blockFatigue(UUID uuid, double attackMultipler, boolean isBlocking) {
    BlockData data = blockDataMap.get(uuid);
    LogUtil.printDebug("Pre reduction block: " + data.getStoredBlock());
    double blockFatigue = attackMultipler * (isBlocking ? 50D : 100D);
    data.setStoredBlock(Math.max(0, data.getStoredBlock() - blockFatigue));
    LogUtil.printDebug("Post reduction block: " + data.getStoredBlock());
  }
}
