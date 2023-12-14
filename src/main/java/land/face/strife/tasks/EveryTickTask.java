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
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import land.face.strife.util.JumpUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EveryTickTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  private final List<GUIComponent> attackIndication = List.of(
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兰")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("共")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兲")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("关")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兴")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兵")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("其")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("具")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("典")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兹")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兺")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("养")), 23, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兼")), 23, 0, Alignment.CENTER)
  );
  private final int arrLen = attackIndication.size();
  private final GUIComponent emptyAttack = new GUIComponent("attack-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER);

  public EveryTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
    runTaskTimer(plugin, 5L, 1L);
  }

  @Override
  public void run() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.getGameMode() == GameMode.ADVENTURE) {
        boolean dead = "Graveyard".equals(p.getWorld().getName());
        GUI gui = plugin.getGuiManager().getGui(p);
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(p);

        if (mob.isInCombat()) {
          int attackProgress = (int) (plugin.getAttackSpeedManager().getRawMultiplier(p.getUniqueId()) * arrLen);
          if (attackProgress != arrLen) {
            gui.update(attackIndication.get(attackProgress));
          } else {
            gui.update(emptyAttack);
          }
        }

        float life = (float) p.getHealth();
        if (dead) {
          life = 0;
        }
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
        if (dead) {
          barrier = 0;
        }
        float percentBarrier = barrier / mob.getMaxBarrier();
        int barrierSegments = (int) (178 * percentBarrier);
        double barrierRatio = mob.getMaxBarrier() / maxLife;
        TextComponent barrierText = barrierRatio < 0.5 ?
            GuiManager.BARRIER_BAR_1.get(barrierSegments) : barrierRatio < 1.0 ?
            GuiManager.BARRIER_BAR_2.get(barrierSegments) : GuiManager.BARRIER_BAR_3.get(barrierSegments);
        gui.update(new GUIComponent("barrier-bar", barrierText, barrierSegments, -90, Alignment.LEFT));

        //int energySeperators = (int) Math.floor(maxLife / 100);

        //String hpString = plugin.getGuiManager().convertToHpDisplay((int) (life + barrier));
        //String energyString = plugin.getGuiManager().convertToEnergyDisplayFont((int) mob.getEnergy());

        //gui.update(new GUIComponent("life-display", new TextComponent(hpString), hpString.length() * 8, 0, Alignment.CENTER));
        //gui.update(new GUIComponent("energy-display", new TextComponent(energyString), energyString.length() * 8, 0, Alignment.CENTER));

        plugin.getGuiManager().updateAir(gui, p);

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
}
