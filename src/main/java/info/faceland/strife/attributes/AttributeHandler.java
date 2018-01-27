/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.attributes;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.StatUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.HiltItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeHandler {

    public static Map<StrifeAttribute, Double> getItemStats(ItemStack stack) {
        return getItemStats(stack, 1.0);
    }

    public static Map<StrifeAttribute, Double> getItemStats(ItemStack stack, double multiplier) {
        if (stack == null || stack.getType() == Material.AIR) {
            return null;
        }
        HiltItemStack item = new HiltItemStack(stack);
        Map<StrifeAttribute, Double> itemStats = new HashMap<>();

        List<String> lore = item.getLore();
        List<String> strippedLore = stripColor(lore);
        for (String s : strippedLore) {
            StrifeAttribute attribute = null;
            double amount = 0;
            String retained = CharMatcher.JAVA_LETTER.or(CharMatcher.is(' ')).retainFrom(s).trim();
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                if (attr.getName() == null) {
                    continue;
                }
                if (retained.equals(attr.getName().trim())) {
                    attribute = attr;
                    amount += NumberUtils.toDouble(CharMatcher.DIGIT.or(CharMatcher.is('-')).retainFrom(s));
                    break;
                }
            }
            if (attribute != null && amount > 0) {
                amount *= multiplier;
                if (itemStats.containsKey(attribute)) {
                    amount += itemStats.get(attribute);
                }
                itemStats.put(attribute, amount);
            }
        }
        return itemStats;
    }

    private static List<String> stripColor(List<String> strings) {
        List<String> stripped = new ArrayList<>();
        for (String s : strings) {
            stripped.add(ChatColor.stripColor(s));
        }
        return stripped;
    }

    public static void updateHealth(Player player, double maxHealth) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        player.setHealthScaled(true);
        player.setHealthScale(2 * Math.ceil(maxHealth / 10));
    }

    public static void updateAttributes(StrifePlugin plugin, Player player) {
        AttributedEntity playerStatEntity = plugin.getEntityStatCache().getAttributedEntity(player);

        double maxHealth = Math.max(StatUtil.getHealth(playerStatEntity), 1);
        AttributeHandler.updateHealth(player, maxHealth);

        double perc = playerStatEntity.getAttribute(StrifeAttribute.MOVEMENT_SPEED) / 100D;
        float speed = 0.2F * (float) perc;
        player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
        player.setFlySpeed(Math.min(Math.max(-1F, speed / 1.5f), 1F));
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(200);
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(2);
        System.out.println("ar: " + player.getAttribute(Attribute.GENERIC_ARMOR));
    }

    @SafeVarargs
    public static Map<StrifeAttribute, Double> combineMaps(Map<StrifeAttribute, Double>... maps) {
        Map<StrifeAttribute, Double> combinedMap = new HashMap<>();
        for (Map<StrifeAttribute, Double> map : maps) {
            for (Map.Entry<StrifeAttribute, Double> statMap : map.entrySet()) {
                double old = combinedMap.containsKey(statMap.getKey()) ? combinedMap.get(statMap.getKey()) : 0D;
                double combinedValue = old + statMap.getValue();
                combinedMap.put(statMap.getKey(), combinedValue);
            }
        }
        return combinedMap;
    }

}
