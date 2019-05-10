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
package info.faceland.strife.menus.stats;

import static info.faceland.strife.attributes.StrifeAttribute.*;
import static info.faceland.strife.menus.stats.StatsMenu.INT_FORMAT;
import static info.faceland.strife.menus.stats.StatsMenu.TWO_DECIMAL;
import static info.faceland.strife.menus.stats.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.TextUtils;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.StatUtil;
import java.util.Map;
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

  StatsOffenseMenuItem(StrifePlugin plugin, Player player) {
    super(TextUtils.color("&c&lOffensive Stats"), new ItemStack(Material.IRON_SWORD));
    this.player = player;
    this.plugin = plugin;
  }

  StatsOffenseMenuItem(StrifePlugin plugin) {
    super(TextUtils.color("&c&lOffensive Stats"), new ItemStack(Material.IRON_SWORD));
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (this.player != null) {
      player = this.player;
    }
    AttributedEntity pStats = plugin.getAttributedEntityManager().getAttributedEntity(player);
    Map<StrifeAttribute, Double> bases = plugin.getMonsterManager()
        .getBaseStats(player, player.getLevel());
    // CombatStyle determines what stat type to use, as well as the icon
    // 0 = melee, 1 = ranged, 2 = magic
    AttackType type = AttackType.MELEE;
    if (player.getEquipment().getItemInMainHand().getType() == Material.BOW) {
      type = AttackType.RANGED;
    } else if (ItemUtil.isWand(player.getEquipment().getItemInMainHand())) {
      type = AttackType.MAGIC;
    }
    ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();
    lore.add(breakLine);
    switch (type) {
      case MELEE:
        lore.add(addStat("Melee Damage: ", StatUtil.getMeleeDamage(pStats), INT_FORMAT));
        itemStack.setType(Material.IRON_SWORD);
        break;
      case RANGED:
        lore.add(addStat("Ranged Damage: ", StatUtil.getRangedDamage(pStats), INT_FORMAT));
        itemStack.setType(Material.BOW);
        break;
      case MAGIC:
        lore.add(addStat("Magic Damage: ", StatUtil.getMagicDamage(pStats), INT_FORMAT));
        itemStack.setType(Material.BLAZE_ROD);
        break;
    }
    double acc = 100 + pStats.getAttribute(ACCURACY) - bases.getOrDefault(ACCURACY, 0D);
    lore.add(addStat("Accuracy Rating: ", acc, INT_FORMAT));
    lore.add(addStat("Attack Speed: ", StatUtil.getAttackTime(pStats), "s", TWO_DECIMAL));
    lore.add(breakLine);
    lore.add(addStat("Overcharge Multiplier: ", StatUtil.getOverchargeMultiplier(pStats), "x",
        TWO_DECIMAL));
    if (pStats.getAttribute(MULTISHOT) > 0 && type != AttackType.MELEE) {
      if (pStats.getAttribute(DOGE) > 0) {
        lore.add(addStat("MultiTHOT: ", pStats.getAttribute(MULTISHOT), "%", INT_FORMAT));
      } else {
        lore.add(addStat("Multishot: ", pStats.getAttribute(MULTISHOT), "%", INT_FORMAT));
      }
    }
    lore.add(addStat("Critical Rate: ", pStats.getAttribute(CRITICAL_RATE), "%", INT_FORMAT));
    lore.add(
        addStat("Critical Multiplier: ", StatUtil.getCriticalMultiplier(pStats), "x", TWO_DECIMAL));
    double aPen =
        pStats.getAttribute(ARMOR_PENETRATION) - bases.getOrDefault(ARMOR_PENETRATION, 0D);
    if (aPen != 0 && type != AttackType.MAGIC) {
      lore.add(addStat("Armor Penetration: " + ChatColor.WHITE + plus(aPen), aPen, INT_FORMAT));
    }
    double wPen = pStats.getAttribute(WARD_PENETRATION) - bases.getOrDefault(WARD_PENETRATION, 0D);
    if (wPen != 0 && type == AttackType.MAGIC) {
      lore.add(addStat("Ward Penetration: " + ChatColor.WHITE + plus(wPen), wPen, INT_FORMAT));
    }
    if (pStats.getAttribute(BLEED_CHANCE) > 0) {
      lore.add(addStat("Bleed Chance: ", pStats.getAttribute(BLEED_CHANCE), "%", INT_FORMAT));
    }
    if (pStats.getAttribute(BLEED_DAMAGE) > 0) {
      lore.add(
          addStat("Bleed Damage: " + ChatColor.WHITE + "+", pStats.getAttribute(BLEED_DAMAGE), "%",
              INT_FORMAT));
    }
    if (pStats.getAttribute(StrifeAttribute.MAXIMUM_RAGE) > 0 &&
        pStats.getAttribute(StrifeAttribute.RAGE_ON_HIT) > 0 ||
        pStats.getAttribute(StrifeAttribute.RAGE_ON_KILL) > 0) {
      lore.add(breakLine);
      lore.add(
          addStat("Maximum Rage: ", pStats.getAttribute(StrifeAttribute.MAXIMUM_RAGE), INT_FORMAT));
      lore.add(
          addStat("Rage On Hit: ", pStats.getAttribute(StrifeAttribute.RAGE_ON_HIT), INT_FORMAT));
      lore.add(
          addStat("Rage On Kill: ", pStats.getAttribute(StrifeAttribute.RAGE_ON_KILL), INT_FORMAT));
    }
    if (pStats.getAttribute(HP_ON_HIT) > 0 || pStats.getAttribute(LIFE_STEAL) > 0
        || pStats.getAttribute(HP_ON_KILL) > 0) {
      lore.add(breakLine);
      if (pStats.getAttribute(LIFE_STEAL) > 0) {
        lore.add(addStat("Life Steal: ", pStats.getAttribute(LIFE_STEAL), "%", INT_FORMAT));
      }
      if (pStats.getAttribute(HP_ON_HIT) > 0) {
        lore.add(addStat("Health On Hit: ", pStats.getAttribute(HP_ON_HIT), INT_FORMAT));
      }
      if (pStats.getAttribute(HP_ON_KILL) > 0) {
        lore.add(addStat("Health On Kill: ", pStats.getAttribute(HP_ON_KILL), INT_FORMAT));
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
    if (pStats.getAttribute(EARTH_DAMAGE) > 0) {
      lore.add(addStat("Earth Damage: ", StatUtil.getEarthDamage(pStats), INT_FORMAT));
      lore.add(addStat("Maximum Earth Runes: ", pStats.getAttribute(MAX_EARTH_RUNES), INT_FORMAT));
    }
    if (pStats.getAttribute(LIGHT_DAMAGE) > 0) {
      lore.add(addStat("Light Damage: ", StatUtil.getLightDamage(pStats), INT_FORMAT));
    }
    if (pStats.getAttribute(DARK_DAMAGE) > 0) {
      lore.add(addStat("Shadow Damage: ", StatUtil.getShadowDamage(pStats), INT_FORMAT));
      lore.add(addStat("Corrupt Chance: ", pStats.getAttribute(CORRUPT_CHANCE), "%", INT_FORMAT));
    }
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

  private String plus(double num) {
    return num >= 0 ? "+" : "";
  }
}
