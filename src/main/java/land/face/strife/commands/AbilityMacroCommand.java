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
package land.face.strife.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.stats.AbilitySlot;
import org.bukkit.entity.Player;

@CommandAlias("ability-macro")
public class AbilityMacroCommand extends BaseCommand {

  private final StrifePlugin plugin;

  public AbilityMacroCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("cast")
  public void baseCommand(Player sender, String slot) {
    int slotNum = NumberUtils.toInt(slot, -1) - 1;
    if (slotNum > 2) {
      sendFailureHelp(sender);
      return;
    }
    if (slotNum < 0) {
      try {
        AbilitySlot abilitySlot = AbilitySlot.valueOf(slot.toUpperCase());
        slotNum = abilitySlot.getSlotIndex();
      } catch (Exception e) {
        sendFailureHelp(sender);
        return;
      }
    }
    plugin.getAbilityIconManager().triggerAbility(sender, slotNum);
  }

  private void sendFailureHelp(Player player) {
    MessageUtils.sendMessage(player, "&cInvalid cast usage. Valid args for [slot]:");
    MessageUtils.sendMessage(player, "&cSLOT_A, SLOT_B, SLOT_C, 1, 2, 3");
  }
}
