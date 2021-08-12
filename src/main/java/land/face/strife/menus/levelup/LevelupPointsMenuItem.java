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
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LevelupPointsMenuItem extends MenuItem {

  private static final String DISPLAY_NAME = "&f&nUnused Levelpoints";
  private static final ItemStack DISPLAY_ICON = new ItemStack(Material.NETHER_STAR);
  private static final String[] DISPLAY_LORE = {
      ChatColor.GRAY + "Click attributes to upgrade them!",
      ChatColor.GRAY + "Once you're done, click this icon",
      ChatColor.GRAY + "to confirm changes!"
  };
  private static final String CLICK_TO_SAVE_TEXT = StringExtensionsKt.chatColorize(
      "&a&lClick to confirm changes!");

  private final StrifePlugin plugin;

  LevelupPointsMenuItem(StrifePlugin plugin) {
    super(StringExtensionsKt.chatColorize(DISPLAY_NAME), DISPLAY_ICON, DISPLAY_LORE);
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack itemStack = super.getFinalIcon(player);
    List<String> lore = new ArrayList<>(itemStack.getLore());

    Champion champion = plugin.getChampionManager().getChampion(player);
    int stacks = champion.getPendingUnusedStatPoints();
    String name = StringExtensionsKt.chatColorize("&f&nUnused Levelpoints (" + stacks + ")");

    if (champion.getPendingUnusedStatPoints() != champion.getUnusedStatPoints()) {
      lore.add("");
      lore.add(CLICK_TO_SAVE_TEXT);
    }

    ItemStackExtensionsKt.setDisplayName(itemStack, name);
    TextUtils.setLore(itemStack, lore);

    stacks = Math.min(stacks, 64);
    itemStack.setAmount(Math.max(1, stacks));
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillClose(true);
    if (plugin.getChampionManager().hasPendingChanges(event.getPlayer())) {
      Bukkit.getScheduler().runTaskLater(plugin, () ->
          plugin.getConfirmationMenu().open(event.getPlayer()), 1L);
    }
  }

}
