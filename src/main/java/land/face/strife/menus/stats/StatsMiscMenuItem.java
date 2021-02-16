/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.menus.stats;

import static land.face.strife.menus.stats.StatsDefenseMenuItem.PER_TEN;
import static land.face.strife.menus.stats.StatsMenu.INT_FORMAT;
import static land.face.strife.menus.stats.StatsMenu.ONE_DECIMAL;
import static land.face.strife.menus.stats.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.TextUtils;
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

public class StatsMiscMenuItem extends MenuItem {

  StatsMiscMenuItem() {
    super(TextUtils.color("&3&lMiscellaneous Stats"), new ItemStack(Material.DIAMOND_BOOTS));
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = StrifePlugin.getInstance().getStatsMenu().getTargetPlayer();
    if (!player.isValid()) {
      return getIcon();
    }
    StrifeMob pStats = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = new ItemStack(Material.DIAMOND_BOOTS);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();
    lore.add(breakLine);
    lore.add(ChatColor.DARK_AQUA + "Maximum Energy: " + ChatColor.WHITE + INT_FORMAT
        .format(StatUtil.updateMaxEnergy(pStats)));
    if (!pStats.hasTrait(StrifeTrait.NO_ENERGY_REGEN)) {
      lore.add(ChatColor.DARK_AQUA + "Energy Regeneration: " + ChatColor.WHITE + ONE_DECIMAL
          .format(pStats.getStat(StrifeStat.ENERGY_REGEN)) + PER_TEN);
    }
    if (pStats.getStat(StrifeStat.ENERGY_ON_HIT) > 0) {
      lore.add(ChatColor.DARK_AQUA + "Energy On Hit: " + ChatColor.WHITE + INT_FORMAT.format(
          pStats.getStat(StrifeStat.ENERGY_ON_HIT)));
    }
    if (pStats.getStat(StrifeStat.ENERGY_ON_KILL) > 0) {
      lore.add(ChatColor.DARK_AQUA + "Energy On Kill: " + ChatColor.WHITE + INT_FORMAT.format(
          pStats.getStat(StrifeStat.ENERGY_ON_KILL)));
    }
    if (pStats.getStat(StrifeStat.ENERGY_WHEN_HIT) > 0) {
      lore.add(ChatColor.DARK_AQUA + "Energy When Hit: " + ChatColor.WHITE + INT_FORMAT.format(
          pStats.getStat(StrifeStat.ENERGY_WHEN_HIT)));
    }
    lore.add(breakLine);
    lore.add(ChatColor.DARK_AQUA + "Movement Speed: " + ChatColor.WHITE + INT_FORMAT.format(
        pStats.getStat(StrifeStat.MOVEMENT_SPEED)));
    lore.add(breakLine);
    lore.add(ChatColor.DARK_AQUA + "Effect Duration: " + ChatColor.WHITE + INT_FORMAT.format(
        100 + pStats.getStat(StrifeStat.EFFECT_DURATION)) + "%");
    lore.add(breakLine);
    if (pStats.getStat(StrifeStat.DOGE) > 0) {
      lore.add(ChatColor.AQUA + "wow " + ChatColor.RED + "such stats " + ChatColor.GREEN + "many levels");
      lore.add(ChatColor.GREEN + "    amazing " + ChatColor.LIGHT_PURPLE + "    dang");
    }
    lore.add(ChatColor.DARK_AQUA + "Crafting Skill Bonus: " + ChatColor.WHITE + "+" +
        INT_FORMAT.format(pStats.getStat(StrifeStat.CRAFT_SKILL)));
    lore.add(ChatColor.DARK_AQUA + "Enchanting Skill Bonus: " + ChatColor.WHITE + "+" +
        INT_FORMAT.format(pStats.getStat(StrifeStat.ENCHANT_SKILL)));

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
