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

import static org.bukkit.attribute.Attribute.GENERIC_FLYING_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.ChampionSaveData.HealthDisplayType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record StatUpdateManager(StrifeMobManager strifeMobManager) {

  public Map<StrifeStat, Float> getItemStats(ItemStack stack) {
    return getItemStats(stack, 1.0f);
  }

  public Map<StrifeStat, Float> getItemStats(ItemStack stack, float multiplier) {
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
      if (attribute != StrifeStat.LEVEL_REQUIREMENT) {
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
    double oldMaxHealth = mob.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    double maxHealth = Math.max(StatUtil.getStat(mob, StrifeStat.HEALTH), 1);
    mob.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    mob.getEntity().setHealth(Math.min(maxHealth, health * (maxHealth / oldMaxHealth)));
  }

  public void updateWeight(StrifeMob mob) {
    float value = 1 - 40 / (40 + mob.getStat(StrifeStat.WEIGHT));
    mob.getEntity().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(value);
  }

  public void updateMovementSpeed(StrifeMob strifeMob) {
    LivingEntity entity = strifeMob.getEntity();
    double speed = strifeMob.getStat(StrifeStat.MOVEMENT_SPEED) / 100f;
    if (entity.getAttribute(GENERIC_MOVEMENT_SPEED) != null) {
      entity.getAttribute(GENERIC_MOVEMENT_SPEED).setBaseValue(0.1 * speed);
    }
    if (entity.getAttribute(GENERIC_FLYING_SPEED) != null) {
      entity.getAttribute(GENERIC_FLYING_SPEED).setBaseValue(0.1 * speed);
    }
  }

  public void updateVanillaAttributes(Player player) {
    updateVanillaAttributes(strifeMobManager.getStatMob(player));
  }

  public void updateVanillaAttributes(StrifeMob strifeMob) {
    updateMovementSpeed(strifeMob);
    updateHealth(strifeMob);
    updateWeight(strifeMob);
    float maxAir = strifeMobManager.getBaseAirTicks() * (1 + strifeMob.getStat(StrifeStat.LUNG_CAPACITY) / 100);
    strifeMob.getEntity().setRemainingAir(Math.min(strifeMob.getEntity().getRemainingAir(), (int) maxAir));
    strifeMob.getEntity().setMaximumAir((int) maxAir);
  }

  public static float getHealthScale(HealthDisplayType healthDisplayType, float maxHealth) {
    return switch (healthDisplayType) {
      case TWENTY_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxHealth / 20f);
      case TEN_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxHealth / 10f);
      case FIVE_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxHealth / 5f);
      case VANILLA_TWO_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxHealth / 2f);
      case TWO_ROWS_OF_LIFE -> 40f;
      case THREE_ROWS_OF_LIFE -> 60f;
      case ONE_ROW_OF_LIFE -> 20f;
    };
  }

  public static float getBarrierScale(HealthDisplayType healthDisplayType, float maxHealth,
      float maxBarrier) {
    return switch (healthDisplayType) {
      case VANILLA_TWO_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxBarrier / 2f);
      case FIVE_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxBarrier / 5f);
      case TEN_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxBarrier / 10f);
      case TWENTY_LIFE_PER_HEART -> 2f * (float) Math.ceil(maxBarrier / 20f);
      case TWO_ROWS_OF_LIFE -> 20f * (float) Math.max(Math.floor(2 * maxBarrier / maxHealth), 1);
      case THREE_ROWS_OF_LIFE -> 20f * (float) Math.max(Math.floor(3 * maxBarrier / maxHealth), 1);
      case ONE_ROW_OF_LIFE -> 20f * (float) Math.max(Math.floor(maxBarrier / maxHealth), 1);
    };
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
