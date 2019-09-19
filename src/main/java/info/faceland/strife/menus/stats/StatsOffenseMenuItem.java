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

import static info.faceland.strife.menus.stats.StatsMenu.INT_FORMAT;
import static info.faceland.strife.menus.stats.StatsMenu.TWO_DECIMAL;
import static info.faceland.strife.menus.stats.StatsMenu.breakLine;
import static info.faceland.strife.stats.StrifeStat.ACCURACY;
import static info.faceland.strife.stats.StrifeStat.ARMOR_PENETRATION;
import static info.faceland.strife.stats.StrifeStat.BLEED_CHANCE;
import static info.faceland.strife.stats.StrifeStat.BLEED_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.CORRUPT_CHANCE;
import static info.faceland.strife.stats.StrifeStat.CRITICAL_RATE;
import static info.faceland.strife.stats.StrifeStat.DARK_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.DOGE;
import static info.faceland.strife.stats.StrifeStat.EARTH_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.FREEZE_CHANCE;
import static info.faceland.strife.stats.StrifeStat.HP_ON_HIT;
import static info.faceland.strife.stats.StrifeStat.HP_ON_KILL;
import static info.faceland.strife.stats.StrifeStat.ICE_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.IGNITE_CHANCE;
import static info.faceland.strife.stats.StrifeStat.LIFE_STEAL;
import static info.faceland.strife.stats.StrifeStat.LIGHTNING_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.LIGHT_DAMAGE;
import static info.faceland.strife.stats.StrifeStat.MAX_EARTH_RUNES;
import static info.faceland.strife.stats.StrifeStat.MULTISHOT;
import static info.faceland.strife.stats.StrifeStat.SHOCK_CHANCE;
import static info.faceland.strife.stats.StrifeStat.WARD_PENETRATION;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil.AttackType;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.StatUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsOffenseMenuItem extends MenuItem {

  StatsOffenseMenuItem() {
    super(TextUtils.color("&c&lOffensive Stats"), new ItemStack(Material.IRON_SWORD));
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    StrifeMob pStats = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    Map<StrifeStat, Float> bases = StrifePlugin.getInstance().getMonsterManager()
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
    double acc = 100 + pStats.getStat(ACCURACY) - bases.getOrDefault(ACCURACY, 0f);
    lore.add(addStat("Accuracy Rating: ", acc, INT_FORMAT));
    lore.add(addStat("Attack Speed: ", StatUtil.getAttackTime(pStats), "s", TWO_DECIMAL));
    lore.add(breakLine);
    lore.add(addStat("Overcharge Multiplier: ", StatUtil.getOverchargeMultiplier(pStats), "x",
        TWO_DECIMAL));
    if (pStats.getStat(MULTISHOT) > 0 && type != AttackType.MELEE) {
      if (pStats.getStat(DOGE) > 0) {
        lore.add(addStat("MultiTHOT: ", pStats.getStat(MULTISHOT), "%", INT_FORMAT));
      } else {
        lore.add(addStat("Multishot: ", pStats.getStat(MULTISHOT), "%", INT_FORMAT));
      }
    }
    lore.add(addStat("Critical Rate: ", pStats.getStat(CRITICAL_RATE), "%", INT_FORMAT));
    lore.add(
        addStat("Critical Multiplier: ", StatUtil.getCriticalMultiplier(pStats), "x", TWO_DECIMAL));
    double aPen =
        pStats.getStat(ARMOR_PENETRATION) - bases.getOrDefault(ARMOR_PENETRATION, 0f);
    if (aPen != 0 && type != AttackType.MAGIC) {
      lore.add(addStat("Armor Penetration: " + ChatColor.WHITE + plus(aPen), aPen, INT_FORMAT));
    }
    double wPen = pStats.getStat(WARD_PENETRATION) - bases.getOrDefault(WARD_PENETRATION, 0f);
    if (wPen != 0 && type == AttackType.MAGIC) {
      lore.add(addStat("Ward Penetration: " + ChatColor.WHITE + plus(wPen), wPen, INT_FORMAT));
    }
    if (pStats.getStat(BLEED_CHANCE) > 0) {
      lore.add(addStat("Bleed Chance: ", pStats.getStat(BLEED_CHANCE), "%", INT_FORMAT));
    }
    if (pStats.getStat(BLEED_DAMAGE) > 0) {
      lore.add(
          addStat("Bleed Damage: " + ChatColor.WHITE + "+", pStats.getStat(BLEED_DAMAGE), "%",
              INT_FORMAT));
    }
    if (pStats.getStat(StrifeStat.MAXIMUM_RAGE) > 0 &&
        pStats.getStat(StrifeStat.RAGE_ON_HIT) > 0 ||
        pStats.getStat(StrifeStat.RAGE_ON_KILL) > 0) {
      lore.add(breakLine);
      lore.add(
          addStat("Maximum Rage: ", pStats.getStat(StrifeStat.MAXIMUM_RAGE), INT_FORMAT));
      lore.add(
          addStat("Rage On Hit: ", pStats.getStat(StrifeStat.RAGE_ON_HIT), INT_FORMAT));
      lore.add(
          addStat("Rage On Kill: ", pStats.getStat(StrifeStat.RAGE_ON_KILL), INT_FORMAT));
    }
    if (pStats.getStat(HP_ON_HIT) > 0 || pStats.getStat(LIFE_STEAL) > 0
        || pStats.getStat(HP_ON_KILL) > 0) {
      lore.add(breakLine);
      if (pStats.getStat(LIFE_STEAL) > 0) {
        lore.add(addStat("Life Steal: ", pStats.getStat(LIFE_STEAL), "%", INT_FORMAT));
      }
      if (pStats.getStat(HP_ON_HIT) > 0) {
        lore.add(addStat("Health On Hit: ", pStats.getStat(HP_ON_HIT), INT_FORMAT));
      }
      if (pStats.getStat(HP_ON_KILL) > 0) {
        lore.add(addStat("Health On Kill: ", pStats.getStat(HP_ON_KILL), INT_FORMAT));
      }
    }
    lore.add(breakLine);
    lore.add(addStat("Fire Damage: ", StatUtil.getFireDamage(pStats), INT_FORMAT));
    lore.add(addStat("Ignite Chance: ", pStats.getStat(IGNITE_CHANCE), "%", INT_FORMAT));
    if (pStats.getStat(ICE_DAMAGE) > 0) {
      lore.add(addStat("Ice Damage: ", StatUtil.getIceDamage(pStats), INT_FORMAT));
      lore.add(addStat("Freeze Chance: ", pStats.getStat(FREEZE_CHANCE), "%", INT_FORMAT));
    }
    if (pStats.getStat(LIGHTNING_DAMAGE) > 0) {
      lore.add(addStat("Lightning Damage: ", StatUtil.getLightningDamage(pStats), INT_FORMAT));
      lore.add(addStat("Shock Chance: ", pStats.getStat(SHOCK_CHANCE), "%", INT_FORMAT));
    }
    if (pStats.getStat(EARTH_DAMAGE) > 0) {
      lore.add(addStat("Earth Damage: ", StatUtil.getEarthDamage(pStats), INT_FORMAT));
      lore.add(addStat("Maximum Earth Runes: ", pStats.getStat(MAX_EARTH_RUNES), INT_FORMAT));
    }
    if (pStats.getStat(LIGHT_DAMAGE) > 0) {
      lore.add(addStat("Light Damage: ", StatUtil.getLightDamage(pStats), INT_FORMAT));
    }
    if (pStats.getStat(DARK_DAMAGE) > 0) {
      lore.add(addStat("Shadow Damage: ", StatUtil.getShadowDamage(pStats), INT_FORMAT));
      lore.add(addStat("Corrupt Chance: ", pStats.getStat(CORRUPT_CHANCE), "%", INT_FORMAT));
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
