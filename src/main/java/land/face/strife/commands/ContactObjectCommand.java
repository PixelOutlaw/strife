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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreBooks;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("contact")
public class ContactObjectCommand extends BaseCommand {

  private final StrifePlugin plugin;
  private static final Set<UUID> sendPlayers = new HashSet<>();

  public ContactObjectCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Default
  public void creationCommand(CommandSender sender) {
    sender.sendMessage("contact create");
    sender.sendMessage("contact delete");
    sender.sendMessage("contact agility-coin");
  }

  @Subcommand("create")
  public void creationCommand(Player player, String id, String prefab, int cooldownTicks, String vars) {
    if (plugin.getContactObjectManager().getSpawner(id) != null) {
      player.sendMessage("alreadt exists");
      return;
    }
    if (plugin.getContactObjectManager().getPrefab(prefab) == null) {
      player.sendMessage("prefab doesnt exist");
      return;
    }
    var sneed = vars.split(" ");
    List<Integer> aaa = new ArrayList<>();
    for (var i : sneed) {
      aaa.add(Integer.parseInt(i));
    }
    plugin.getContactObjectManager().createSpawner(id,
        plugin.getContactObjectManager().getPrefab(prefab), player.getLocation(), cooldownTicks, aaa);
  }

  @Subcommand("delete")
  public void creationCommand(Player player, String id) {
    if (plugin.getContactObjectManager().getSpawner(id) == null) {
      player.sendMessage("doesn't exists");
      return;
    }
    plugin.getContactObjectManager().deleteSpawner(id);
  }

  @Subcommand("edit location")
  public void locationCommand(Player player, String id) {
    if (plugin.getContactObjectManager().getSpawner(id) == null) {
      player.sendMessage("doesn't exists");
      return;
    }
    var spawner = plugin.getContactObjectManager().getSpawner(id);
    spawner.setLocation(player.getLocation());
    spawner.destroy();
    spawner.create();
  }

  @Subcommand("agility-coin award")
  public void creationCommand(CommandSender sender, OnlinePlayer target, double coinLevel, double baseXp) {
    coinLevel = Math.max(1, coinLevel);
    double lv = PlayerDataUtil.getLifeSkillLevel(target.getPlayer(), LifeSkillType.AGILITY);
    double ratio = Math.min(1.0, coinLevel / lv);
    if (!sendPlayers.contains(target.player.getUniqueId())) {
      target.player.sendMessage(FaceColor.TEAL +
          "Woo! You got an agility token! These tokens award Agility XP and will regenerate after awhile!");
    }
    plugin.getSkillExperienceManager().addExperience(target.getPlayer(), LifeSkillType.AGILITY, baseXp * ratio, false, false);
    sendPlayers.add(target.player.getUniqueId());
  }

  @Subcommand("lore")
  public void creationCommand(CommandSender sender, String section, OnlinePlayer target, double level, double baseXp) {
    level = Math.max(1, level);
    double lv = PlayerDataUtil.getLifeSkillLevel(target.getPlayer(), LifeSkillType.LOREMASTER);
    double ratio = Math.min(1.0, level / lv);

    LoreBooks.openBook(target.player, section);

    plugin.getSkillExperienceManager().addExperience(target.getPlayer(),
        LifeSkillType.LOREMASTER, baseXp * ratio, false, false);
  }
}
