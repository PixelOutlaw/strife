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
package info.faceland.strife.menus;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
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
        double armor = 100 * (1-(100/(100 + (Math.pow((valueMap.get(StrifeAttribute.ARMOR) * 100), 1.3)))));
        lore.add(ChatColor.BLUE + "Armor: " + ChatColor.WHITE + DECIMAL_FORMAT.format(100 * valueMap
            .get(StrifeAttribute.ARMOR)) + ChatColor.GRAY + " (" + REDUCER_FORMAT.format(armor) + "%)" );
        if (valueMap.get(StrifeAttribute.EVASION) > 0) {
            double evasion = 100 * (1-(100/(100 + (Math.pow((valueMap.get(StrifeAttribute.EVASION) * 100), 1.25)))));
            lore.add(ChatColor.BLUE + "Evasion: " + ChatColor.WHITE + DECIMAL_FORMAT.format(100 * valueMap
                .get(StrifeAttribute.EVASION)) + ChatColor.GRAY + " (" + REDUCER_FORMAT.format(evasion) + "%)" );
        }
        if (valueMap.get(StrifeAttribute.RESISTANCE) > 0) {
            lore.add(
                ChatColor.BLUE + "Resistance: " + ChatColor.WHITE + DECIMAL_FORMAT.format(100*valueMap.get(StrifeAttribute.RESISTANCE)) + "%");
        }
        if (valueMap.get(StrifeAttribute.PARRY) > 0) {
            if (valueMap.get(StrifeAttribute.PARRY) < 0.85) {
                lore.add(ChatColor.BLUE + "Parry Chance: " + ChatColor.WHITE + DECIMAL_FORMAT
                    .format(valueMap.get(StrifeAttribute.PARRY) * 100) + "%");
            } else {
                lore.add(ChatColor.BLUE + "Parry Chance: " + ChatColor.WHITE + "85% " + ChatColor.GRAY + "(Max)");
            }

        }
        if (valueMap.get(StrifeAttribute.BLOCK) != 0.1) {
            if (valueMap.get(StrifeAttribute.BLOCK) < 0.85) {
                lore.add(ChatColor.BLUE + "Block: " + ChatColor.WHITE + DECIMAL_FORMAT
                    .format(valueMap.get(StrifeAttribute.BLOCK) * 100) + "%");
            } else {
                lore.add(ChatColor.BLUE + "Block: " + ChatColor.WHITE + "85% " + ChatColor.GRAY + "(Max)");
            }
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
