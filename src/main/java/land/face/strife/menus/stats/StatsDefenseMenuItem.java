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

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsDefenseMenuItem extends MenuItem {

  private final StatsMenu statsMenu;
  public static final String PER_TEN = ChatColor.GRAY + "/10s";

  StatsDefenseMenuItem(StatsMenu statsMenu) {
    super(StringExtensionsKt.chatColorize("&e&lDefense Stats"), new ItemStack(Material.IRON_CHESTPLATE));
    this.statsMenu = statsMenu;
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = statsMenu.getInspectionTargetMap().get(commandSender);
    if (!player.isValid()) {
      return getIcon();
    }
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = new ItemStack(Material.IRON_CHESTPLATE);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();

    lore.add(breakLine);
    if (!mob.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED) && mob.getMaxBarrier() > 0) {
      lore.add(addStat("Maximum Barrier: ", StatUtil.getStat(mob, StrifeStat.BARRIER), INT_FORMAT));
      lore.add(addStat("Barrier Recharge: ", StatUtil.getBarrierPerSecond(mob) * 10, PER_TEN,
          ONE_DECIMAL));
      if (mob.getStat(StrifeStat.BARRIER_REGEN) > 0) {
        lore.add(addStat("Barrier Regeneration: ", StatUtil.getStat(mob, StrifeStat.BARRIER_REGEN), PER_TEN, TWO_DECIMAL));
      }
      lore.add(breakLine);
    }

    lore.add(addStat("Maximum Life: ", mob.getMaxLife(), INT_FORMAT));
    lore.add(addStat("Life Regeneration: ", StatUtil.getStat(mob, StrifeStat.REGENERATION), PER_TEN, TWO_DECIMAL));
    if (mob.getStat(StrifeStat.RAGE_WHEN_HIT) > 0) {
      lore.add(breakLine);
      lore.add(addStat("Maximum Rage: ", mob.getStat(StrifeStat.MAXIMUM_RAGE), INT_FORMAT));
      lore.add(addStat("Rage When Hit: ", mob.getStat(StrifeStat.RAGE_WHEN_HIT), ONE_DECIMAL));
    }
    lore.add(breakLine);
    lore.add(addStat("Armor Rating: ", StatUtil.getStat(mob, StrifeStat.ARMOR), INT_FORMAT));
    lore.add(addStat("Ward Rating: ", StatUtil.getStat(mob, StrifeStat.WARDING), INT_FORMAT));
    lore.add(addStat("Evasion Rating: ", StatUtil.getEvasion(mob), INT_FORMAT));
    if (mob.getStat(StrifeStat.BLOCK) > 0) {
      lore.add(addStat("Block Rating: ", mob.getStat(StrifeStat.BLOCK), INT_FORMAT));
    }
    if (mob.getStat(StrifeStat.DAMAGE_REFLECT) > 0) {
      lore.add(addStat("Reflected Damage: ", mob.getStat(StrifeStat.DAMAGE_REFLECT),
          INT_FORMAT));
    }
    lore.add(breakLine);
    lore.add(addStat("Fire Resistance: ", StatUtil.getStat(mob, StrifeStat.FIRE_RESIST), "%", INT_FORMAT));
    lore.add(addStat("Ice Resistance: ", StatUtil.getStat(mob, StrifeStat.ICE_RESIST), "%", INT_FORMAT));
    lore.add(addStat("Lightning Resistance: ", StatUtil.getStat(mob, StrifeStat.LIGHTNING_RESIST), "%", INT_FORMAT));
    lore.add(addStat("Earth Resistance: ", StatUtil.getStat(mob, StrifeStat.EARTH_RESIST), "%", INT_FORMAT));
    lore.add(addStat("Light Resistance: ", StatUtil.getStat(mob, StrifeStat.LIGHT_RESIST), "%", INT_FORMAT));
    lore.add(addStat("Shadow Resistance: ", StatUtil.getStat(mob, StrifeStat.DARK_RESIST), "%", INT_FORMAT));
    lore.add(breakLine);
    lore.add(StringExtensionsKt.chatColorize("&8&oUse &7&o/help stats &8&ofor info!"));

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
