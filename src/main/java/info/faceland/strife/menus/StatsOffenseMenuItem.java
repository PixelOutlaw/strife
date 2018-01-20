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

import com.tealcube.minecraft.bukkit.TextUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsOffenseMenuItem extends MenuItem {

    private final StrifePlugin plugin;
    private Player player;
    private static final DecimalFormat INT_FORMAT = new DecimalFormat("#");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final DecimalFormat AS_FORMAT = new DecimalFormat("#.##");
    private static final String breakLine = TextUtils.color("&7&m--------------------");

    public StatsOffenseMenuItem(StrifePlugin plugin, Player player) {
        super(TextUtils.color("&c&lOffensive Stats"), new ItemStack(Material.IRON_SWORD));
        this.player = player;
        this.plugin = plugin;
    }

    public StatsOffenseMenuItem(StrifePlugin plugin) {
        super(TextUtils.color("&c&lOffensive Stats"), new ItemStack(Material.IRON_SWORD));
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        if (this.player != null) {
            player = this.player;
        }
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        // CombatStyle determines what stat type to use, as well as the icon
        // 0 = melee, 1 = ranged, 2 = magic
        int combatStyle = 0;
        if (player.getEquipment().getItemInMainHand().getType() == Material.BOW) {
            combatStyle = 1;
        } else if (player.getEquipment().getItemInMainHand().getType() == Material.WOOD_SWORD) {
            if (player.getEquipment().getItemInMainHand().getItemMeta().getLore().get(1).endsWith("Wand")) {
                combatStyle = 2;
            }
        }
        ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(getDisplayName());
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        List<String> lore = new ArrayList<>();
        lore.add(breakLine);
        switch (combatStyle) {
            case 0:
                lore.add(ChatColor.RED + "Melee Damage: " + ChatColor.WHITE + INT_FORMAT.format(
                        champion.getCache().getAttribute(StrifeAttribute.MELEE_DAMAGE)));
                itemStack.setType(Material.IRON_SWORD);
                break;
            case 1:
                lore.add(ChatColor.RED + "Ranged Damage: " + ChatColor.WHITE + INT_FORMAT.format(
                        champion.getCache().getAttribute(StrifeAttribute.RANGED_DAMAGE)));
                itemStack.setType(Material.BOW);
                break;
            case 2:
                lore.add(ChatColor.RED + "Magic Damage: " + ChatColor.WHITE + INT_FORMAT.format(
                        champion.getCache().getAttribute(StrifeAttribute.MAGIC_DAMAGE)));
                itemStack.setType(Material.BLAZE_ROD);
                break;
        }
        double statCap;
        lore.add(ChatColor.RED + "Attack Speed: " + ChatColor.WHITE + AS_FORMAT.format(2 / (1 + champion.getCache().getAttribute(StrifeAttribute.ATTACK_SPEED) / 100))
                + "s " + ChatColor.GRAY + "(+" + INT_FORMAT.format(champion.getCache().getAttribute
                (StrifeAttribute.ATTACK_SPEED)) + "%)");

        lore.add(ChatColor.RED + "Overcharge: " + ChatColor.WHITE + DECIMAL_FORMAT
            .format(1 + champion.getCache().getAttribute(StrifeAttribute.OVERCHARGE) / 100) + "x");

        lore.add(ChatColor.RED + "Critical Strike: " + ChatColor.WHITE + DECIMAL_FORMAT.format(
            champion.getCache().getAttribute(StrifeAttribute.CRITICAL_RATE)) + "% " + ChatColor.GRAY + "(" + DECIMAL_FORMAT
            .format(1 + champion.getCache().getAttribute(StrifeAttribute.CRITICAL_DAMAGE) / 100) + "x)");

        lore.add(ChatColor.RED + "Life Steal: " + ChatColor.WHITE + INT_FORMAT.format(champion.getCache().getAttribute(StrifeAttribute.LIFE_STEAL)) + "%");

        lore.add(breakLine);

        lore.add(ChatColor.RED + "Fire Damage: " + ChatColor.WHITE + INT_FORMAT.format(champion.getCache().getAttribute(
                StrifeAttribute.FIRE_DAMAGE)) + ChatColor.GRAY + " (" + INT_FORMAT.format(champion.getCache().getAttribute(
                StrifeAttribute.IGNITE_CHANCE)) + "%)");

        if (champion.getCache().getAttribute(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            lore.add(ChatColor.RED + "Lightning Damage: " + ChatColor.WHITE + INT_FORMAT
                .format(champion.getCache().getAttribute(
                    StrifeAttribute.LIGHTNING_DAMAGE)) + ChatColor.GRAY + " (" + INT_FORMAT
                .format(champion.getCache().getAttribute(
                    StrifeAttribute.SHOCK_CHANCE)) + "%)");
        }

        if (champion.getCache().getAttribute(StrifeAttribute.ICE_DAMAGE) > 0) {
            lore.add(ChatColor.RED + "Ice Damage: " + ChatColor.WHITE + INT_FORMAT
                .format(champion.getCache().getAttribute(
                    StrifeAttribute.ICE_DAMAGE)) + ChatColor.GRAY + " (" + INT_FORMAT
                .format(champion.getCache().getAttribute(
                    StrifeAttribute.FREEZE_CHANCE)) + "%)");
        }

        if (champion.getCache().getAttribute(StrifeAttribute.DARK_DAMAGE) > 0) {
            lore.add(ChatColor.RED + "Shadow Damage: " + ChatColor.WHITE + INT_FORMAT
                .format(champion.getCache().getAttribute(
                    StrifeAttribute.DARK_DAMAGE)) + ChatColor.GRAY + " (" + INT_FORMAT
                .format(champion.getCache().getAttribute(
                    StrifeAttribute.CORRUPT_CHANCE)) + "%)");
        }

        lore.add(breakLine);

        statCap = Math.min(champion.getCache().getAttribute(StrifeAttribute.ACCURACY),
                StrifeAttribute.ACCURACY.getPlayerCap());
        lore.add(ChatColor.RED + "Accuracy: " + ChatColor.WHITE + "+" + INT_FORMAT.format(statCap * 100) + "%");

        statCap = Math.min(champion.getCache().getAttribute(StrifeAttribute.ARMOR_PENETRATION),
                StrifeAttribute.ARMOR_PENETRATION.getPlayerCap());
        lore.add(ChatColor.RED + "Armor Penetration: " + ChatColor.WHITE + INT_FORMAT.format(statCap * 100) + "%");

        lore.add(breakLine);

        lore.add(TextUtils.color("&8&oUse &7&o/help stats &8&ofor info!"));

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}
