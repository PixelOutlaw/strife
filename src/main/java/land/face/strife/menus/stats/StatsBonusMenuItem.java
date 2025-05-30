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

import static land.face.strife.menus.stats.StatsMenu.addStat;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsBonusMenuItem extends MenuItem {

  private final StatsMenu statsMenu;
  private final Map<Player, ItemStack> cachedIcon = new WeakHashMap<>();
  
  private final FaceColor c = FaceColor.LIGHT_GREEN;

  StatsBonusMenuItem(StatsMenu statsMenu) {
    super(StringExtensionsKt.chatColorize("&a&lDrop Modifiers"), ItemUtils.BLANK.clone());
    this.statsMenu = statsMenu;
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

    int totalCombatXp = Math.round(pStats.getStat(StrifeStat.XP_GAIN));
    lore.add(addStat(c, "Combat XP: ", totalCombatXp, "%", StatsMenu.INT_FORMAT, true));
    int running = Math.round(StrifePlugin.getInstance().getBoostManager()
        .getStats(player.getLevel()).getOrDefault(StrifeStat.XP_GAIN, 0f));
    int contributors = StrifePlugin.getInstance().getBoostManager().getContributorSize();
    int buffs = Math.round(pStats.getBuffStats().getOrDefault(StrifeStat.XP_GAIN, 0f));
    if (buffs > 0 || running > 0 || contributors > 0) {
      int fromItems = totalCombatXp - buffs - running;
      if (fromItems > 0) {
        lore.add(FaceColor.GRAY + " +" + fromItems + "% From Items");
      }
      if (buffs > 0) {
        lore.add(FaceColor.GRAY + " +" + buffs + "% From Buffs");
      }
      if (running - contributors > 0) {
        lore.add(FaceColor.GRAY + " +" + (running - contributors) + "% From Global Boosts");
      }
      if (contributors > 0) {
        lore.add(FaceColor.GRAY + " +" + contributors + "% From Contributors");
      }
      if (fromItems < 0) {
        lore.add(FaceColor.RED + " " + fromItems + "% From Items");
      }
    }

    int totalSkillXp = Math.round(pStats.getStat(StrifeStat.SKILL_XP_GAIN));
    lore.add(addStat(c, "Skill XP: ", totalSkillXp, "%", StatsMenu.INT_FORMAT, true));
    int running2 = Math.round(StrifePlugin.getInstance().getBoostManager()
        .getStats(player.getLevel()).getOrDefault(StrifeStat.SKILL_XP_GAIN, 0f));
    int boosters = StrifePlugin.getInstance().getBoostManager().getDiscordBoostSize() * 2;
    int buffs2 = Math.round(pStats.getBuffStats().getOrDefault(StrifeStat.SKILL_XP_GAIN, 0f));
    if (buffs2 > 0 || running2 > 0 || boosters > 0) {
      int skillXpFromItems = totalSkillXp - buffs2 - running2;
      if (skillXpFromItems > 0) {
        lore.add(FaceColor.GRAY + " +" + skillXpFromItems + "% From Items");
      }
      if (buffs2 > 0) {
        lore.add(FaceColor.GRAY + " +" + buffs2 + "% From Buffs");
      }
      if (running2 - boosters > 0) {
        lore.add(FaceColor.GRAY + " +" + (running2 - boosters) + "% From Global Boosts");
      }
      if (boosters > 0) {
        lore.add(FaceColor.GRAY + " +" + boosters + "% From Discord Boosters");
      }
      if (skillXpFromItems < 0) {
        lore.add(FaceColor.RED + " " + skillXpFromItems + "% From Items");
      }
    }

    lore.add(addStat(c, "Loot Bonus: ", pStats.getStat(StrifeStat.ITEM_DISCOVERY), "%", StatsMenu.INT_FORMAT, true));
    lore.add(addStat(c, "Loot Rarity: ", pStats.getStat(StrifeStat.ITEM_RARITY), "%", StatsMenu.INT_FORMAT, true));
    lore.add(addStat(c, "Bits Dropped: ", pStats.getStat(StrifeStat.GOLD_FIND), "%", StatsMenu.INT_FORMAT, true));

    lore.add(StatsMenu.breakLine);

    lore.add(StringExtensionsKt.chatColorize("&8&oUse &7&o/help &8&ofor more info!"));

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
