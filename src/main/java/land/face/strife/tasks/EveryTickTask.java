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
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.pojo.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
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

  private final String lifeEmptyChar = "❷\uF801";
  private final String energyEmptyChar = "❸\uF801";

  private final String barrierChar1 = "⑴\uF801";
  private final String barrierChar2 = "⑵\uF801";
  private final String barrierChar3 = "⑶\uF801";

  private final List<TextComponent> attackIndication = List.of(
      new TextComponent("੦"),
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
  private final List<TextComponent> xpBar = List.of(
      new TextComponent("⒈"),
      new TextComponent("⒉"),
      new TextComponent("⒊"),
      new TextComponent("⒋"),
      new TextComponent("⒌"),
      new TextComponent("⒍"),
      new TextComponent("⒎"),
      new TextComponent("⒏"),
      new TextComponent("⒐"),
      new TextComponent("⒑")
  );

  private final List<TextComponent> rageLevels = List.of(
      new TextComponent("\uD809\uDC15"),
      new TextComponent("\uD809\uDC16"),
      new TextComponent("\uD809\uDC17"),
      new TextComponent("\uD809\uDC18"),
      new TextComponent("\uD809\uDC19"));

  private static final TextComponent emptyText = new TextComponent("");

  private int test;

  public EveryTickTask(StrifePlugin plugin) {
    this.plugin = plugin;
    test = plugin.getConfig().getInt("config.test", 0);
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
        String missingBar = ChatColor.DARK_RED + StringUtils.repeat(lifeEmptyChar, totalMissingSegments);
        gui.update(new GUIComponent("missing-life",
            new TextComponent(missingBar + ChatColor.RESET), totalMissingSegments, 88,
            Alignment.RIGHT));

        float energy = mob.getEnergy();
        float missingEnergyPercent = 1 - energy / mob.getMaxEnergy();
        int missingEnergySegments = (int) (178 * missingEnergyPercent);
        String missingEnergyBar =
            ChatColor.GOLD + StringUtils.repeat(energyEmptyChar, missingEnergySegments);
        gui.update(new GUIComponent("missing-energy",
            new TextComponent(missingEnergyBar + ChatColor.RESET), missingEnergySegments, 88,
            Alignment.RIGHT));

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
        String originalLevelString = Integer.toString(p.getLevel());
        String levelString = plugin.getGuiManager().convertToLevelFont(p.getLevel());
        int money = (int) MintPlugin.getInstance().getManager().getPlayerBalance(p.getUniqueId());
        String moneyString = plugin.getGuiManager().convertToMoneyFont(money);
        int gems = plugin.getPlayerPointsPlugin().getAPI().look(p.getUniqueId());
        String gemString = plugin.getGuiManager().convertToGemFont(gems);
        gui.update(new GUIComponent("life-display",
            new TextComponent(hpString), hpString.length() * 8, 1, Alignment.CENTER));
        gui.update(new GUIComponent("energy-display",
            new TextComponent(energyString), energyString.length() * 8, 1, Alignment.CENTER));
        gui.update(new GUIComponent("level-display",
            new TextComponent(levelString), originalLevelString.length() * 12, -106,
            Alignment.CENTER));
        gui.update(new GUIComponent("money-display",
            new TextComponent(moneyString), divideAndConquerLength(money) * 4, 175,
            Alignment.RIGHT));
        gui.update(new GUIComponent("gem-display",
            new TextComponent(gemString), divideAndConquerLength(gems) * 4, 227,
            Alignment.RIGHT));

        int attackProgress = (int) (10 * plugin.getAttackSpeedManager().getAttackRecharge(mob));
        if (attackProgress != 10) {
          gui.update(new GUIComponent("attack-bar", attackIndication.get(attackProgress), 20, 0, Alignment.CENTER));
        } else {
          gui.update(new GUIComponent("attack-bar", emptyText, 0, 0, Alignment.CENTER));
        }

        int xpProgress = (int) (9 * p.getExp());
        gui.update(new GUIComponent("xp-base", xpBar.get(xpProgress), 15, 98, Alignment.CENTER));

        double rage = plugin.getRageManager().getRage(p);
        if (rage > 0.5) {
          int rageStage = (int) (4 * plugin.getRageManager().getRage(p) / mob.getMaxRage());
          gui.update(new GUIComponent("rage-bar", rageLevels.get(rageStage), 22, -120, Alignment.CENTER));
        } else {
          gui.update(new GUIComponent("rage-bar", emptyText, 0, 0, Alignment.CENTER));
        }

        PlayerData data = DeluxeInvyPlugin.getInstance().getPlayerManager().getPlayerData(p);
        if (data != null) {
          gui.update(new GUIComponent("dura-helmet",
              new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.HELMET), "০")),
              45, 233, Alignment.RIGHT));
          gui.update(new GUIComponent("dura-body",
              new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.BODY), "১")),
              45, 233, Alignment.RIGHT));
          gui.update(new GUIComponent("dura-legs",
              new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.LEGS), "২")),
              45, 233, Alignment.RIGHT));
          gui.update(new GUIComponent("dura-boots",
              new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.BOOTS), "৩")),
              45, 233, Alignment.RIGHT));
          gui.update(new GUIComponent("dura-weapon",
              new TextComponent(duraString(p.getEquipment().getItemInMainHand(), "৪")),
              45, 233, Alignment.RIGHT));
          gui.update(new GUIComponent("dura-offhand",
              new TextComponent(duraString(p.getEquipment().getItemInOffHand(), "৫")),
              45, 233, Alignment.RIGHT));
        }

        double hoverPower = JumpUtil.determineHoverPower(p);
        if (hoverPower > 0) {
          Vector bonusVelocity = p.getLocation().getDirection().clone().multiply(0.01);
          bonusVelocity.setY(bonusVelocity.getY() + hoverPower * 0.0005);
          p.setVelocity(p.getVelocity().clone().add(bonusVelocity));
        }
      }
    }
  }

  public static String duraString(ItemStack stack, String string) {
    if (stack == null || stack.getType().getMaxDurability() < 5) {
      return ChatColor.GRAY + string;
    }
    float percent = 1 - ((float) stack.getDurability() / stack.getType().getMaxDurability());
    if (percent > 0.6) {
      return ChatColor.WHITE + string;
    }
    if (percent > 0.4) {
      return ChatColor.YELLOW + string;
    }
    if (percent > 0.2) {
      return ChatColor.GOLD + string;
    }
    return ChatColor.DARK_RED + string;
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
