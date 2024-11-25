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

import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Syntax;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import land.face.strife.StrifePlugin;
import land.face.strife.listeners.DeathListener;
import land.face.strife.util.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("curse")
public class CurseCommand extends BaseCommand {

  private final StrifePlugin plugin;
  private final static int CURSE_DURATION = 10 * 60 * 20;

  public CurseCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("increase")
  @CommandCompletion("@players @range:1-10")
  @Syntax("<player> <amount>")
  @CommandPermission("strife.curse")
  public void increaseCurse(CommandSender sender, OnlinePlayer target, int amount) {
    increaseCurse(target.getPlayer(), amount);
  }

  @Subcommand("decrease")
  @CommandCompletion("@players @range:1000-1010")
  @Syntax("<player> <amount>")
  @CommandPermission("strife.curse")
  public void decreaseCurse(CommandSender sender, OnlinePlayer target, int ticks) {
    decreaseCurse(target.getPlayer(), ticks);
  }

  @Subcommand("trigger")
  @CommandCompletion("@players")
  @Syntax("<player>")
  @CommandPermission("strife.curse")
  public void triggerCurse(CommandSender sender, OnlinePlayer player) {
    triggerCurse(plugin, player.getPlayer());
  }

  @Subcommand("trigger")
  @CommandCompletion("@players")
  @Syntax("<player>")
  @CommandPermission("strife.curse")
  public void triggerCurse2(CommandSender sender) {
    triggerCurse(plugin, null);
  }

  public static void increaseCurse(Player target, int amount) {
    var currentCurse = target.getPotionEffect(PotionEffectType.TRIAL_OMEN);
    var currentLevel = currentCurse == null ? 0 : currentCurse.getAmplifier();
    var newLevel = Math.min(9, currentLevel + amount);
    target.removePotionEffect(PotionEffectType.TRIAL_OMEN);
    target.addPotionEffect(new PotionEffect(PotionEffectType.TRIAL_OMEN, CURSE_DURATION, newLevel, false, false));
  }

  public static void decreaseCurse(Player target, int ticks) {
    var currentCurse = target.getPotionEffect(PotionEffectType.TRIAL_OMEN);
    if (currentCurse == null) {
      return;
    }
    var currentLevel = currentCurse.getAmplifier();
    var currentTicks = CURSE_DURATION * currentLevel + currentCurse.getDuration();
    var reducedTicks = currentTicks - ticks;

    target.removePotionEffect(PotionEffectType.TRIAL_OMEN);

    if (reducedTicks <= 0) {
      return;
    }
    var newLevel = reducedTicks / CURSE_DURATION;
    var remainderTicks = reducedTicks - (newLevel * CURSE_DURATION);
    target.addPotionEffect(new PotionEffect(PotionEffectType.TRIAL_OMEN, remainderTicks, newLevel, false, false));
  }

  public static void afflictCurseDamage(StrifePlugin plugin, Player target) {
    var currentCurse = target.getPotionEffect(PotionEffectType.TRIAL_OMEN);
    if (currentCurse == null) {
      return;
    }
    var currentLevel = 1 + currentCurse.getAmplifier();
    var maxHp = target.getMaxHealth();
    var damage = maxHp * 0.05 * currentLevel;
    var newHealth = target.getHealth() - damage;
    if (newHealth < 1) {
      newHealth = 1;
    }
    target.setHealth(newHealth);
    DamageUtil.dealRawDamage(plugin.getStrifeMobManager().getStatMob(target), 0.01f);
    decreaseCurse(target, 20 * 60);
  }

  public static void triggerCurse(StrifePlugin plugin, Player triggerer) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p == triggerer) {
        continue;
      }
      if (!p.hasPotionEffect(PotionEffectType.TRIAL_OMEN)) {
        continue;
      }
      if (DeathListener.CURSE_EXEMPT_WORLDS.contains(p.getWorld().getName())) {
        continue;
      }
      afflictCurseDamage(plugin, p);
    }
  }
}
