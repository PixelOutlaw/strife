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

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.Champion;
import land.face.strife.events.AbilityCastEvent;
import land.face.strife.events.AbilityChangeEvent;
import land.face.strife.managers.AbilityIconManager;
import land.face.strife.managers.AbilityManager.AbilityType;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.ItemUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityButton extends MenuItem {

  private final StrifePlugin plugin;
  private final Ability ability;
  private final String subMenu;

  AbilityButton(StrifePlugin plugin, String subMenu, Ability ability) {
    super("", new ItemStack(ability.getCastType().getMaterial()));
    this.subMenu = subMenu;
    this.plugin = plugin;
    this.ability = ability;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack stack = ability.getAbilityIconData().getStack().clone();
    Champion champ = plugin.getChampionManager().getChampion(player);
    List<String> stackLore = plugin.getAbilityIconManager().buildRequirementsLore(champ, ability.getAbilityIconData());
    stackLore.addAll(stack.getLore() == null ? new ArrayList<>() : stack.getLore());
    TextUtils.setLore(stack, stackLore);

    if (ability.getAbilityIconData().isRequirementMet(champ)) {
      ItemStackExtensionsKt.setCustomModelData(stack, ItemUtil.getCustomData(stack));
    } else {
      ItemStackExtensionsKt.setCustomModelData(stack, 7998);
    }

    if (plugin.getAbilityIconManager().playerHasAbility(player, ability)) {
      stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    }
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
    if (!ability.getAbilityIconData().isRequirementMet(champion)) {
      sendMessage(event.getPlayer(), AbilityIconManager.ABILITY_REQ_NOT_MET);
      return;
    }
    AbilitySlot slot = ability.getAbilityIconData().getAbilitySlot();
    boolean abilityMatches = false;
    for (Ability a : champion.getAbilities().values()) {
      if (ability == a) {
        abilityMatches = true;
        break;
      }
    }
    if (abilityMatches) {
      return;
    }
    if (slot == AbilitySlot.SLOT_C) {
      SlotThreeChoiceMenu slotThreeChoiceMenu = new SlotThreeChoiceMenu(plugin, event.getPlayer(), champion, ability, subMenu);
      slotThreeChoiceMenu.open(event.getPlayer());
    } else {
      Ability oldAbility = champion.getAbilities().get(slot);
      if (oldAbility != null) {
        if (plugin.getAbilityManager().getCooldownTracker(event.getPlayer(), oldAbility.getId()) != null) {
          sendMessage(event.getPlayer(), AbilityIconManager.ABILITY_ON_COOLDOWN);
          return;
        }
      }
      champion.getSaveData().setAbility(slot, ability.getId());
      champion.getAbilities().put(slot, ability);
      plugin.getAbilityIconManager().setAbilityIcon(event.getPlayer(),
          ability.getAbilityIconData(), ability.getAbilityIconData().getAbilitySlot());

      AbilityChangeEvent abilityChangeEvent = new AbilityChangeEvent(champion, ability);
      Bukkit.getPluginManager().callEvent(abilityChangeEvent);
      plugin.getAbilityIconManager().updateChargesGui(event.getPlayer());
      event.setWillUpdate(true);
    }
  }
}
