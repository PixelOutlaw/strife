/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.tealcube.minecraft.bukkit.facecore.utilities.AdvancedActionBarUtil;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BlockData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.LogUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockManager {

  private final Map<LivingEntity, BlockData> blockDataMap = new WeakHashMap<>();
  private final Random random = new Random();

  private static final ItemStack BLOCK_DATA = new ItemStack(Material.COARSE_DIRT);

  private static final double FLAT_BLOCK_S = 10;
  private static final double PERCENT_BLOCK_S = 0.05;
  private static final long DEFAULT_BLOCK_MILLIS = 10000;
  private static final double MAX_BLOCK_CHANCE = 0.6;

  public void tickHolograms() {

    Iterator<LivingEntity> iterator = blockDataMap.keySet().iterator();

    while (iterator.hasNext()) {
      LivingEntity le = iterator.next();

      if (le == null) {
        iterator.remove();
        continue;
      }

      BlockData blockData = blockDataMap.get(le);

      if (!le.isValid()) {
        for (Hologram holo : blockData.getRuneHolograms()) {
          holo.delete();
        }
        iterator.remove();
        continue;
      }

      if (blockData.getRunes() < blockData.getRuneHolograms().size()) {
        while (blockData.getRunes() < blockData.getRuneHolograms().size()) {
          Hologram hologram = getRandomFromCollection(blockData.getRuneHolograms());
          le.getWorld().playSound(hologram.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);
          le.getWorld().spawnParticle(
              Particle.ITEM_CRACK,
              hologram.getLocation(),
              20, 0, 0, 0, 0.07f,
              BLOCK_DATA
          );
          blockData.getRuneHolograms().remove(hologram);
          hologram.delete();
        }
      } else {
        while (blockData.getRunes() > blockData.getRuneHolograms().size() && blockData.getRuneHolograms().size() < 6) {
          Hologram hologram = HologramsAPI.createHologram(StrifePlugin.getInstance(), le.getEyeLocation());
          hologram.appendItemLine(new ItemStack(Material.COARSE_DIRT));
          blockData.getRuneHolograms().add(hologram);
        }
      }
      orbitRunes(le.getLocation().clone().add(0, 1, 0), blockData.getRuneHolograms());
    }
  }

  private void orbitRunes(Location center, Set<Hologram> runeHolograms) {
    float step = 360f / runeHolograms.size();
    float start = ParticleTask.getCurrentTick();
    int index = 0;
    for (Hologram holo : runeHolograms) {
      float radian1 = (float) Math.toRadians(start + step * index);
      Location loc = center.clone();
      loc.add(Math.cos(radian1), 0, Math.sin(radian1));
      holo.teleport(loc);
      index++;
    }
  }

  public boolean isAttackBlocked(StrifeMob attacker, StrifeMob defender, float attackMult, AttackType attackType,
      boolean isBlocking) {
    if (rollBlock(defender, isBlocking)) {
      blockFatigue(defender.getEntity(), attackMult, isBlocking);
      bumpRunes(defender);
      DamageUtil.doReflectedDamage(defender, attacker, attackType);
      DamageUtil.doBlock(attacker, defender);
      return true;
    }
    return false;
  }

  public boolean rollBlock(StrifeMob strifeMob, boolean isBlocking) {
    if (strifeMob.getStat(StrifeStat.BLOCK) < 1) {
      return false;
    }
    updateStoredBlock(strifeMob);
    double blockChance = Math.min(blockDataMap.get(strifeMob.getEntity()).getStoredBlock() / 100, MAX_BLOCK_CHANCE);
    if (isBlocking) {
      blockChance *= 2;
    }
    LogUtil.printDebug("Block chance: " + blockChance);
    return random.nextDouble() < blockChance;
  }

  public long getMillisSinceBlock(LivingEntity livingEntity) {
    return System.currentTimeMillis() - blockDataMap.get(livingEntity).getLastHit();
  }

  public double getSecondsSinceBlock(LivingEntity livingEntity) {
    return ((double) getMillisSinceBlock(livingEntity)) / 1000;
  }

  public int getEarthRunes(LivingEntity livingEntity) {
    if (!blockDataMap.containsKey(livingEntity)) {
      return 0;
    }
    return blockDataMap.get(livingEntity).getRunes();
  }

  public void setEarthRunes(StrifeMob mob, int runes) {
    if (!blockDataMap.containsKey(mob.getEntity())) {
      BlockData data = new BlockData(0, mob.getStat(StrifeStat.BLOCK));
      blockDataMap.put(mob.getEntity(), data);
    }
    BlockData data = blockDataMap.get(mob.getEntity());
    int maxRunes = Math.round(mob.getStat(StrifeStat.MAX_EARTH_RUNES));
    if (maxRunes < 0.99 || mob.getStat(StrifeStat.EARTH_DAMAGE) < 1) {
      data.setRunes(0);
      pushRunesBar(mob, maxRunes, 0);
      return;
    }
    int newRunes = Math.max(Math.min(runes, maxRunes), 0);
    data.setRunes(newRunes);
    pushRunesBar(mob, maxRunes, newRunes);
  }

  public void bumpRunes(StrifeMob mob) {
    if (mob.getStat(StrifeStat.EARTH_DAMAGE) < 1) {
      return;
    }
    int runes = getEarthRunes(mob.getEntity());
    setEarthRunes(mob, runes + 1);
  }

  private void updateStoredBlock(StrifeMob mob) {
    if (blockDataMap.get(mob.getEntity()) == null) {
      BlockData data = new BlockData(System.currentTimeMillis() - DEFAULT_BLOCK_MILLIS, 0);
      blockDataMap.put(mob.getEntity(), data);
    }
    double maximumBlock = mob.getStat(StrifeStat.BLOCK);
    double block = blockDataMap.get(mob.getEntity()).getStoredBlock();
    double restoredBlock = FLAT_BLOCK_S + PERCENT_BLOCK_S * maximumBlock;
    if (mob.getStat(StrifeStat.BLOCK_RECOVERY) > 1) {
      restoredBlock *= 1 + mob.getStat(StrifeStat.BLOCK_RECOVERY) / 100;
    }

    block += restoredBlock * getSecondsSinceBlock(mob.getEntity());
    blockDataMap.get(mob.getEntity()).setStoredBlock(Math.min(block, maximumBlock));
    blockDataMap.get(mob.getEntity()).setLastHit(System.currentTimeMillis());
    LogUtil.printDebug("New block before clamp: " + block);
  }

  public void blockFatigue(LivingEntity livingEntity, double attackMultipler, boolean isBlocking) {
    BlockData data = blockDataMap.get(livingEntity);
    LogUtil.printDebug("Pre reduction block: " + data.getStoredBlock());
    double blockFatigue = attackMultipler * (isBlocking ? 50D : 100D);
    data.setStoredBlock(Math.max(0, data.getStoredBlock() - blockFatigue));
    LogUtil.printDebug("Post reduction block: " + data.getStoredBlock());
  }

  private static void pushRunesBar(StrifeMob mob, int maxRunes, int runes) {
    if (!(mob.getEntity() instanceof Player)) {
      return;
    }
    String message = ChatColor.GREEN + "Runes: " + ChatColor.DARK_GREEN + IntStream.range(0, runes).mapToObj(i -> "₪")
        .collect(Collectors.joining(""));
    message += ChatColor.BLACK + IntStream.range(0, maxRunes - runes).mapToObj(i -> "₪")
        .collect(Collectors.joining(""));

    AdvancedActionBarUtil.addMessage((Player) mob.getEntity(), "rune-bar", message, runes == 0 ? 200 : 12000, 6);
  }

  public static <T> T getRandomFromCollection(Collection<T> coll) {
    int num = (int) (Math.random() * coll.size());
    for (T t : coll) {
      if (--num < 0) {
        return t;
      }
    }
    throw new AssertionError();
  }
}
