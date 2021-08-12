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
package land.face.strife.menus.levelup;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.StrifeAttribute;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LevelupMenuItem extends MenuItem {

  private final StrifePlugin plugin;
  private final StrifeAttribute attribute;

  private static final String breakLine = StringExtensionsKt.chatColorize("&7&m---------------------------");

  LevelupMenuItem(StrifePlugin plugin, StrifeAttribute strifeAttribute) {
    super(StringExtensionsKt.chatColorize(strifeAttribute.getName()), new ItemStack(Material.MAGMA_CREAM));
    this.plugin = plugin;
    this.attribute = strifeAttribute;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    int pendingPoints = champion.getPendingLevel(attribute);
    int actualPoints = champion.getAttributeLevel(attribute);
    int statCap = plugin.getAttributeManager().getPendingStatCap(attribute, champion);

    ItemStack icon = getIcon().clone();

    if (attribute.getCustomData() != -1) {
      if (champion.getAttributeLevel(attribute) == attribute.getMaxCap()) {
        ItemStackExtensionsKt.setCustomModelData(icon, attribute.getCustomData() + 1);
        icon.setAmount(attribute.getMaxCap());
      } else if (pendingPoints == 0) {
        ItemStackExtensionsKt.setCustomModelData(icon, 99);
      } else {
        ItemStackExtensionsKt.setCustomModelData(icon, attribute.getCustomData());
        icon.setAmount(pendingPoints);
      }
    }
    ItemMeta itemMeta = icon.getItemMeta();
    if (pendingPoints != statCap && champion.getPendingUnusedStatPoints() > 0) {
      itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
      itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }
    icon.setItemMeta(itemMeta);

    List<String> lore = new ArrayList<>();
    List<String> reqList = plugin.getAttributeManager()
        .generateRequirementString(attribute, champion, statCap);
    if (!reqList.isEmpty()) {
      lore.add(breakLine);
    }
    lore.addAll(reqList);
    lore.add(breakLine);
    for (String desc : attribute.getDescription()) {
      lore.add(StringExtensionsKt.chatColorize(desc));
    }
    lore.add(breakLine);

    if (pendingPoints > actualPoints) {
      String pendingPlus = ChatColor.WHITE + "(+" + (pendingPoints - actualPoints) + " Pending)";
      ItemStackExtensionsKt.setDisplayName(icon, getDisplayName() + " [" + pendingPoints + "/" + statCap + "]" + pendingPlus);
    } else {
      ItemStackExtensionsKt.setDisplayName(icon, getDisplayName() + " [" + actualPoints + "/" + statCap + "]");
    }

    TextUtils.setLore(icon, lore);

    return icon;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    Player p = event.getPlayer();
    Champion champion = plugin.getChampionManager().getChampion(p);
    event.setWillUpdate(false);
    if (event.getClickType() == ClickType.LEFT) {
      if (champion.getPendingUnusedStatPoints() < 1) {
        return;
      }
      int currentLevel = champion.getPendingLevel(attribute);
      if (currentLevel + 1 > plugin.getAttributeManager().getPendingStatCap(attribute, champion)) {
        return;
      }
      p.playSound(p.getLocation(), attribute.getClickSound(), 1f, attribute.getClickPitch());
      champion.setPendingLevel(attribute, currentLevel + 1);
      champion.setPendingUnusedStatPoints(champion.getPendingUnusedStatPoints() - 1);
      event.setWillUpdate(true);
    } else if (event.getClickType() == ClickType.RIGHT) {
      int currentLevel = champion.getPendingLevel(attribute);
      if (currentLevel == 0 || currentLevel == champion.getAttributeLevel(attribute)) {
        return;
      }
      p.playSound(p.getLocation(), Sound.UI_LOOM_TAKE_RESULT, 1f, 0.5f);
      champion.setPendingLevel(attribute, currentLevel - 1);
      plugin.getChampionManager().verifyPendingStats(champion);
      event.setWillUpdate(true);
    }
  }
}
