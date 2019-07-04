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
package info.faceland.strife.menus.levelup;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.StrifeAttribute;
import java.util.ArrayList;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

public class LevelupMenuItem extends MenuItem {

  private final StrifePlugin plugin;
  private final StrifeAttribute attribute;

  private static final String breakLine = TextUtils.color("&7&m---------------------------");

  public LevelupMenuItem(StrifePlugin plugin, StrifeAttribute strifeAttribute) {
    super(TextUtils.color(strifeAttribute.getName()), new Dye().toItemStack());
    this.plugin = plugin;
    this.attribute = strifeAttribute;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    int currentPoints = champion.getPendingLevel(attribute);
    int statCap = plugin.getAttributeManager().getPendingStatCap(attribute, champion);

    ItemStack itemStack;

    if (currentPoints != 0) {
      itemStack = new Dye(attribute.getDyeColor()).toItemStack(Math.min(currentPoints, 64));
    } else {
      itemStack = new Dye(DyeColor.GRAY).toItemStack(1);
    }

    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName() + " [" + currentPoints + "/" + statCap + "]");
    if (currentPoints != statCap && champion.getPendingUnusedStatPoints() > 0) {
      itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }
    List<String> lore = new ArrayList<>();
    List<String> reqList = plugin.getAttributeManager()
        .generateRequirementString(attribute, champion, statCap);
    if (!reqList.isEmpty()) {
      lore.add(breakLine);
    }
    lore.addAll(reqList);
    lore.add(breakLine);
    for (String desc : attribute.getDescription()) {
      lore.add(TextUtils.color(desc));
    }
    lore.add(breakLine);
    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    Player player = event.getPlayer();
    Champion champion = plugin.getChampionManager().getChampion(player);
    if (champion.getPendingUnusedStatPoints() < 1) {
      return;
    }
    int currentLevel = champion.getPendingLevel(attribute);
    if (currentLevel + 1 > plugin.getAttributeManager().getPendingStatCap(attribute, champion)) {
      return;
    }
    player.playSound(player.getLocation(), attribute.getClickSound(), 1f, attribute.getClickPitch());
    champion.setPendingLevel(attribute, currentLevel + 1);
    champion.setPendingUnusedStatPoints(champion.getPendingUnusedStatPoints() - 1);
    event.setWillUpdate(true);
  }
}
