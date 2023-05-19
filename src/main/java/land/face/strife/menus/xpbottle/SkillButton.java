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
package land.face.strife.menus.xpbottle;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.ItemStack;

public class SkillButton extends MenuItem {

  private final StrifePlugin plugin;
  private final LifeSkillType lifeSkillType;
  private final int amount;
  private final int bowlId;

  private final ItemStack icon;

  public SkillButton(StrifePlugin plugin, LifeSkillType lifeSkillType,
      int bowlId, int modelData, int amount) {
    super("", new ItemStack(Material.PLAYER_HEAD));
    this.plugin = plugin;
    this.lifeSkillType = lifeSkillType;
    this.amount = amount;
    this.bowlId = bowlId;
    icon = buildIcon(modelData);
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    return icon;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    boolean success = getAndRemoveMatch(event.getPlayer(), bowlId);
    if (PlayerDataUtil.getSkillLevels(event.getPlayer(), lifeSkillType, false).getLevel() >= 99) {
      event.getPlayer().sendMessage(FaceColor.YELLOW + "You're already max level in this skill!");
      return;
    }
    if (success) {
      plugin.getSkillExperienceManager()
          .addExperience(event.getPlayer(), lifeSkillType, amount, true, true);
    } else {
      event.getPlayer().sendMessage(FaceColor.YELLOW + "You don't have any more of this bottle!");
      event.getPlayer().closeInventory(Reason.PLUGIN);
    }
  }

  private ItemStack buildIcon(int modelData) {
    ItemStack stack = new ItemStack(Material.PAPER);
    ItemStackExtensionsKt.setCustomModelData(stack, modelData);
    ItemStackExtensionsKt.setDisplayName(stack, FaceColor.GREEN + "Gain " +
        lifeSkillType.getColor() + lifeSkillType.getPrettyName() + FaceColor.GREEN + " XP!");
    TextUtils.setLore(stack, PaletteUtil.color(List.of(
        "|gray|Click to gain |white|" + amount + " XP |gray|in",
        "|gray|the skill " + lifeSkillType.getColor() + lifeSkillType.getPrettyName() + "|gray|!"
    )), false);
    return stack;
  }

  private static boolean getAndRemoveMatch(Player player, int bowlId) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (item == null || item.getType() != Material.BOWL || !item.hasItemMeta()
          || item.getAmount() < 1) {
        continue;
      }
      if (ItemUtils.getModelData(item) == bowlId) {
        item.setAmount(item.getAmount() - 1);
        return true;
      }
    }
    return false;
  }
}
