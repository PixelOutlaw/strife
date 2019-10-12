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
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.ChampionSaveData.HealthDisplayType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ForceAttackSpeed;
import land.face.strife.util.StatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StatUpdateManager {

  private final StrifeMobManager strifeMobManager;

  public StatUpdateManager(final StrifeMobManager strifeMobManager) {
    this.strifeMobManager = strifeMobManager;
  }

  public Map<StrifeStat, Float> getItemStats(ItemStack stack) {
    return getItemStats(stack, 1.0f);
  }

  public Map<StrifeStat, Float> getItemStats(ItemStack stack, float multiplier) {
    if (stack == null || stack.getType() == Material.AIR) {
      return new HashMap<>();
    }
    Map<StrifeStat, Float> itemStats = new HashMap<>();

    List<String> lore = ItemStackExtensionsKt.getLore(stack);
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

  public void updateHealth(StrifeMob aEntity) {
    double maxHealth = Math.max(StatUtil.getHealth(aEntity), 1);
    aEntity.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    HealthDisplayType displayType = aEntity.getChampion().getSaveData().getHealthDisplayType();
    if (displayType == HealthDisplayType.TWO_HEALTH_HEARTS) {
      ((Player) aEntity.getEntity()).setHealthScaled(false);
      return;
    }
    ((Player) aEntity.getEntity()).setHealthScaled(true);
    ((Player) aEntity.getEntity()).setHealthScale(getHealthScale(displayType, maxHealth));
  }

  public void updateMovementSpeed(StrifeMob strifeMob) {
    LivingEntity entity = strifeMob.getEntity();
    double speed = strifeMob.getFinalStats().getOrDefault(StrifeStat.MOVEMENT_SPEED, 80f) / 100f;
    if (entity instanceof Player) {
      ((Player) entity).setWalkSpeed(0.2f * (float) speed);
      ((Player) entity).setFlySpeed(0.2f * (float) speed);
    } else {
      if (entity.getAttribute(GENERIC_MOVEMENT_SPEED) != null) {
        entity.getAttribute(GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
      }
      if (entity.getAttribute(GENERIC_FLYING_SPEED) != null) {
        entity.getAttribute(GENERIC_FLYING_SPEED).setBaseValue(speed);
      }
    }
  }

  public void updateAttackAttrs(StrifeMob strifeMob) {
    double attacksPerSecond = 1 / StatUtil.getAttackTime(strifeMob);
    ForceAttackSpeed.addAttackTime((Player) strifeMob.getEntity(), attacksPerSecond);
    strifeMob.getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(200);
  }

  public void updateBarrier(StrifeMob mob) {
    StrifePlugin.getInstance().getBarrierManager().createBarrierEntry(mob);
  }

  public void updateAttributes(Player player) {
    updateAttributes(strifeMobManager.getStatMob(player));
  }

  public void updateAttributes(StrifeMob strifeMob) {
    updateMovementSpeed(strifeMob);
    updateAttackAttrs(strifeMob);
    updateHealth(strifeMob);
    updateBarrier(strifeMob);
  }

  private double getHealthScale(HealthDisplayType healthDisplayType, double maxHealth) {
    switch (healthDisplayType) {
      case FIVE_HEALTH_HEARTS:
        return 2 * Math.ceil(maxHealth / 5);
      case TEN_HEALTH_HEARTS:
        return 2 * Math.ceil(maxHealth / 10);
      case FIVE_PERCENT_HEARTS:
        return 40;
      case THREE_PERCENT_HEARTS:
        return 60;
      case TEN_PERCENT_HEARTS:
      default:
        return 20;
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
