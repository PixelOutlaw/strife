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

import static land.face.strife.util.UberConfig.BASE_AIR_TICKS;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatUpdateManager {

  private final StrifePlugin plugin;

  public StatUpdateManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Map<StrifeStat, Float> getItemStatsFromLore(ItemStack stack) {
    return getItemStatsFromLore(stack, 1.0f);
  }

  public Map<StrifeStat, Float> getItemStatsFromLore(ItemStack stack, float multiplier) {
    if (stack == null || stack.getType() == Material.AIR) {
      return new HashMap<>();
    }
    Map<StrifeStat, Float> itemStats = new HashMap<>();

    if (stack.getLore() == null) {
      return itemStats;
    }

    List<String> lore = new ArrayList<>(stack.getLore());
    if (lore.isEmpty()) {
      return itemStats;
    }
    List<String> strippedLore = stripColor(lore);

    for (String s : strippedLore) {
      s = ItemUtil.splitOnOffset(s);
      float amount = 0;
      String retained = CharMatcher.forPredicate(Character::isLetter).or(CharMatcher.is(' '))
          .retainFrom(s).trim();
      StrifeStat attribute = StrifeStat.fromName(retained);
      if (attribute == null) {
        continue;
      }
      amount += NumberUtils.toFloat(CharMatcher.digit().or(CharMatcher.is('-')).retainFrom(s));
      if (amount == 0) {
        continue;
      }
      if (attribute != StrifeStat.LEVEL_REQUIREMENT && attribute != StrifeStat.SKILL_REQUIREMENT) {
        amount *= multiplier;
      }
      if (itemStats.containsKey(attribute)) {
        amount += itemStats.get(attribute);
      }
      itemStats.put(attribute, amount);
    }
    return itemStats;
  }

  private List<String> stripColor(List<String> strings) {
    List<String> stripped = new ArrayList<>();
    for (String s : strings) {
      stripped.add(ChatColor.stripColor(s));
    }
    return stripped;
  }

  public void updateHealth(StrifeMob mob) {
    if (!mob.getEntity().isValid()) {
      return;
    }
    double health = mob.getEntity().getHealth();
    double oldMaxHealth = mob.getEntity().getAttribute(Attribute.MAX_HEALTH).getBaseValue();
    double maxHealth = Math.max(StatUtil.getStat(mob, StrifeStat.HEALTH), 1);
    mob.getEntity().getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
    mob.getEntity().setHealth(Math.min(maxHealth, health * (maxHealth / oldMaxHealth)));
  }

  public void updateWeight(StrifeMob mob) {
    float amount = 0.35f;
    float weight = mob.getStat(StrifeStat.WEIGHT);
    if (weight >= 100) {
      amount += 0.6499999f * ((weight-100) / 100);
      amount = Math.min(0.9999999f, amount);
    } else {
      amount *= weight / 100;
    }
    mob.getEntity().getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(amount);
  }

  public void updateSize(StrifeMob mob) {
    float value = mob.getStat(StrifeStat.CHARACTER_SIZE);
    if (Math.round(value) == 100) {
      return;
    }
    value = mob.getStat(StrifeStat.CHARACTER_SIZE) / 100f;
    mob.getEntity().getAttribute(Attribute.SCALE).setBaseValue(value);
  }

  public void updateRange(StrifeMob mob) {
    float multiplier = 1f + (mob.getStat(StrifeStat.ATTACK_RANGE) / 100);
    float value = 2.75f * multiplier;
    mob.getEntity().getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(0.4f + value);
    mob.getEntity().getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(1.5f + value);
  }

  public void updateBreak(StrifeMob mob) {
    mob.getEntity().getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0.005);
  }

  public void updateLeg(StrifeMob mob) {
    if (mob.getChampion() != null && !mob.getChampion().getSaveData().isLongLegs()) {
      mob.getEntity().getAttribute(Attribute.STEP_HEIGHT).setBaseValue(0.6);
      return;
    }
    mob.getEntity().getAttribute(Attribute.STEP_HEIGHT).setBaseValue(1.07);
  }

  public void updateJump(StrifeMob mob) {
    float netGravity = mob.getStat(StrifeStat.INCREASED_GRAVITY) - mob.getStat(StrifeStat.REDUCED_GRAVITY);
    float gravityRatio;
    if (netGravity == 0) {
      gravityRatio = 1;
      mob.getEntity().getAttribute(Attribute.GRAVITY).setBaseValue(0.08f);
    } else if (netGravity > 0) {
      gravityRatio = 1 + netGravity / 185f;
      float multiplier = 1 + (netGravity / 100);
      mob.getEntity().getAttribute(Attribute.GRAVITY).setBaseValue(0.08f * multiplier);
    } else {
      gravityRatio = 155f / (-netGravity + 155f);
      float multiplier = 1 + (-netGravity / 100);
      mob.getEntity().getAttribute(Attribute.GRAVITY).setBaseValue(0.08f / multiplier);
    }
    float value = (1f + mob.getStat(StrifeStat.JUMP_HEIGHT) / 100) * gravityRatio;
    if (value <= 0) {
      mob.getEntity().getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
    } else {
      mob.getEntity().getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(value * 0.41f);
    }
  }

  public void updateMovementSpeed(StrifeMob strifeMob) {
    LivingEntity entity = strifeMob.getEntity();
    float speed = strifeMob.getStat(StrifeStat.MOVEMENT_SPEED) / 100f;
    if (entity.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
      entity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1 * speed);
    }
    if (entity.getAttribute(Attribute.FLYING_SPEED) != null) {
      entity.getAttribute(Attribute.FLYING_SPEED).setBaseValue(0.1 * speed);
    }
    if (entity instanceof Player) {
      ((Player) entity).setFlySpeed(0.25f);
    }
  }

  public void updateAllAttributes(Player player) {
    updateAllAttributes(plugin.getStrifeMobManager().getStatMob(player));
  }

  public void updateAllAttributes(StrifeMob strifeMob) {
    updateMovementSpeed(strifeMob);
    updateHealth(strifeMob);
    updateWeight(strifeMob);
    updateSize(strifeMob);
    updateJump(strifeMob);
    updateLeg(strifeMob);

    float maxAir = BASE_AIR_TICKS * (1 + strifeMob.getStat(StrifeStat.LUNG_CAPACITY) / 100);
    strifeMob.getEntity().setRemainingAir(Math.min(strifeMob.getEntity().getRemainingAir(), (int) maxAir));
    strifeMob.getEntity().setMaximumAir((int) maxAir);
    StatUtil.getStat(strifeMob, StrifeStat.BARRIER);
    StatUtil.getStat(strifeMob, StrifeStat.MAXIMUM_RAGE);
    StatUtil.getStat(strifeMob, StrifeStat.MAX_PRAYER_POINTS);

    if (strifeMob.getChampion() != null) {
      updateRange(strifeMob);
      updateBreak(strifeMob);
      Player p = (Player) strifeMob.getEntity();
      if (plugin.getGuiManager().getLifeSeparators().get(105) != null) {
        if (strifeMob.getMaxLife() >= 105) {
          plugin.getGuiManager().updateComponent(p,
              plugin.getGuiManager().getLifeSeparators().get((int) strifeMob.getMaxLife()));
        } else {
          plugin.getGuiManager().updateComponent(p,
              new GUIComponent("life-segments", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
        }
        if (strifeMob.getMaxEnergy() >= 105) {
          plugin.getGuiManager().updateComponent(p,
              plugin.getGuiManager().getEnergySeparators().get((int) strifeMob.getMaxEnergy()));
        } else {
          plugin.getGuiManager().updateComponent(p,
              new GUIComponent("energy-segments", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
        }
      }
    }
  }

  @SafeVarargs
  public static Map<StrifeStat, Float> combineMaps(Map<StrifeStat, Float>... maps) {
    Map<StrifeStat, Float> combinedMap = new HashMap<>();
    for (Map<StrifeStat, Float> map : maps) {
      for (Map.Entry<StrifeStat, Float> statMap : map.entrySet()) {
        float old = combinedMap.getOrDefault(statMap.getKey(), 0f);
        float combinedValue = old + statMap.getValue();
        combinedMap.put(statMap.getKey(), combinedValue);
      }
    }
    return combinedMap;
  }

}
