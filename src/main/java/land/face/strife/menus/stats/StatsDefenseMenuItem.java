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
package land.face.strife.menus.stats;

import static land.face.strife.menus.stats.StatsMenu.INT_FORMAT;
import static land.face.strife.menus.stats.StatsMenu.ONE_DECIMAL;
import static land.face.strife.menus.stats.StatsMenu.TWO_DECIMAL;
import static land.face.strife.menus.stats.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.StatUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsDefenseMenuItem extends MenuItem {

  private final StatsMenu statsMenu;
  public static final String PER_TEN = ChatColor.GRAY + "/10s";
  private final Map<Player, ItemStack> cachedIcon = new HashMap<>();

  StatsDefenseMenuItem(StatsMenu statsMenu) {
    super(StringExtensionsKt.chatColorize("&e&lDefense Stats"), new ItemStack(Material.BARRIER));
    this.statsMenu = statsMenu;
    ItemStackExtensionsKt.setCustomModelData(getIcon(), 50);
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = statsMenu.getInspectionTargetMap().get(commandSender);
    if (!player.isValid()) {
      return getIcon();
    }
    if (cachedIcon.containsKey(player)) {
      return cachedIcon.get(player);
    }
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = getIcon().clone();
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();

    lore.add(breakLine);
    if (!mob.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED) && mob.getMaxBarrier() > 0) {
      lore.add(addStat("Maximum Barrier: ", StatUtil.getStat(mob, StrifeStat.BARRIER), INT_FORMAT));
      lore.add(addStat("Barrier Recharge: ", StatUtil.getBarrierPerSecond(mob) * 10, PER_TEN, INT_FORMAT));
      if (mob.getStat(StrifeStat.BARRIER_REGEN) > 0) {
        lore.add(addStat("Barrier Regeneration: ", StatUtil.getStat(mob, StrifeStat.BARRIER_REGEN), PER_TEN, INT_FORMAT));
      }
      lore.add(breakLine);
    }

    lore.add(addStat("Maximum Life: ", mob.getMaxLife(), INT_FORMAT));
    lore.add(addStat("Life Regeneration: ", StatUtil.getStat(mob, StrifeStat.REGENERATION), PER_TEN, INT_FORMAT));
    if (mob.getStat(StrifeStat.RAGE_WHEN_HIT) > 0) {
      lore.add(breakLine);
      lore.add(addStat("Maximum Rage: ", mob.getStat(StrifeStat.MAXIMUM_RAGE), INT_FORMAT));
      lore.add(addStat("Rage When Hit: ", mob.getStat(StrifeStat.RAGE_WHEN_HIT), ONE_DECIMAL));
    }
    lore.add(breakLine);

    float armor = StatUtil.getStat(mob, StrifeStat.ARMOR);
    String physReduction = "";
    if (armor > 0.1) {
      float armorMultNumber = 100 * (1 - StatUtil.getArmorMult(armor));
      physReduction = " " + FaceColor.GRAY + "(-" + INT_FORMAT.format(armorMultNumber) + "%" +
          FaceColor.RED + "⚔" + FaceColor.GRAY + ")";
    }
    lore.add(addStat("Armor Rating: ", armor, INT_FORMAT) + physReduction);

    float warding = StatUtil.getStat(mob, StrifeStat.WARDING);
    String wardReduction = "";
    if (warding > 0.1) {
      float wardMultNumber = 100 * (1 - StatUtil.getWardingMult(warding));
      wardReduction = " " + FaceColor.GRAY + "(-" + INT_FORMAT.format(wardMultNumber) + "%" +
          FaceColor.BLUE + "☄" + FaceColor.GRAY + ")";
    }
    lore.add(addStat("Ward Rating: ", warding, INT_FORMAT) + wardReduction);

    float evasion = StatUtil.getEvasion(mob);
    float dodgeChance = StatUtil.getStat(mob, StrifeStat.DODGE_CHANCE);
    if (evasion > 0.1) {
      lore.add(addStat("Evasion Rating: ", evasion, INT_FORMAT));
    }
    if (evasion > 10 || dodgeChance > 0.5) {
      Map<StrifeStat, Float> normalMobStats = StrifePlugin.getInstance()
          .getMonsterManager().getBaseStats(EntityType.ZOMBIE, player.getLevel());
      float accForLevel = normalMobStats.get(StrifeStat.ACCURACY) *
          (1 + normalMobStats.get(StrifeStat.ACCURACY_MULT) / 100);
      float dodgeFromEvasion = DamageUtil.getDodgeChanceFromEvasion(evasion, accForLevel);
      float evasionRate = 0;
      if (dodgeFromEvasion > 0) {
        evasionRate = (100 - dodgeChance) * dodgeFromEvasion;
      }
      lore.add(addStat("Chance To Avoid Hits: ", evasionRate + dodgeChance, INT_FORMAT) + "%");
      if (dodgeChance > 0.5) {
        lore.add(FaceColor.GRAY + " +" + INT_FORMAT.format(dodgeChance) + "% From Dodge Chance");
      }
      if (dodgeFromEvasion > 0) {
        lore.add(FaceColor.GRAY + " +" + INT_FORMAT.format(evasionRate) + "% From Evasion (Estimated)");
      }
    }
    if (mob.getStat(StrifeStat.BLOCK) > 0) {
      lore.add(addStat("Block Rating: ", mob.getStat(StrifeStat.BLOCK), INT_FORMAT));
      float blockRecovery = mob.getStat(StrifeStat.BLOCK_RECOVERY);
      if (blockRecovery != 0) {
        String plus = blockRecovery >= 0 ? FaceColor.WHITE + "+" : "";
        lore.add(addStat("Block Recovery: " + plus,
            mob.getStat(StrifeStat.BLOCK_RECOVERY), INT_FORMAT) + "%");
      }
    }
    if (mob.getStat(StrifeStat.DAMAGE_REFLECT) > 0) {
      lore.add(addStat("Reflected Damage: ", mob.getStat(StrifeStat.DAMAGE_REFLECT),
          INT_FORMAT));
    }
    lore.add(breakLine);
    lore.add(FaceColor.YELLOW + "Elemental Resistances:");
    StringBuilder resistDisplay = new StringBuilder();
    resistDisplay.append(" ");
    addResist(resistDisplay, StatUtil.getStat(mob, StrifeStat.FIRE_RESIST), FaceColor.ORANGE, "\uD83D\uDD25");
    addResist(resistDisplay, StatUtil.getStat(mob, StrifeStat.ICE_RESIST), FaceColor.CYAN, "❄");
    addResist(resistDisplay, StatUtil.getStat(mob, StrifeStat.LIGHTNING_RESIST), FaceColor.YELLOW, "⚡");
    addResist(resistDisplay, StatUtil.getStat(mob, StrifeStat.EARTH_RESIST), FaceColor.GREEN, "₪");
    lore.add(resistDisplay.toString());
    StringBuilder resistDisplay2 = new StringBuilder();
    resistDisplay2.append(" ");
    addResist(resistDisplay2, StatUtil.getStat(mob, StrifeStat.LIGHT_RESIST), FaceColor.WHITE, "❂");
    addResist(resistDisplay2, StatUtil.getStat(mob, StrifeStat.DARK_RESIST), FaceColor.PURPLE, "☠");

    lore.add(resistDisplay2.toString());
    lore.add(FaceColor.YELLOW + "Status Resistances:");
    StringBuilder resistDisplay3 = new StringBuilder();
    resistDisplay3.append(" ");
    addResist(resistDisplay3, StatUtil.getStat(mob, StrifeStat.BLEED_RESIST), FaceColor.RED, "\uD83D\uDCA7");
    addResist(resistDisplay3, StatUtil.getStat(mob, StrifeStat.POISON_RESIST), FaceColor.GREEN, "\uD83D\uDCA7");
    addResist(resistDisplay3, StatUtil.getStat(mob, StrifeStat.WITHER_RESIST), FaceColor.DARK_GRAY, "☠");
    addResist(resistDisplay3, StatUtil.getStat(mob, StrifeStat.BURNING_RESIST), FaceColor.RED, "\uD83D\uDD25");
    lore.add(resistDisplay3.toString());
    lore.add(breakLine);
    lore.add(StringExtensionsKt.chatColorize("&8&oUse &7&o/help stats &8&ofor info!"));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);

    cachedIcon.put(player, itemStack);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> cachedIcon.remove(player), 2);
    return itemStack;
  }

  private static void addResist(StringBuilder builder, float value, FaceColor color, String symbol) {
    builder.append(color).append((int) value).append("%").append(symbol).append("  ");
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

  private String addStat(String name, double value, DecimalFormat format) {
    return FaceColor.YELLOW + name + FaceColor.WHITE + format.format(value);
  }

  private String addStat(String name, double value, String extra, DecimalFormat format) {
    return FaceColor.YELLOW + name + FaceColor.WHITE + format.format(value) + extra;
  }
}
