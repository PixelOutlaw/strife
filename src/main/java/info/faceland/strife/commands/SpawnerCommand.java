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

  @Command(identifier = "spawner create", permissions = "strife.command.spawner")
  public void creationCommand(Player sender, @Arg(name = "spawnerName") String spawnerName,
      @Arg(name = "uniqueName") String uniqueName, @Arg(name = "leashRange") double leashRange,
      @Arg(name = "respawnDelaySeconds") int respawnSecs) {

    if (plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eA spawner with the name " + spawnerName + " already exists!");
      return;
    }
    UniqueEntity uniqueEntity = plugin.getUniqueEntityManager().getLoadedUniquesMap()
        .get(uniqueName);
    if (uniqueEntity == null) {
      sendMessage(sender, "&eNo unique named " + uniqueName + " exists!");
      return;
    }

    Spawner spawner = new Spawner(uniqueEntity, sender.getLocation(), respawnSecs, leashRange);
    plugin.getSpawnerManager().addSpawner(spawnerName, spawner);
    sendMessage(sender, "&aSpawner &f" + spawnerName + " &asuccessfully added!");
  }

  @Command(identifier = "spawner delete", permissions = "strife.command.spawner")
  public void deleteSpawnerCommand(Player sender, @Arg(name = "spawnerName") String spawnerName) {
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().removeSpawner(spawnerName);
    sendMessage(sender, "&aDeleted spawner &f" + spawnerName);
  }

  @Command(identifier = "spawner list", permissions = "strife.command.spawner")
  public void spawnerList(Player sender) {
    sendMessage(sender, "&2&lList of loaded spawners:");
    StringBuilder listString = new StringBuilder();
    for (String s : plugin.getSpawnerManager().getSpawnerMap().keySet()) {
      listString.append(s).append(" ");
    }
    sendMessage(sender, "&f" + listString.toString());
  }

  @Command(identifier = "spawner info", permissions = "strife.command.spawner")
  public void spawnerInfo(Player sender, @Arg(name = "spawnerName") String spawnerName) {
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    Spawner spawner = plugin.getSpawnerManager().getSpawnerMap().get(spawnerName);
    sendMessage(sender, "&2&lSpawner info for &e&l" + spawnerName);
    sendMessage(sender, "&f&l Entity: &b" + spawner.getUniqueEntity().getId());
    sendMessage(sender, "&f&l World: &b" + spawner.getLocation().getWorld().getName());
    sendMessage(sender, "&f&l Location: &b" + spawner.getLocation().toVector().toString());
    sendMessage(sender, "&f&l Cooldown: &b" + spawner.getRespawnSeconds() + "s");
    sendMessage(sender, "&f&l Leash Range: &b" + spawner.getLeashRange());
  }

  @Command(identifier = "spawner teleport", permissions = "strife.command.spawner")
  public void spawnerTeleport(Player sender, @Arg(name = "spawnerName") String spawnerName) {
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    sender.teleport(plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).getLocation());
    sendMessage(sender, "&aTeleported to location of spawner &f" + spawnerName);
  }

  @Command(identifier = "spawner location", permissions = "strife.command.spawner")
  public void updateLocationCommand(Player sender, @Arg(name = "spawnerName") String spawnerName) {
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).setLocation(sender.getLocation());
    sendMessage(sender, "&aUpdated location of &f" + spawnerName + " &ato here.");
  }

  @Command(identifier = "spawner range", permissions = "strife.command.spawner")
  public void updateRangeCommand(Player sender, @Arg(name = "spawnerName") String spawnerName,
      @Arg(name = "leashRange") double leashRange) {
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).setLeashRange(leashRange);
    sendMessage(sender, "&aUpdated leash range of &f" + spawnerName + " &ato &f" + leashRange);
  }

}
