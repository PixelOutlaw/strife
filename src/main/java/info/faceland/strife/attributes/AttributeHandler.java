/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife.attributes;

import com.tealcube.minecraft.bukkit.facecore.shade.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.CharMatcher;
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
