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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;

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
import net.kyori.adventure.text.Component;
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
      new GUIComponent("attack-bar", noShadow(Component.text("兰")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("共")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("兲")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("关")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("兴")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("兵")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("其")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("具")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("典")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("兹")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("兺")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("养")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("兼")), 49, 0, Alignment.CENTER)
  );
  private final List<GUIComponent> overdrawIndicator = List.of(
      // Skip phase1 to show the bar is not empty?
      // new GUIComponent("attack-bar", GuiManager.noShadow(Component.text("夞")), 45, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("够")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峂")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峃")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峅")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峆")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峇")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峈")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峉")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峊")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峋")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峌")), 49, 0, Alignment.CENTER),
      new GUIComponent("attack-bar", noShadow(Component.text("峍")), 49, 0, Alignment.CENTER)
  );
  private final int arrLen = attackIndication.size();
  private final int arrLen2 = overdrawIndicator.size();
  public static final GUIComponent EMPTY_ATTACK = new GUIComponent("attack-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER);

  public EveryTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
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
