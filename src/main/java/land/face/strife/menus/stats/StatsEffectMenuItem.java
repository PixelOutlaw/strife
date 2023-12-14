/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.menus.stats;

import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.stats.StrifeTrait;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsEffectMenuItem extends MenuItem {

  private final StatsMenu statsMenu;
  private Map<Player, ItemStack> cachedIcon = new HashMap<>();

  StatsEffectMenuItem(StatsMenu statsMenu) {
    super(StringExtensionsKt.chatColorize("&6&lAdditional Effects"), new ItemStack(Material.BARRIER));
    this.statsMenu = statsMenu;
    ItemStackExtensionsKt.setCustomModelData(getIcon(), 50);
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = statsMenu.getInspectionTargetMap().get(commandSender);
    if (!player.isValid()) {
      return getIcon();
    }
    if (cachedIcon.containsKey(player)) {
      return cachedIcon.get(player);
    }
    StrifeMob pStats = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = getIcon().clone();
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(getDisplayName());
    List<String> lore = new ArrayList<>();

    lore.add(StatsMenu.breakLine);

    List<String> traitLores = new ArrayList<>();
    for (StrifeTrait trait : pStats.getTraits()) {
      if (trait.getAdditionalText().isEmpty()) {
        traitLores.add(ChatColor.YELLOW + "❂ " + trait.getName());
      }
    }

    if (!traitLores.isEmpty()) {
      lore.addAll(ListExtensionsKt.chatColorize(traitLores));
      lore.add(StatsMenu.breakLine);
    }

    List<String> abilityLores = new ArrayList<>();
    for (StrifeTrait trait : pStats.getTraits()) {
      if (!trait.getAdditionalText().isEmpty()) {
        abilityLores.add(trait.getName());
        abilityLores.addAll(trait.getAdditionalText());
      }
    }
    for (LoreAbility la : pStats.getLoreAbilities()) {
      if (la.isHide()) {
        continue;
      }
      abilityLores.add(la.getTriggerText());
      abilityLores.addAll(la.getDescription());
    }

    if (!abilityLores.isEmpty()) {
      lore.addAll(abilityLores);
    }

    lore.add(StatsMenu.breakLine);
    lore.add(StringExtensionsKt.chatColorize("&8&oTraits and effects appear here!"));
    lore.add(StringExtensionsKt.chatColorize("&8&oThey usually come from unique"));
    lore.add(StringExtensionsKt.chatColorize("&8&oitems and socket gems!"));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);

    cachedIcon.put(player, itemStack);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> cachedIcon.remove(player), 2);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

}
