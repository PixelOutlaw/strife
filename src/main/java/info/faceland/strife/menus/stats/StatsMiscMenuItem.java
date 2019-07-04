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
import static info.faceland.strife.menus.stats.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import java.util.ArrayList;
import java.util.List;
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

  private final StrifePlugin plugin;
  private Player player;

  StatsMiscMenuItem(StrifePlugin plugin, Player player) {
    super(TextUtils.color("&3&lMiscellaneous Stats"), new ItemStack(Material.DIAMOND_BOOTS));
    this.player = player;
    this.plugin = plugin;
  }

  StatsMiscMenuItem(StrifePlugin plugin) {
    super(TextUtils.color("&3&lMiscellaneous Stats"), new ItemStack(Material.DIAMOND_BOOTS));
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (this.player != null) {
      player = this.player;
    }
    StrifeMob pStats = plugin.getStrifeMobManager().getAttributedEntity(player);
    ItemStack itemStack = new ItemStack(Material.DIAMOND_BOOTS);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();
    lore.add(breakLine);
    lore.add(ChatColor.DARK_AQUA + "Movement Speed: " + ChatColor.WHITE + INT_FORMAT.format(
        pStats.getAttribute(StrifeStat.MOVEMENT_SPEED)));
    lore.add(ChatColor.DARK_AQUA + "Effect Duration: " + ChatColor.WHITE + INT_FORMAT.format(
        100 + pStats.getAttribute(StrifeStat.EFFECT_DURATION)) + "%");
    if (pStats.getAttribute(StrifeStat.DOGE) > 0) {
      lore.add(ChatColor.AQUA + "wow " + ChatColor.RED + "such stats " + ChatColor.GREEN
          + "many levels");
      lore.add(ChatColor.GREEN + "    amazing " + ChatColor.LIGHT_PURPLE + "    dang");
    }
    lore.add(ChatColor.DARK_AQUA + "Crafting Skill Bonus: " + ChatColor.WHITE + "+" +
        INT_FORMAT.format(pStats.getAttribute(StrifeStat.CRAFT_SKILL)));
    lore.add(ChatColor.DARK_AQUA + "Enchanting Skill Bonus: " + ChatColor.WHITE + "+" +
        INT_FORMAT.format(pStats.getAttribute(StrifeStat.ENCHANT_SKILL)));
    lore.add(ChatColor.DARK_AQUA + "Fishing Skill Bonus: " + ChatColor.WHITE + "+" +
        INT_FORMAT.format(pStats.getAttribute(StrifeStat.FISH_SKILL)));
    lore.add(ChatColor.DARK_AQUA + "Mining Skill Bonus: " + ChatColor.WHITE + "+" +
        INT_FORMAT.format(pStats.getAttribute(StrifeStat.MINE_SKILL)));

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
