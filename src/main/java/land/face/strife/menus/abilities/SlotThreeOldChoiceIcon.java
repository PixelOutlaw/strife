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
package land.face.strife.menus.abilities;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import land.face.strife.data.ability.Ability;
import land.face.strife.stats.AbilitySlot;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlotThreeOldChoiceIcon extends MenuItem {

  private final SlotThreeChoiceMenu menu;
  private final AbilitySlot slot;

  public SlotThreeOldChoiceIcon(SlotThreeChoiceMenu menu, AbilitySlot slot) {
    super("dum", new ItemStack(Material.BARRIER), "");
    this.menu = menu;
    this.slot = slot;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    Ability ability = menu.getChampion().getSaveData().getAbility(slot);
    if (ability == null) {
      ItemStack stack = new ItemStack(Material.BARRIER);
      ItemStackExtensionsKt.setCustomModelData(stack, 50);
      ItemStackExtensionsKt.setDisplayName(stack, FaceColor.YELLOW + "Nothing!");
      TextUtils.setLore(stack, List.of(FaceColor.LIGHT_GRAY + "You should probably pick this one!"), false);
      return stack;
    }
    return ability.getAbilityIconData().getStack().clone();
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillClose(false);
    event.setWillUpdate(false);
    event.setWillGoBack(false);
  }
}
