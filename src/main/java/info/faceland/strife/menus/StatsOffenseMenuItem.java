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

import static info.faceland.strife.attributes.StrifeAttribute.*;

import com.tealcube.minecraft.bukkit.TextUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.StatUtil;
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
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
    private static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##");
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
        AttributedEntity pStats = plugin.getEntityStatCache().getAttributedEntity(player);
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
                lore.add(addStat("Melee Damage: ", StatUtil.getMeleeDamage(pStats), INT_FORMAT));
                itemStack.setType(Material.IRON_SWORD);
                break;
            case 1:
                lore.add(addStat("Ranged Damage: ", StatUtil.getRangedDamage(pStats), INT_FORMAT));
                itemStack.setType(Material.BOW);
                break;
            case 2:
                lore.add(addStat("Magic Damage: ", StatUtil.getMagicDamage(pStats), INT_FORMAT));
                itemStack.setType(Material.BLAZE_ROD);
                break;
        }
        lore.add(addStat("Attack Speed: ", StatUtil.getAttackTime(pStats), "s", TWO_DECIMAL));
        lore.add(addStat("Overcharge Multiplier: ", StatUtil.getOverchargeMultiplier(pStats), "x", TWO_DECIMAL));
        lore.add(breakLine);
        lore.add(addStat("Critical Rate: ", pStats.getAttribute(CRITICAL_RATE), "%", INT_FORMAT));
        lore.add(addStat("Critical Multiplier: ", StatUtil.getCriticalMultiplier(pStats), "x", TWO_DECIMAL));
        if (pStats.getAttribute(BLEED_CHANCE) > 0) {
            lore.add(breakLine);
            lore.add(addStat("Bleed Chance: ", pStats.getAttribute(BLEED_CHANCE), "%", INT_FORMAT));
        }
        if (pStats.getAttribute(HP_ON_HIT) > 0 || pStats.getAttribute(LIFE_STEAL) > 0) {
            lore.add(breakLine);
            if (pStats.getAttribute(HP_ON_HIT) > 0) {
                lore.add(addStat("Health On Hit: ", pStats.getAttribute(HP_ON_HIT), INT_FORMAT));
            }
            if (pStats.getAttribute(LIFE_STEAL) > 0) {
                lore.add(addStat("Life Steal: ", pStats.getAttribute(LIFE_STEAL), "%", INT_FORMAT));
            }
        }
        lore.add(breakLine);
        lore.add(addStat("Fire Damage: ", StatUtil.getFireDamage(pStats), INT_FORMAT));
        lore.add(addStat("Ignite Chance: ", pStats.getAttribute(IGNITE_CHANCE), "%", INT_FORMAT));
        if (pStats.getAttribute(ICE_DAMAGE) > 0) {
            lore.add(addStat("Ice Damage: ", StatUtil.getIceDamage(pStats), INT_FORMAT));
            lore.add(addStat("Freeze Chance: ", pStats.getAttribute(FREEZE_CHANCE), "%", INT_FORMAT));
        }
        if (pStats.getAttribute(LIGHTNING_DAMAGE) > 0) {
            lore.add(addStat("Lightning Damage: ", StatUtil.getLightningDamage(pStats), INT_FORMAT));
            lore.add(addStat("Shock Chance: ", pStats.getAttribute(SHOCK_CHANCE), "%", INT_FORMAT));
        }
        if (pStats.getAttribute(DARK_DAMAGE) > 0) {
            lore.add(addStat("Shadow Damage: ", StatUtil.getShadowDamage(pStats), INT_FORMAT));
            lore.add(addStat("Corrupt Chance: ", pStats.getAttribute(CORRUPT_CHANCE), "%", INT_FORMAT));
        }
        lore.add(breakLine);
        lore.add(addStat("Armor Penetration: ", pStats.getAttribute(ARMOR_PENETRATION), INT_FORMAT));
        lore.add(addStat("Ward Penetration: ", pStats.getAttribute(WARD_PENETRATION), INT_FORMAT));
        lore.add(addStat("Accuracy: ", pStats.getAttribute(ACCURACY), INT_FORMAT));
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

    private String addStat(String name, double value, DecimalFormat format) {
        return ChatColor.RED + name + ChatColor.WHITE + format.format(value);
    }

    private String addStat(String name, double value, String extra, DecimalFormat format) {
        return ChatColor.RED + name + ChatColor.WHITE + format.format(value) + extra;
    }
}
