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
package info.faceland.strife.menus;

import static info.faceland.strife.menus.StatsMenu.INT_FORMAT;
import static info.faceland.strife.menus.StatsMenu.ONE_DECIMAL;
import static info.faceland.strife.menus.StatsMenu.TWO_DECIMAL;
import static info.faceland.strife.menus.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.TextUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.StatUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsDefenseMenuItem extends MenuItem {

  private final StrifePlugin plugin;
  private Player player;
  private static final String hpPerFive = TextUtils.color("&7 (HP/5s)");

  StatsDefenseMenuItem(StrifePlugin plugin, Player player) {
    super(TextUtils.color("&e&lDefensive Stats"), new ItemStack(Material.IRON_CHESTPLATE));
    this.plugin = plugin;
    this.player = player;
  }

  StatsDefenseMenuItem(StrifePlugin plugin) {
    super(TextUtils.color("&e&lDefensive Stats"), new ItemStack(Material.IRON_CHESTPLATE));
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (this.player != null) {
      player = this.player;
    }
    AttributedEntity pStats = plugin.getEntityStatCache().getAttributedEntity(player);
    ItemStack itemStack = new ItemStack(Material.IRON_CHESTPLATE);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();

    lore.add(breakLine);
    if (pStats.getAttribute(StrifeAttribute.BARRIER) > 0) {
      lore.add(
          addStat("Maximum Barrier: ", pStats.getAttribute(StrifeAttribute.BARRIER), INT_FORMAT));
      lore.add(
          addStat("Barrier Recharge: ", StatUtil.getBarrierPerSecond(pStats), "/s", ONE_DECIMAL));
    }

    lore.add(addStat("Maximum Health: ", StatUtil.getHealth(pStats), INT_FORMAT));
    lore.add(addStat("Regeneration: ", StatUtil.getRegen(pStats), hpPerFive, TWO_DECIMAL));
    if (pStats.getAttribute(StrifeAttribute.MAXIMUM_RAGE) > 0 && pStats.getAttribute(StrifeAttribute.RAGE_WHEN_HIT) > 0) {
      lore.add(breakLine);
      lore.add(addStat("Maximum Rage: ", pStats.getAttribute(StrifeAttribute.MAXIMUM_RAGE), INT_FORMAT));
      lore.add(addStat("Rage When Hit: ", pStats.getAttribute(StrifeAttribute.RAGE_WHEN_HIT), ONE_DECIMAL));
    }
    lore.add(breakLine);
    lore.add(addStat("Armor Rating: ", StatUtil.getArmor(pStats), INT_FORMAT));
    lore.add(addStat("Ward Rating: ", StatUtil.getWarding(pStats), INT_FORMAT));
    lore.add(addStat("Evasion Rating: ", StatUtil.getEvasion(pStats), INT_FORMAT));
    if (pStats.getAttribute(StrifeAttribute.BLOCK) > 0) {
      lore.add(addStat("Block Rating: ", pStats.getAttribute(StrifeAttribute.BLOCK), INT_FORMAT));
    }
    if (pStats.getAttribute(StrifeAttribute.DAMAGE_REFLECT) > 0) {
      lore.add(addStat("Reflected Damage: ", pStats.getAttribute(StrifeAttribute.DAMAGE_REFLECT),
          INT_FORMAT));
    }
    lore.add(breakLine);
    lore.add(addStat("Fire Resistance: ", StatUtil.getFireResist(pStats), "%", INT_FORMAT));
    lore.add(addStat("Ice Resistance: ", StatUtil.getIceResist(pStats), "%", INT_FORMAT));
    lore.add(addStat("Lightning Resistance: ", StatUtil.getLightningResist(pStats), "%", INT_FORMAT));
    lore.add(addStat("Earth Resistance: ", StatUtil.getEarthResist(pStats), "%", INT_FORMAT));
    lore.add(addStat("Light Resistance: ", StatUtil.getLightResist(pStats), "%", INT_FORMAT));
    lore.add(addStat("Shadow Resistance: ", StatUtil.getShadowResist(pStats), "%", INT_FORMAT));
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
    return ChatColor.YELLOW + name + ChatColor.WHITE + format.format(value);
  }

  private String addStat(String name, double value, String extra, DecimalFormat format) {
    return ChatColor.YELLOW + name + ChatColor.WHITE + format.format(value) + extra;
  }
}
