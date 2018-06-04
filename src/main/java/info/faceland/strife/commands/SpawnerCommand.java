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
package info.faceland.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Spawner;
import info.faceland.strife.data.UniqueEntity;
import org.bukkit.entity.Player;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class SpawnerCommand {

  private final StrifePlugin plugin;

  public SpawnerCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Command(identifier = "strife spawner", permissions = "strife.command.spawner")
  public void baseCommand(Player sender, @Arg(name = "spawnerName") String spawnerName,
      @Arg(name = "uniqueName") String uniqueName, @Arg(name = "leashRange") double leashRange,
      @Arg(name = "respawnDelaySeconds") int respawnSecs) {

    if (plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eA spawner with this name already exists!");
      return;
    }
    UniqueEntity uniqueEntity = plugin.getUniqueEntityManager().getLoadedUniquesMap().get(uniqueName);
    if (uniqueEntity == null) {
      sendMessage(sender, "&eNo unique named " + uniqueName + " exists!");
      return;
    }

    Spawner spawner = new Spawner(uniqueEntity, sender.getLocation(), respawnSecs, leashRange);
    plugin.getSpawnerManager().addSpawner(spawnerName, spawner);
    sendMessage(sender, "&aSpawner &f" + spawnerName + " &asuccessfully added!");
  }

}
