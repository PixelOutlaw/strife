/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
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
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@CommandAlias("mount|mounts")
public class MountCommand extends BaseCommand {

  private final StrifePlugin plugin;
  private final List<String> bannedWorlds;

  public MountCommand(StrifePlugin plugin) {
    this.plugin = plugin;
    bannedWorlds = plugin.getMountsYAML().getStringList("banned-worlds");
  }

  @Default
  public void baseCommand() {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    if (plugin.getPlayerMountManager().isMounted(sender)) {
      MessageUtils.sendMessage(sender, "&e[!] Listen here you little dingus I don't know what you're trying to pull but I don't like it");
      return;
    }
    if (bannedWorlds.contains((sender.getWorld().getName()))) {
      MessageUtils.sendMessage(sender, "&e[!] Mounts cannot be summoned here!");
      return;
    }
    Material floorMaterial = sender.getLocation().clone().add(-0, -0.1, 0).getBlock().getType();
    if (!floorMaterial.isSolid()) {
      MessageUtils.sendMessage(sender, "&e[!] You can only summon mounts while on the ground!");
      return;
    }
    Material eyeMaterial = sender.getEyeLocation().getBlock().getType();
    if (!(eyeMaterial == Material.AIR || eyeMaterial == Material.CAVE_AIR)) {
      MessageUtils.sendMessage(sender, "&e[!] You cannot summon a mount here!");
      return;
    }
    Material footMaterial = sender.getLocation().clone().add(-0, 0.1, 0).getBlock().getType();
    if (footMaterial.isSolid()) {
      MessageUtils.sendMessage(sender, "&e[!] You cannot summon a mount on this block!");
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(sender);
    if (mob.isInCombat()) {
      MessageUtils.sendMessage(sender, "&e[!] You cannot summon a mount while in combat!");
      return;
    }
    plugin.getPlayerMountManager().spawnMount(sender);
  }
}
