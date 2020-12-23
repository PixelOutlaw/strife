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
package land.face.strife.menus.abilities;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.menus.BlankIcon;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class ReturnButton extends MenuItem {

  private final StrifePlugin plugin;
  private static final Map<Player, Boolean> noUseMap = new WeakHashMap<>();

  public ReturnButton(StrifePlugin plugin, Material material, String name) {
    super(StringExtensionsKt.chatColorize(name), setNameAndLore(new ItemStack(material),
        StringExtensionsKt.chatColorize(name), new ArrayList<>()));
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (noUseMap.containsKey(player)) {
      return BlankIcon.getBlankStack();
    }
    ItemStack stack = getIcon().clone();
    ItemStackExtensionsKt.addItemFlags(stack, ItemFlag.HIDE_ATTRIBUTES);
    ItemStackExtensionsKt.setDisplayName(stack, getDisplayName());
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (noUseMap.containsKey(event.getPlayer())) {
      event.setWillUpdate(false);
      event.setWillClose(false);
      return;
    }
    event.setWillClose(true);
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      if (event.getPlayer() != null && event.getPlayer().isValid()) {
        plugin.getAbilityPicker().open(event.getPlayer());
      }
    }, 2L);
  }

  public static void setBackButtonEnabled(Player player, boolean value) {
    if (value) {
      noUseMap.remove(player);
    } else {
      noUseMap.put(player, true);
    }
  }

}
