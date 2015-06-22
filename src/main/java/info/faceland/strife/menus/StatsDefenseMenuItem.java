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
package info.faceland.strife.menus;

import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.events.ItemClickEvent;
import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.items.MenuItem;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsDefenseMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");
    private static final DecimalFormat REDUCER_FORMAT = new DecimalFormat("#.#");

    public StatsDefenseMenuItem(StrifePlugin plugin) {
        super(ChatColor.WHITE + "Defensive Stats", new ItemStack(Material.IRON_CHESTPLATE));
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> valueMap = champion.getAttributeValues();
        ItemStack itemStack = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName());
        List<String> lore = new ArrayList<>(getLore());
        lore.add(ChatColor.BLUE + "Hitpoints: " + ChatColor.WHITE + DECIMAL_FORMAT.format(valueMap.get(StrifeAttribute.HEALTH)));
        if (valueMap.get(StrifeAttribute.REGENERATION) > 1) {
            lore.add(ChatColor.BLUE + "Regeneration: " + ChatColor.WHITE + valueMap.get(StrifeAttribute.REGENERATION));
        }
        if (valueMap.get(StrifeAttribute.ARMOR) > 0.35) {
            double highArmor = 100 * (1-(500/(500+Math.pow(((valueMap.get(StrifeAttribute.ARMOR) * 100) * (1 - valueMap.get(StrifeAttribute.ARMOR))), 1.7))));
            lore.add(ChatColor.BLUE + "Armor: " + ChatColor.WHITE + DECIMAL_FORMAT
                .format(100 * valueMap.get(StrifeAttribute.ARMOR)) + ChatColor.DARK_GRAY + " (" + REDUCER_FORMAT.format(highArmor) + "%)");
        } else {
            double lowArmor = 100 * (valueMap.get(StrifeAttribute.ARMOR) * (1 + (0.71-valueMap.get(StrifeAttribute.ARMOR))));
            lore.add(ChatColor.BLUE + "Armor: " + ChatColor.WHITE + DECIMAL_FORMAT
                .format(100 * valueMap.get(StrifeAttribute.ARMOR)) + ChatColor.DARK_GRAY + " (" + REDUCER_FORMAT.format(lowArmor) + "%)");
        }
        if (valueMap.get(StrifeAttribute.EVASION) > 0) {
            double evasion = 100 * (1-(100/(100 + (Math.pow((valueMap.get(StrifeAttribute.EVASION) * 100), 1.25)))));
            lore.add(ChatColor.BLUE + "Evasion: " + ChatColor.WHITE + DECIMAL_FORMAT.format(100 * valueMap
                .get(StrifeAttribute.EVASION)) + ChatColor.DARK_GRAY + " (" + REDUCER_FORMAT.format(evasion) + "%)" );
        }
        if (valueMap.get(StrifeAttribute.RESISTANCE) > 0) {
            lore.add(
                ChatColor.BLUE + "Resistance: " + ChatColor.WHITE + DECIMAL_FORMAT.format(100*valueMap.get(StrifeAttribute.RESISTANCE)) + "%");
        }
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}
