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

import java.util.ArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.stats.AbilitySlot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class StyleCommand {

  private final StrifePlugin plugin;

  public StyleCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Command(identifier = "strife style add", permissions = "strife.command.ability-style", onlyPlayers = true)
  public void addStyleCommand(CommandSender sender, @Arg(name = "slot") String slot,
      @Arg(name = "text") String text) {
    AbilitySlot abilitySlot;
    try {
      abilitySlot = AbilitySlot.valueOf(slot);
    } catch (Exception e) {
      sendMessage(sender, "&cValid slots are: SLOT_A, SLOT_B, SLOT_C");
      return;
    }
    if (abilitySlot != AbilitySlot.SLOT_A && abilitySlot != AbilitySlot.SLOT_B
        && abilitySlot != AbilitySlot.SLOT_C) {
      sendMessage(sender, "&cValid slots are: SLOT_A, SLOT_B, SLOT_C");
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion((Player) sender);
    champion.getSaveData().getCastMessages().computeIfAbsent(abilitySlot, k -> new ArrayList<>());
    champion.getSaveData().getCastMessages().get(abilitySlot).add(text);
  }

  @Command(identifier = "strife style clear", permissions = "strife.command.ability-style", onlyPlayers = true)
  public void clearStyleCommand(CommandSender sender, @Arg(name = "slot") String slot) {
    AbilitySlot abilitySlot;
    try {
      abilitySlot = AbilitySlot.valueOf(slot);
    } catch (Exception e) {
      sendMessage(sender, "&cValid slots are: SLOT_A, SLOT_B, SLOT_C");
      return;
    }
    if (abilitySlot != AbilitySlot.SLOT_A && abilitySlot != AbilitySlot.SLOT_B
        && abilitySlot != AbilitySlot.SLOT_C) {
      sendMessage(sender, "&cValid slots are: SLOT_A, SLOT_B, SLOT_C");
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion((Player) sender);
    champion.getSaveData().getCastMessages().computeIfAbsent(abilitySlot, k -> new ArrayList<>());
    champion.getSaveData().getCastMessages().get(abilitySlot).clear();
  }
}