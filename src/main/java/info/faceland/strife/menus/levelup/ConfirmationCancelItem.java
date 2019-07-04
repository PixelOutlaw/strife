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
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Arrays;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmationCancelItem extends MenuItem {

  private static final String DISPLAY_NAME = TextUtils.color("&c&nDiscard Changes");
  private static final ItemStack DISPLAY_ICON = new ItemStack(Material.BARRIER);
  private static final String[] DISPLAY_LORE = {
      ChatColor.GRAY + "Click here to discard",
      ChatColor.GRAY + "your attribute changes"
  };
  private final StrifePlugin plugin;

  ConfirmationCancelItem(StrifePlugin plugin) {
    super(TextUtils.color(DISPLAY_NAME), DISPLAY_ICON, DISPLAY_LORE);
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack stack = this.getIcon().clone();
    ItemStackExtensionsKt.setDisplayName(stack, DISPLAY_NAME);
    ItemStackExtensionsKt.setLore(stack, new ArrayList<>(Arrays.asList(DISPLAY_LORE)));
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillClose(true);
    plugin.getChampionManager()
        .resetPendingStats(plugin.getChampionManager().getChampion(event.getPlayer()));
  }
}
