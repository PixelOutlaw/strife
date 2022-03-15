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

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.BlockData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.StatUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockManager {

  private final StrifePlugin plugin;

  private final Map<StrifeMob, BlockData> blockDataMap = new WeakHashMap<>();
  private final Random random = new Random();
  private static final double MAX_BLOCK_CHANCE = 0.55;

  private final float FLAT_BLOCK_S;
  private final float PERCENT_BLOCK_S;
  private final float PERCENT_MAX_BLOCK_MIN;
  private final float MELEE_FATIGUE;
  private final float PROJECTILE_FATIGUE;
  private final float PHYSICAL_BLOCK_FATIGUE_MULT;
  private final float PHYSICAL_BLOCK_CHANCE_MULT;
  private final float GUARD_BREAK_POWER;

  private static final ItemStack BLOCK_DATA = new ItemStack(Material.COARSE_DIRT);

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

  public void tickBlock() {
    Map<LivingEntity, StrifeMob> loopMobs = Collections
        .synchronizedMap(plugin.getStrifeMobManager().getMobs());
    for (StrifeMob mob : loopMobs.values()) {
      if (mob.getMaxBlock() < 1) {
        continue;
      }
      float blockGain = FLAT_BLOCK_S + PERCENT_BLOCK_S * mob.getMaxBlock();
      mob.setBlock(Math.min(mob.getMaxBlock(), mob.getBlock() + blockGain / 10));
    }
  }

  public void tickHolograms() {
    Iterator<StrifeMob> iterator = blockDataMap.keySet().iterator();
    while (iterator.hasNext()) {
      StrifeMob mob = iterator.next();
      if (mob == null) {
        iterator.remove();
        continue;
      }
      BlockData blockData = blockDataMap.get(mob);
      if (!mob.getEntity().isValid()) {
        for (Hologram holo : blockData.getRuneHolograms()) {
          holo.delete();
        }
        iterator.remove();
        continue;
      }
      if (System.currentTimeMillis() > blockData.getRuneFalloff()) {
        setEarthRunes(mob, 0);
      }
      if (blockData.getRunes() == 0 && blockData.getRuneHolograms().size() == 0) {
        continue;
      }
      if (blockData.getRunes() < blockData.getRuneHolograms().size()) {
        while (blockData.getRunes() < blockData.getRuneHolograms().size()) {
          Hologram hologram = getRandomFromCollection(blockData.getRuneHolograms());
          mob.getEntity().getWorld().playSound(hologram.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);
          mob.getEntity().getWorld().spawnParticle(
              Particle.ITEM_CRACK,
              hologram.getLocation(),
              20, 0, 0, 0, 0.07f,
              BLOCK_DATA
          );
          blockData.getRuneHolograms().remove(hologram);
          hologram.delete();
        }
      } else {
        while (blockData.getRunes() > blockData.getRuneHolograms().size()
            && blockData.getRuneHolograms().size() < 6) {
          Hologram holo = DHAPI.createHologram(UUID.randomUUID().toString(),
              mob.getEntity().getEyeLocation().clone(),
              List.of("#ICON: COARSE_DIRT"));
          blockData.getRuneHolograms().add(holo);
        }
      }
      orbitRunes(mob.getEntity().getLocation().clone().add(0, 1, 0), blockData.getRuneHolograms());
    }
  }

  public void clearAllEarthRunes() {
    for (Entry<StrifeMob, BlockData> entry : blockDataMap.entrySet()) {
      if (entry.getValue().getRunes() > 0) {
        for (Hologram holo : entry.getValue().getRuneHolograms()) {
          holo.delete();
          holo.getLocation().getWorld().playSound(holo.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);
          holo.getLocation().getWorld().spawnParticle(
              Particle.ITEM_CRACK,
              holo.getLocation(),
              20, 0, 0, 0, 0.07f,
              BLOCK_DATA
          );
        }
      }
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
      DHAPI.moveHologram(holo, loc);
      index++;
    }
  }

  public boolean isAttackBlocked(StrifeMob attacker, StrifeMob defender, float attackMult,
      AttackType attackType, boolean isBlocking, boolean guardBreak) {
    if (rollBlock(defender, attackMult, isBlocking, attackType == AttackType.PROJECTILE, guardBreak)) {
      DamageUtil.doReflectedDamage(defender, attacker, attackType);
      DamageUtil.doBlock(attacker, defender);
      return true;
    }
    return false;
  }

  public int getEarthRunes(StrifeMob mob) {
    if (!blockDataMap.containsKey(mob)) {
      return 0;
    }
    return blockDataMap.get(mob).getRunes();
  }

  public void setEarthRunes(StrifeMob mob, int runes) {
    if (!blockDataMap.containsKey(mob)) {
      BlockData data = new BlockData();
      blockDataMap.put(mob, data);
    }
    BlockData data = blockDataMap.get(mob);
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
    int runes = getEarthRunes(mob);
    setEarthRunes(mob, runes + 1);
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

    if (random.nextDouble() > blockChance) {
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

  private static void pushRunesBar(StrifeMob mob, int maxRunes, int runes) {
    if (!(mob.getEntity() instanceof Player)) {
      return;
    }
    if (runes == 0) {
      StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
          new GUIComponent("rune-display", GuiManager.EMPTY, 0, 0, Alignment.RIGHT));
      StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
          new GUIComponent("rune-amount", GuiManager.EMPTY, 0, 0, Alignment.RIGHT));
      return;
    }
    StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
        new GUIComponent("rune-display", GuiManager.EARTH_RUNE, 17, 125, Alignment.RIGHT));
    String string = StrifePlugin.getInstance().getGuiManager().convertToHpDisplay(runes);
    TextComponent aaa = new TextComponent(ChatColor.GREEN + string + ChatColor.RESET);
    StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
        new GUIComponent("rune-amount", aaa, string.length() * 8, 128, Alignment.RIGHT));
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
