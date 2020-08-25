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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.Spawner;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.effects.TargetingComparators.SpawnerComparator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("spawner")
public class SpawnerCommand extends BaseCommand {

  private final StrifePlugin plugin;

  public SpawnerCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("create")
  @CommandCompletion("@uniques")
  @CommandPermission("strife.spawners")
  public void creationCommand(String spawnerId, @Values("@uniques") String uniqueName, int amount, double leashRange,
      int respawnSecs) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerId)) {
      sendMessage(sender, "&eA spawner with the name " + spawnerId + " already exists!");
      return;
    }
    UniqueEntity uniqueEntity = plugin.getUniqueEntityManager().getLoadedUniquesMap().get(uniqueName);
    if (uniqueEntity == null) {
      sendMessage(sender, "&eNo unique named " + uniqueName + " exists!");
      return;
    }

    Spawner spawner = new Spawner(spawnerId, uniqueEntity, uniqueName, amount, sender.getLocation(), respawnSecs, leashRange);
    plugin.getSpawnerManager().addSpawner(spawnerId, spawner);
    sendMessage(sender, "&aSpawner &f" + spawnerId + " &asuccessfully added!");
  }

  @Subcommand("delete|remove")
  @CommandCompletion("@spawners")
  @CommandPermission("strife.spawners")
  public void deleteCommand(@Values("@spawners") String spawnerName) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().removeSpawner(spawnerName);
    sendMessage(sender, "&aDeleted spawner &f" + spawnerName);
  }

  @Subcommand("list")
  @CommandPermission("strife.spawners")
  public void spawnerList() {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    sendMessage(sender, "&2&lList of loaded spawners:");
    StringBuilder listString = new StringBuilder();
    List<Spawner> spawners = new ArrayList<>(plugin.getSpawnerManager().getSpawnerMap().values());
    spawners.removeIf(s -> s.getLocation().getWorld() != sender.getWorld());
    SpawnerComparator comparator = new SpawnerComparator();
    comparator.setLoc(sender.getLocation());
    spawners.sort(comparator);
    for (Spawner s : spawners) {
      if (s.getLocation().getWorld() == sender.getLocation().getWorld()) {
        listString.append(ChatColor.WHITE).append(s.getId()).append(" ");
      } else {
        listString.append(ChatColor.GRAY).append(s.getId()).append(" ");
      }
    }
    sendMessage(sender, "&f" + listString.toString());
  }

  @Subcommand("nearest")
  @CommandPermission("strife.spawners")
  public void spawnerListNearest() {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    sendMessage(sender, "&2&lList of loaded spawners:");
    StringBuilder listString = new StringBuilder();
    List<Spawner> spawners = new ArrayList<>(plugin.getSpawnerManager().getSpawnerMap().values());
    spawners.removeIf(s -> s.getLocation().getWorld() != sender.getWorld());
    SpawnerComparator comparator = new SpawnerComparator();
    comparator.setLoc(sender.getLocation());
    spawners.sort(comparator);
    int count = 0;
    for (Spawner s : spawners) {
      if (count >= 10) {
        break;
      }
      if (s.getLocation().getWorld() == sender.getLocation().getWorld()) {
        listString.append(ChatColor.WHITE).append(s.getId()).append(" ");
      } else {
        listString.append(ChatColor.GRAY).append(s.getId()).append(" ");
      }
      count++;
    }
    sendMessage(sender, "&f" + listString.toString());
  }

  @Subcommand("info")
  @CommandCompletion("@spawners")
  @CommandPermission("strife.spawners")
  public void spawnerInfo(String spawnerName) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    Spawner spawner = plugin.getSpawnerManager().getSpawnerMap().get(spawnerName);
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    sendMessage(sender, "&2&lSpawner info for &e&l" + spawnerName);
    sendMessage(sender, "&f&l Entity: &b" + spawner.getUniqueEntity().getId());
    sendMessage(sender, "&f&l World: &b" + spawner.getLocation().getWorld().getName());
    sendMessage(sender, "&f&l Location: &b" + spawner.getLocation().toVector().toString());
    sendMessage(sender, "&f&l Cooldown: &b" + spawner.getRespawnSeconds() + "s");
    sendMessage(sender, "&f&l Leash Range: &b" + spawner.getLeashRange());
  }

  @Subcommand("teleport")
  @CommandCompletion("@spawners")
  @CommandPermission("strife.spawners")
  public void spawnerTeleport(String spawnerName) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    sender.teleport(plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).getLocation());
    sendMessage(sender, "&aTeleported to location of spawner &f" + spawnerName);
  }

  @Subcommand("update location")
  @CommandCompletion("@spawners")
  @CommandPermission("strife.spawners")
  public void updateLocationCommand(String spawnerName) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).setLocation(sender.getLocation());
    sendMessage(sender, "&aUpdated location of &f" + spawnerName + " &ato here.");
  }

  @Subcommand("update amount")
  @CommandCompletion("@spawners @range:1-20")
  @CommandPermission("strife.spawners")
  public void updateLocationCommand(String spawnerName, int amount) {
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).setAmount(amount);
    sendMessage(sender, "&aUpdated amount of &f" + spawnerName + " &ato &f" + amount);
  }

  @Subcommand("update range")
  @CommandCompletion("@spawners @range:1-200")
  @CommandPermission("strife.spawners")
  public void updateRangeCommand(String spawnerName, double leashRange) {
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).setLeashRange(leashRange);
    sendMessage(sender, "&aUpdated leash range of &f" + spawnerName + " &ato &f" + leashRange);
  }

  @Subcommand("update time")
  @CommandCompletion("@spawners @range:1-200")
  @CommandPermission("strife.spawners")
  public void updateRespawnCommand(String spawnerName, int seconds) {
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (!plugin.getSpawnerManager().getSpawnerMap().containsKey(spawnerName)) {
      sendMessage(sender, "&eNo spawner with the name  " + spawnerName + " name exists!");
      return;
    }
    plugin.getSpawnerManager().getSpawnerMap().get(spawnerName).setRespawnSeconds(seconds);
    sendMessage(sender, "&aUpdated respawn time range of &f" + spawnerName + " &ato &f" + seconds);
  }

}
