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
import java.util.Arrays;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.StrifeAttribute;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmationConfirmItem extends MenuItem {

  private static final String DISPLAY_NAME = "&a&nApply Attributes Changes";
  private static final ItemStack DISPLAY_ICON = new ItemStack(Material.EMERALD);
  private static final String[] DISPLAY_LORE = {
      ChatColor.GRAY + "Do you want to raise the",
      ChatColor.GRAY + "following attributes?",
      ChatColor.GRAY + ""
  };
  private final StrifePlugin plugin;

  ConfirmationConfirmItem(StrifePlugin plugin) {
    super(StringExtensionsKt.chatColorize(DISPLAY_NAME), DISPLAY_ICON, DISPLAY_LORE);
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    List<String> changesLore = new ArrayList<>(Arrays.asList(DISPLAY_LORE));
    for (StrifeAttribute strifeAttribute : plugin.getAttributeManager().getAttributes()) {
      int initial = champion.getAttributeLevel(strifeAttribute);
      int newValue = champion.getPendingLevel(strifeAttribute);
      if (initial < newValue) {
        changesLore.add(strifeAttribute.getName() + " Lv" + initial + " -> Lv" + newValue);
      }
    }
    ItemStack stack = this.getIcon().clone();
    ItemStackExtensionsKt.setDisplayName(stack, StringExtensionsKt.chatColorize(DISPLAY_NAME));
    TextUtils.setLore(stack, changesLore, true);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.getPlayer().closeInventory();
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
    plugin.getChampionManager().savePendingStats(champion);
    plugin.getChampionManager().update(event.getPlayer());
    plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
  }
}
