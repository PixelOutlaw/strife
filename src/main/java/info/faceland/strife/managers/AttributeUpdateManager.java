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
package info.faceland.strife.managers;

import static info.faceland.strife.attributes.StrifeAttribute.LEVEL_REQUIREMENT;
import static info.faceland.strife.attributes.StrifeAttribute.MOVEMENT_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_FLYING_SPEED;
import static org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.StatUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.HiltItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeUpdateManager {

  private final AttributedEntityManager attributedEntityManager;

  public AttributeUpdateManager(final AttributedEntityManager attributedEntityManager) {
    this.attributedEntityManager = attributedEntityManager;
  }

  public Map<StrifeAttribute, Double> getItemStats(ItemStack stack) {
    return getItemStats(stack, 1.0);
  }

  public Map<StrifeAttribute, Double> getItemStats(ItemStack stack, double multiplier) {
    if (stack == null || stack.getType() == Material.AIR) {
      return new HashMap<>();
    }
    HiltItemStack item = new HiltItemStack(stack);
    Map<StrifeAttribute, Double> itemStats = new HashMap<>();

    List<String> lore = item.getLore();
    if (lore == null || lore.isEmpty()) {
      return itemStats;
    }
    List<String> strippedLore = stripColor(lore);

    for (String s : strippedLore) {
      double amount = 0;
      String retained = CharMatcher.JAVA_LETTER.or(CharMatcher.is(' ')).retainFrom(s).trim();

      StrifeAttribute attribute = StrifeAttribute.fromName(retained);
      if (attribute == null) {
        continue;
      }
      amount += NumberUtils.toDouble(CharMatcher.DIGIT.or(CharMatcher.is('-')).retainFrom(s));
      if (amount == 0) {
        continue;
      }
      if (attribute != LEVEL_REQUIREMENT) {
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

  private void updateHealth(AttributedEntity aEntity) {
    double maxHealth = Math.max(StatUtil.getHealth(aEntity), 1);
    aEntity.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    if (aEntity.getEntity() instanceof Player) {
      ((Player) aEntity.getEntity()).setHealthScaled(true);
      ((Player) aEntity.getEntity()).setHealthScale(2 * Math.ceil(maxHealth / 10));
    }
  }

  private void updateMovementSpeed(AttributedEntity attributedEntity) {
    LivingEntity entity = attributedEntity.getEntity();
    double speed =
        attributedEntity.getAttributes().getOrDefault(MOVEMENT_SPEED, 80D) / 100;
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

  public void updateAttackSpeed(AttributedEntity attributedEntity) {
    double attacksPerSecond = 1 / StatUtil.getAttackTime(attributedEntity);
    attributedEntity.getEntity().getAttribute(GENERIC_ATTACK_SPEED).setBaseValue(attacksPerSecond);
  }

  public void updateAttributes(Player player) {
    updateAttributes(attributedEntityManager.getAttributedEntity(player));
  }

  public void updateAttributes(AttributedEntity attributedEntity) {
    updateMovementSpeed(attributedEntity);
    updateAttackSpeed(attributedEntity);
    updateHealth(attributedEntity);

    StrifePlugin.getInstance().getBarrierManager().createBarrierEntry(attributedEntity);
    attributedEntity.getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(200);
  }

  @SafeVarargs
  public static Map<StrifeAttribute, Double> combineMaps(Map<StrifeAttribute, Double>... maps) {
    Map<StrifeAttribute, Double> combinedMap = new HashMap<>();
    for (Map<StrifeAttribute, Double> map : maps) {
      for (Map.Entry<StrifeAttribute, Double> statMap : map.entrySet()) {
        double old = combinedMap.getOrDefault(statMap.getKey(), 0D);
        double combinedValue = old + statMap.getValue();
        combinedMap.put(statMap.getKey(), combinedValue);
      }
    }
    return combinedMap;
  }

}
