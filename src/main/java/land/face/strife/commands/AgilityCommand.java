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
package land.face.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import land.face.strife.StrifePlugin;
import org.bukkit.entity.Player;

@CommandAlias("agility")
public class AgilityCommand extends BaseCommand {

  private final StrifePlugin plugin;

  public AgilityCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("create")
  public void creationCommand(String name, double difficulty, double xp) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    boolean success = plugin.getAgilityManager()
        .createAgilityContainer(name, sender.getLocation(), (float) difficulty, (float) xp);

    if (success) {
      sendMessage(sender, "&eMAde it");
    } else {
      sendMessage(sender, "&efailed");
    }
  }

  @Subcommand("create")
  public void addCommand(String name) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    boolean success = plugin.getAgilityManager().addAgilityLocation(name, sender.getLocation());
    if (success) {
      sendMessage(sender, "&eMAde it");
    } else {
      sendMessage(sender, "&efailed");
    }
  }
}
