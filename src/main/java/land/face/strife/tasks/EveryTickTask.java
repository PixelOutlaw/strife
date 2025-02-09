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
package land.face.strife.tasks;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUI;
import com.sentropic.guiapi.gui.GUIComponent;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.JumpUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EveryTickTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  private final Map<Player, Integer> lastLifeNum = new WeakHashMap<>();
  private final Map<Player, Integer> lastEnergyNum = new WeakHashMap<>();
  private final Map<Player, Integer> lastBarrierNum = new WeakHashMap<>();
  private final Map<Player, Integer> lastOxygenNum = new WeakHashMap<>();
  private final Map<Player, Integer> lastAttackProgress = new WeakHashMap<>();

  private final List<GUIComponent> attackIndication = List.of(
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兰")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("共")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兲")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("关")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兴")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兵")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("其")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("具")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("典")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兹")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兺")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("养")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("兼")), 45, 0, Alignment.CENTER)
  );
  private final List<GUIComponent> overdrawIndicator = List.of(
      // Skip phase1 to show the bar is not empty?
      // new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("夞")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("够")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峂")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峃")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峅")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峆")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峇")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峈")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峉")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峊")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峋")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峌")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", GuiManager.noShadow(new TextComponent("峍")), 45, 0, Alignment.CENTER)
  );
  private final int arrLen = attackIndication.size();
  private final int arrLen2 = overdrawIndicator.size();
  public static final GUIComponent EMPTY_ATTACK = new GUIComponent("attack-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER);

  public EveryTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
    runTaskTimer(plugin, 5L, 1L);
  }

  @Override
  public void run() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      try {
        updateForPlayer(p);
        if (p.getGameMode() == GameMode.ADVENTURE && p.isFlying()) {
          Bukkit.getLogger().warning("[Strife] Player " + p.getName() + " is flying!!");
        }
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Strife] Failed to tick player " + p.getName());
        e.printStackTrace();
      }
    }
  }

  private void updateForPlayer(Player p) {
    if (p.getGameMode() != GameMode.ADVENTURE) {
      return;
    }
    boolean dead = "Graveyard".equals(p.getWorld().getName());
    GUI gui = plugin.getGuiManager().getGui(p);
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(p);

    if (mob.isInCombat() && !plugin.getStealthManager().isStealthed(p)) {
      float chargePercent = plugin.getAttackSpeedManager()
          .getRawMultiplier(p.getUniqueId(), mob.hasTrait(StrifeTrait.OVERDRAW));
      if (chargePercent > 1) {
        int attackProgress = (int) (((chargePercent - 1f) / 0.35f) * arrLen2);
        if (lastAttackProgress.getOrDefault(p, arrLen2) != attackProgress) {
          lastAttackProgress.put(p, attackProgress);
          if (attackProgress != arrLen2) {
            gui.update(overdrawIndicator.get(attackProgress));
          } else {
            gui.update(EMPTY_ATTACK);
          }
        }
      } else {
        int attackProgress = (int) (chargePercent * arrLen);
        if (lastAttackProgress.getOrDefault(p, arrLen) != attackProgress) {
          lastAttackProgress.put(p, attackProgress);
          if (attackProgress != arrLen) {
            gui.update(attackIndication.get(attackProgress));
          } else {
            gui.update(EMPTY_ATTACK);
          }
        }
      }
    }

    float life = (float) p.getHealth();
    if (dead) {
      life = 0;
    }
    float maxLife = (float) p.getMaxHealth();
    float missingPercent = 1 - life / maxLife;
    int totalMissingSegments = (int) (178 * missingPercent);
    if (lastLifeNum.getOrDefault(p, -1) != totalMissingSegments) {
      gui.update(GuiManager.HP_BAR.get(totalMissingSegments));
      lastLifeNum.put(p, totalMissingSegments);
    }

    float energy = mob.getEnergy();
    float missingEnergyPercent = 1 - energy / mob.getMaxEnergy();
    int missingEnergySegments = (int) (178 * missingEnergyPercent);
    if (lastEnergyNum.getOrDefault(p, -1) != missingEnergySegments) {
      gui.update(GuiManager.ENERGY_BAR.get(missingEnergySegments));
      lastEnergyNum.put(p, missingEnergySegments);
    }

    float totalMaxShielding = mob.getMaxBarrier() + mob.getMaxShielding();
    if (dead || totalMaxShielding < 0.99) {
      if (lastBarrierNum.getOrDefault(p, -1) != 0) {
        gui.update(GuiManager.BARRIER_BAR_1.get(0));
        lastBarrierNum.put(p, 0);
      }
    } else {
      float totalShielding = mob.getBarrier() + mob.getShielding();
      float percentBarrier = totalShielding / totalMaxShielding;
      int barrierSegments = (int) (178 * percentBarrier);
        /*
        Bukkit.getLogger().info("[Strife] Player: " + p.getName());
        Bukkit.getLogger().info("[Strife] Barrier percent: " + percentBarrier);
        Bukkit.getLogger().info("[Strife] Barrier: " + mob.getBarrier());
        Bukkit.getLogger().info("[Strife] shielding: " + mob.getShielding());
        Bukkit.getLogger().info("[Strife] max barrier: " + mob.getMaxBarrier());
        Bukkit.getLogger().info("[Strife] max shielding: " + mob.getMaxShielding());
        Bukkit.getLogger().info("[Strife] segments: " + barrierSegments);
        */
      if (lastBarrierNum.getOrDefault(p, -1) != barrierSegments) {
        double barrierRatio = mob.getMaxBarrier() / maxLife;
        GUIComponent barrierText = barrierRatio < 0.5 ?
            GuiManager.BARRIER_BAR_1.get(barrierSegments) : barrierRatio < 1.0 ?
            GuiManager.BARRIER_BAR_2.get(barrierSegments) : GuiManager.BARRIER_BAR_3.get(barrierSegments);
        gui.update(barrierText);
        lastBarrierNum.put(p, barrierSegments);
      }
    }

    // Energy separator logic - see history

    float percentOxygen = (float) p.getRemainingAir() / p.getMaximumAir();
    int progress = Math.max(0, Math.round(((float) GuiManager.oxygenBar.size()) * percentOxygen));
    if (lastOxygenNum.getOrDefault(p, 22) != progress) {
      plugin.getGuiManager().updateAir(gui, progress);
      lastOxygenNum.put(p, progress);
    }

    double hoverPower = JumpUtil.determineHoverPower(p);
    if (hoverPower > 0) {
      Vector bonusVelocity = p.getLocation().getDirection().clone().multiply(0.01);
      bonusVelocity.setY(bonusVelocity.getY() + hoverPower * 0.0005);
      p.setVelocity(p.getVelocity().clone().add(bonusVelocity));
    }
    /*
    int power = (p.getCurrentInput().isRight() ? -1 : 0) + (p.getCurrentInput().isLeft() ? 1 : 0);
    if (power != 0) {
      p.setVelocity(p.getVelocity().clone().rotateAroundY(power * 0.025));
    }

     */

    plugin.getGuiManager().tickNotices(p);
  }
}
