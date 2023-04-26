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

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import java.util.ArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.stats.AbilitySlot;
import org.bukkit.entity.Player;

@CommandAlias("style")
public class StyleCommand extends BaseCommand {

  private final StrifePlugin plugin;

  public StyleCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("style add")
  @CommandPermission("strife.command.ability-style")
  public void addStyleCommand(OnlinePlayer player, String slot, String text) {
    AbilitySlot abilitySlot;
    try {
      abilitySlot = AbilitySlot.valueOf(slot);
    } catch (Exception e) {
      sendMessage(player.getPlayer(), "&cValid slots are: SLOT_A, SLOT_B, SLOT_C");
      return;
    }
    if (abilitySlot != AbilitySlot.SLOT_A && abilitySlot != AbilitySlot.SLOT_B
        && abilitySlot != AbilitySlot.SLOT_C && abilitySlot != AbilitySlot.SLOT_D) {
      sendMessage(player.getPlayer(), "&cValid slots are: SLOT_A, SLOT_B, SLOT_C SLOT_D");
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion((Player) player.getPlayer());
    champion.getSaveData().getCastMessages().computeIfAbsent(abilitySlot, k -> new ArrayList<>());
    champion.getSaveData().getCastMessages().get(abilitySlot).add(text);
  }

  @Subcommand("style clear")
  @CommandPermission("strife.command.ability-style")
  public void clearStyleCommand(OnlinePlayer player, String slot) {
    AbilitySlot abilitySlot;
    try {
      abilitySlot = AbilitySlot.valueOf(slot);
    } catch (Exception e) {
      sendMessage(player.getPlayer(), "&cValid slots are: SLOT_A, SLOT_B, SLOT_C");
      return;
    }
    if (abilitySlot != AbilitySlot.SLOT_A && abilitySlot != AbilitySlot.SLOT_B
        && abilitySlot != AbilitySlot.SLOT_C && abilitySlot != AbilitySlot.SLOT_D) {
      sendMessage(player.getPlayer(), "&cValid slots are: SLOT_A, SLOT_B, SLOT_C SLOT_D");
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(player.getPlayer());
    champion.getSaveData().getCastMessages().computeIfAbsent(abilitySlot, k -> new ArrayList<>());
    champion.getSaveData().getCastMessages().get(abilitySlot).clear();
  }
}