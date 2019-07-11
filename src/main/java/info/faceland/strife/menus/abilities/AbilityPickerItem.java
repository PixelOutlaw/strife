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
package info.faceland.strife.menus.abilities;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AbilityIconData;
import info.faceland.strife.data.ability.Ability;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityPickerItem extends MenuItem {

  private final StrifePlugin plugin;
  private final Ability ability;

  AbilityPickerItem(StrifePlugin plugin, Ability ability) {
    super("", new ItemStack(Material.DIAMOND_CHESTPLATE));
    this.plugin = plugin;
    this.ability = ability;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack stack = ability.getAbilityIconData().getStack().clone();
    List<String> stackLore = AbilityIconData.buildRequirementsLore(
        plugin.getChampionManager().getChampion(player), ability.getAbilityIconData());
    stackLore.addAll(ItemStackExtensionsKt.getLore(stack));
    ItemStackExtensionsKt.setLore(stack, stackLore);
    if (plugin.getAbilityIconManager().playerHasAbilityIcon(player, ability)) {
      stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    }
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (plugin.getAbilityIconManager().playerHasAbilityIcon(event.getPlayer(), ability)) {
      event.getPlayer().getInventory()
          .setItem(ability.getAbilityIconData().getAbilitySlot().ordinal(), null);
      plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer());
      return;
    }
    plugin.getAbilityIconManager().setAbilityIcon(event.getPlayer(), ability);
  }
}
