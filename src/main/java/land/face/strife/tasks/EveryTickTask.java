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
package land.face.strife.tasks;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUI;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import land.face.strife.util.JumpUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.nunnerycode.mint.MintPlugin;

public class EveryTickTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  private static final String barrierChar1 = "⑴\uF801";
  private static final String barrierChar2 = "⑵\uF801";
  private static final String barrierChar3 = "⑶\uF801";

  private final List<TextComponent> attackIndication = List.of(
      new TextComponent("码"),
      new TextComponent("੧"),
      new TextComponent("੨"),
      new TextComponent("੩"),
      new TextComponent("੪"),
      new TextComponent("੫"),
      new TextComponent("੬"),
      new TextComponent("੭"),
      new TextComponent("੮"),
      new TextComponent("੯")
  );

  private final List<TextComponent> blockLevels = List.of(
      new TextComponent("௦"),
      new TextComponent("௧"),
      new TextComponent("௨"),
      new TextComponent("௩"),
      new TextComponent("௪"),
      new TextComponent("௫"),
      new TextComponent("௬"),
      new TextComponent("௭"),
      new TextComponent("௮"),
      new TextComponent("௯")
  );

  public EveryTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    plugin.getBlockManager().tickHolograms();
    for (Player p : Bukkit.getOnlinePlayers()) {
      p.setFoodLevel(19);
      if (p.getGameMode() == GameMode.ADVENTURE) {
        GUI gui = plugin.getGuiManager().getGui(p);
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(p);

        float life = (float) p.getHealth();
        float maxLife = (float) p.getMaxHealth();
        float missingPercent = 1 - life / maxLife;
        int totalMissingSegments = (int) (178 * missingPercent);
        gui.update(new GUIComponent("missing-life", GuiManager.HP_BAR.get(totalMissingSegments),
            totalMissingSegments, 88, Alignment.RIGHT));

        float energy = mob.getEnergy();
        float missingEnergyPercent = 1 - energy / mob.getMaxEnergy();
        int missingEnergySegments = (int) (178 * missingEnergyPercent);
        gui.update(new GUIComponent("missing-energy", GuiManager.ENERGY_BAR.get(missingEnergySegments),
            missingEnergySegments, 88, Alignment.RIGHT));

        float barrier = mob.getBarrier();
        float percentBarrier = barrier / mob.getMaxBarrier();
        int barrierSegments = (int) (178 * percentBarrier);
        double barrierRatio = mob.getMaxBarrier() / maxLife;
        String barrierChar =
            barrierRatio < 0.5 ? barrierChar1 : barrierRatio < 1.0 ? barrierChar2 : barrierChar3;
        String barrierString = StringUtils.repeat(barrierChar, barrierSegments);
        gui.update(new GUIComponent("barrier-bar",
            new TextComponent(barrierString), barrierSegments, -90, Alignment.LEFT));

        String hpString = plugin.getGuiManager().convertToHpDisplay((int) (p.getHealth() + barrier));
        String energyString = plugin.getGuiManager().convertToEnergyDisplayFont((int) mob.getEnergy());

        int money = (int) MintPlugin.getInstance().getManager().getPlayerBalance(p.getUniqueId());
        String moneyString = plugin.getGuiManager().convertToMoneyFont(money);
        int gems = plugin.getPlayerPointsPlugin().getAPI().look(p.getUniqueId());
        String gemString = plugin.getGuiManager().convertToGemFont(gems);
        gui.update(new GUIComponent("life-display",
            new TextComponent(hpString), hpString.length() * 8, 1, Alignment.CENTER));
        gui.update(new GUIComponent("energy-display",
            new TextComponent(energyString), energyString.length() * 8, 1, Alignment.CENTER));
        gui.update(new GUIComponent("money-display",
            new TextComponent(moneyString), divideAndConquerLength(money) * 4, 175,
            Alignment.RIGHT));
        gui.update(new GUIComponent("gem-display",
            new TextComponent(gemString), divideAndConquerLength(gems) * 4, 227,
            Alignment.RIGHT));

        int attackProgress = (int) (10 * plugin.getAttackSpeedManager().getAttackRecharge(mob));
        if (attackProgress != 10) {
          gui.update(new GUIComponent("attack-bar", attackIndication.get(attackProgress), 22, 0, Alignment.CENTER));
        } else {
          gui.update(new GUIComponent("attack-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
        }

        double maxBlock = mob.getMaxBlock();
        if (maxBlock > 20) {
          int blockStage = 0;
          if (mob.getBlock() > 0) {
            blockStage = 1 + (int) (9 * mob.getBlock() / mob.getMaxBlock());
          }
          if (blockStage < 10) {
            gui.update(new GUIComponent("block-ind", blockLevels.get(blockStage), 20, -120,
                Alignment.CENTER));
          } else {
            gui.update(new GUIComponent("block-ind", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
          }
        } else {
          gui.update(new GUIComponent("block-ind", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
        }

        double hoverPower = JumpUtil.determineHoverPower(p);
        if (hoverPower > 0) {
          Vector bonusVelocity = p.getLocation().getDirection().clone().multiply(0.01);
          bonusVelocity.setY(bonusVelocity.getY() + hoverPower * 0.0005);
          p.setVelocity(p.getVelocity().clone().add(bonusVelocity));
        }

        plugin.getGuiManager().tickNotices(p);
      }
    }
  }

  // This ugly function is surprisingly the most efficient way
  // to determine the length of an integer...
  // https://www.baeldung.com/java-number-of-digits-in-int
  private int divideAndConquerLength(int number) {
    if (number < 100000) {
      if (number < 100) {
        if (number < 10) {
          return 1;
        } else {
          return 2;
        }
      } else {
        if (number < 1000) {
          return 3;
        } else {
          if (number < 10000) {
            return 4;
          } else {
            return 5;
          }
        }
      }
    } else {
      if (number < 10000000) {
        if (number < 1000000) {
          return 6;
        } else {
          return 7;
        }
      } else {
        if (number < 100000000) {
          return 8;
        } else {
          if (number < 1000000000) {
            return 9;
          } else {
            return 10;
          }
        }
      }
    }
  }
}
