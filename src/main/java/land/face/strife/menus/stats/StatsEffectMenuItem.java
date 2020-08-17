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
package land.face.strife.menus.stats;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.util.ArrayList;
import java.util.List;
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

  StatsEffectMenuItem() {
    super(TextUtils.color("&6&lAdditional Effects"), new ItemStack(Material.EMERALD));
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = StrifePlugin.getInstance().getStatsMenu().getTargetPlayer();
    if (!player.isValid()) {
      return getIcon();
    }
    StrifeMob pStats = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = new ItemStack(Material.EMERALD);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    List<String> lore = new ArrayList<>();

    lore.add(StatsMenu.breakLine);

    List<String> traitLores = new ArrayList<>();
    for (StrifeTrait trait : pStats.getChampion().getTraits()) {
      traitLores.add(ChatColor.YELLOW + "❂ " + trait.getName());
    }

    if (!traitLores.isEmpty()) {
      lore.addAll(TextUtils.color(traitLores));
      lore.add(StatsMenu.breakLine);
    }

    List<String> abilityLores = new ArrayList<>();
    for (TriggerType triggerType : pStats.getChampion().getLoreAbilities().keySet()) {
      for (LoreAbility la : pStats.getChampion().getLoreAbilities().get(triggerType)) {
        abilityLores.add(la.getTriggerText());
        abilityLores.addAll(la.getDescription());
      }
    }
    if (!abilityLores.isEmpty()) {
      lore.addAll(abilityLores);
    }

    lore.add(StatsMenu.breakLine);
    lore.add(TextUtils.color("&8&oTraits and effects appear here!"));
    lore.add(TextUtils.color("&8&oThey usually come from unique"));
    lore.add(TextUtils.color("&8&oitems and socket gems!"));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

}
