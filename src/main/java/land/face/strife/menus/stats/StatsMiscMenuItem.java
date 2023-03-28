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
package land.face.strife.menus.stats;

import static land.face.strife.menus.stats.StatsDefenseMenuItem.PER_TEN;
import static land.face.strife.menus.stats.StatsMenu.INT_FORMAT;
import static land.face.strife.menus.stats.StatsMenu.ONE_DECIMAL;
import static land.face.strife.menus.stats.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsMiscMenuItem extends MenuItem {

  private final StatsMenu statsMenu;
  private Map<Player, ItemStack> cachedIcon = new HashMap<>();

  StatsMiscMenuItem(StatsMenu statsMenu) {
    super(FaceColor.TEAL.s() + FaceColor.BOLD.getColor() + "Miscellaneous Stats", new ItemStack(Material.BARRIER));
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
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    ItemStack itemStack = getIcon().clone();
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();
    lore.add(breakLine);
    lore.add(FaceColor.TEAL.getColor() + "Maximum Energy: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(mob.getMaxEnergy()));
    if (!mob.hasTrait(StrifeTrait.NO_ENERGY_REGEN)) {
      lore.add(FaceColor.TEAL.getColor() + "Energy Regeneration: " + FaceColor.WHITE.getColor() + ONE_DECIMAL
          .format(mob.getStat(StrifeStat.ENERGY_REGEN)) + PER_TEN);
    }
    if (mob.getStat(StrifeStat.ENERGY_ON_HIT) > 0) {
      lore.add(FaceColor.TEAL.getColor() + "Energy On Hit: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
          mob.getStat(StrifeStat.ENERGY_ON_HIT)));
    }
    if (mob.getStat(StrifeStat.ENERGY_ON_KILL) > 0) {
      lore.add(FaceColor.TEAL.getColor() + "Energy On Kill: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
          mob.getStat(StrifeStat.ENERGY_ON_KILL)));
    }
    if (mob.getStat(StrifeStat.ENERGY_WHEN_HIT) > 0) {
      lore.add(FaceColor.TEAL.getColor() + "Energy When Hit: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
          mob.getStat(StrifeStat.ENERGY_WHEN_HIT)));
    }
    lore.add(breakLine);
    lore.add(FaceColor.TEAL.getColor() + "Movement Speed: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
        mob.getStat(StrifeStat.MOVEMENT_SPEED)));
    lore.add(breakLine);
    lore.add(FaceColor.TEAL.getColor() + "Cooldown Reduction: " + FaceColor.WHITE.getColor()
        + INT_FORMAT.format(mob.getStat(StrifeStat.COOLDOWN_REDUCTION)) + "%");
    lore.add(FaceColor.TEAL.getColor() + "Effect Duration: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
        100 + mob.getStat(StrifeStat.EFFECT_DURATION)) + "%");
    lore.add(FaceColor.TEAL.getColor() + "Healing Power: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
        100 + mob.getStat(StrifeStat.HEALING_POWER)) + "%");
    lore.add(breakLine);
    lore.add(FaceColor.TEAL.getColor() + "Life From Potions: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
        100 + mob.getStat(StrifeStat.LIFE_FROM_POTIONS)) + "%");
    lore.add(FaceColor.TEAL.getColor() + "Energy From Potions: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
        100 + mob.getStat(StrifeStat.ENERGY_FROM_POTIONS)) + "%");
    lore.add(FaceColor.TEAL.getColor() + "Potion Refill Speed: " + FaceColor.WHITE.getColor() + INT_FORMAT.format(
        100 + mob.getStat(StrifeStat.POTION_REFILL)) + "%");

    lore.add(breakLine);
    if (mob.getStat(StrifeStat.DOGE) > 0) {
      lore.add(ChatColor.AQUA + "wow " + ChatColor.RED + "such stats " + ChatColor.GREEN + "many levels");
      lore.add(ChatColor.GREEN + "    amazing " + ChatColor.LIGHT_PURPLE + "    dang");
    }
    lore.add(FaceColor.TEAL.getColor() + "Crafting Skill Bonus: " + FaceColor.WHITE.getColor() + "+" +
        INT_FORMAT.format(mob.getStat(StrifeStat.CRAFT_SKILL)));
    lore.add(FaceColor.TEAL.getColor() + "Enchanting Skill Bonus: " + FaceColor.WHITE.getColor() + "+" +
        INT_FORMAT.format(mob.getStat(StrifeStat.ENCHANT_SKILL)));

    lore.add(breakLine);

    lore.add(PaletteUtil.color("|dgray||i|Use |lgray||i|/help stats |dgray||i|for info!"));

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
