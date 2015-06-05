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

public class StatsDropsMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    public StatsDropsMenuItem(StrifePlugin plugin) {
        super(ChatColor.WHITE + "Mob Kill Modifiers", new ItemStack(Material.EMERALD));
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> valueMap = champion.getAttributeValues();
        ItemStack itemStack = new ItemStack(Material.EMERALD);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName());
        List<String> lore = new ArrayList<>(getLore());
        lore.add(ChatColor.GREEN + "Bonus Experience: " + ChatColor.WHITE + "+" + DECIMAL_FORMAT.format(valueMap.get(StrifeAttribute.XP_GAIN)*100)+"%");
        lore.add(ChatColor.GREEN + "Bonus Item Drop Rate: " + ChatColor.WHITE + "" +DECIMAL_FORMAT.format(valueMap.get(StrifeAttribute.ITEM_DISCOVERY)*100)+"%");
        lore.add(ChatColor.GREEN + "Bonus Cash Dropped: " + ChatColor.WHITE + "+" + DECIMAL_FORMAT.format(valueMap.get(StrifeAttribute.DAMAGE_REFLECT)*100)+"%");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}
