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

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import info.faceland.strife.data.ChampionSaveData;
import java.util.ArrayList;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsChangeHealthDisplay extends MenuItem {

  private final StrifePlugin plugin;
  private Player player;
  private int position;

  StatsChangeHealthDisplay(StrifePlugin plugin, Player player, int postion) {
    super(TextUtils.color("&c&lHealth Display Options"), new ItemStack(Material.APPLE));
    this.plugin = plugin;
    this.player = player;
    this.position = postion;
  }

  StatsChangeHealthDisplay(StrifePlugin plugin, int position) {
    super(TextUtils.color("&c&lHealth Display Options"), new ItemStack(Material.APPLE));
    this.plugin = plugin;
    this.position = position;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack itemStack = new ItemStack(Material.APPLE);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    List<String> lore = new ArrayList<>();

    lore.add(TextUtils.color("&7Click this icon to change how your"));
    lore.add(TextUtils.color("&7hearts are displayed!"));
    lore.add(TextUtils.color(" &7> 5 Health Per Heart"));
    lore.add(TextUtils.color(" &3> 10 Health Per Heart"));
    lore.add(TextUtils.color(" &7> One Heart Row"));
    lore.add(TextUtils.color(" &7> Two Heart Rows"));
    lore.add(TextUtils.color(" &7> Vanilla Health Display"));
    lore.add(TextUtils.color(""));
    lore.add(TextUtils.color("&8&oNote: May or may not actually work?"));
    lore.add(TextUtils.color("&8&oPending vanilla bugfix in 1.13!"));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
    int ordinal = champion.getSaveData().getHealthDisplayType().ordinal();
    ordinal++;
    if (ordinal == ChampionSaveData.DISPLAY_OPTIONS.length) {
      ordinal = 0;
    }
    champion.getSaveData().setHealthDisplayType(ChampionSaveData.DISPLAY_OPTIONS[ordinal]);
    plugin.getAttributeUpdateManager()
        .updateHealth(plugin.getAttributedEntityManager().getAttributedEntity(event.getPlayer()));
    plugin.getStatsMenu().setItem(position, this);
  }

}
