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

import com.tealcube.minecraft.bukkit.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeHandler {

    public static double getValue(LivingEntity livingEntity, StrifeAttribute attribute) {
        double amount = 0D;
        for (ItemStack itemStack : livingEntity.getEquipment().getArmorContents()) {
            amount += getValue(itemStack, attribute);
        }
        amount += getValue(livingEntity.getEquipment().getItemInHand(), attribute);
        return amount;
    }

    public static double getValue(ItemStack itemStack, StrifeAttribute attribute) {
        return getValue(new HiltItemStack(itemStack), attribute);
    }

    public static double getValue(HiltItemStack itemStack, StrifeAttribute attribute) {
        double amount = 0D;
        if (itemStack == null || itemStack.getType() == Material.AIR || attribute == null) {
            return amount;
        }
        List<String> lore = itemStack.getLore();
        List<String> strippedLore = stripColor(lore);
        for (String s : strippedLore) {
            String retained = CharMatcher.JAVA_LETTER.or(CharMatcher.is(' ')).retainFrom(s).trim();
            if (retained.equals(attribute.getName().trim())) {
                amount += NumberUtils.toDouble(CharMatcher.DIGIT.or(CharMatcher.is('-')).retainFrom(s));
            }
        }
        if (attribute.isPercentage()) {
            amount /= 100;
        }
        return attribute.getCap() > 0D ? Math.min(amount, attribute.getCap()) : amount;
    }

    private static List<String> stripColor(List<String> strings) {
        List<String> stripped = new ArrayList<>();
        for (String s : strings) {
            stripped.add(ChatColor.stripColor(s));
        }
        return stripped;
    }

    public static void updateHealth(Player player, Map<StrifeAttribute, Double> attributeDoubleMap) {
        if (!attributeDoubleMap.containsKey(StrifeAttribute.HEALTH)) {
            return;
        }
        double newMaxHealth = attributeDoubleMap.get(StrifeAttribute.HEALTH);
        double oldHealth = player.getHealth();
        if (player.getHealth() > newMaxHealth) {
            double tempHealth = Math.min(newMaxHealth, player.getMaxHealth()) / 2;
            player.setHealth(tempHealth);
        }
        player.setMaxHealth(newMaxHealth);
        player.setHealthScaled(true);
        player.setHealthScale(player.getMaxHealth());
        player.setHealth(Math.min(oldHealth, player.getMaxHealth()));
    }

}
