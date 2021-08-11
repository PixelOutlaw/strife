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
package land.face.strife.managers;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import land.face.SnazzyPartiesPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.SkillBar;
import land.face.strife.data.StatusBar;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BossBarManager {

  private final StrifePlugin plugin;
  private final Map<Player, StatusBar> statusBars = new WeakHashMap<>();
  private final Map<Player, SkillBar> skillBars = new HashMap<>();
  private final List<String> deathMessages;
  private final int healthDuration;
  private final int skillDuration;
  private final Random random = new Random();

  public BossBarManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.deathMessages = ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.bar-title-entity-killed"));
    this.healthDuration = plugin.getSettings().getInt("config.mechanics.health-bar-duration", 200);
    this.skillDuration = plugin.getSettings().getInt("config.mechanics.skill-bar-duration", 200);
  }

  private StatusBar createHealthBar(Player player, StrifeMob target) {
    if (statusBars.containsKey(player)) {
      return statusBars.get(player);
    }
    BossBar barrierBar = makeBarrierBar();
    barrierBar.addPlayer(player);
    BossBar healthBar = makeHealthBar();
    healthBar.addPlayer(player);

    StatusBar bar = new StatusBar(target, healthBar, barrierBar);
    bar.setLifeTicks(healthDuration);
    bar.setHidden(false);
    bar.setDead(false);

    statusBars.put(player, bar);
    return bar;
  }

  private void createSkillBar(Player player) {
    if (skillBars.containsKey(player)) {
      return;
    }
    BossBar skillBar = makeSkillBar();
    skillBar.addPlayer(player);

    SkillBar bar = new SkillBar(plugin.getChampionManager().getChampion(player), skillBar);
    bar.setLifeTicks(skillDuration);

    skillBars.put(player, bar);
  }

  void pushSkillBar(Player player, LifeSkillType lifeSkillType) {
    createSkillBar(player);
    String name = lifeSkillType.getName();
    SkillBar bar = skillBars.get(player);
    int level = bar.getOwner().getSaveData().getSkillLevel(lifeSkillType);
    String xp = StrifePlugin.INT_FORMAT.format(PlayerDataUtil.getLifeSkillExpToLevel(bar.getOwner(), lifeSkillType));
    String barName = StringExtensionsKt
        .chatColorize("&f" + name + " Lv" + level + " &8- " + "&f(&a" + xp + "xp to " + (level + 1) + "&f)");
    bar.getSkillBar().setTitle(barName);
    bar.setLifeTicks(skillDuration);
    bar.setLifeSkillType(lifeSkillType);
    bar.getSkillBar().setVisible(true);
    updateSkillBar(bar);
  }

  public void pushBar(Player player, StrifeMob target) {
    StatusBar bar = statusBars.get(player);
    if (bar == null) {
      createHealthBar(player, target);
    }
    bar = statusBars.get(player);
    bar.setTarget(target);
    bar.setLifeTicks(healthDuration);
    bar.setHidden(false);
    bar.setDead(false);
    if (plugin.getChampionManager().getChampion(player).getSaveData().isGlowEnabled()) {
      if (target.getEntity() instanceof Player && SnazzyPartiesPlugin.getInstance()
          .getPartyManager()
          .areInSameParty(player, (Player) target.getEntity())) {
        bar.refreshGlow(player, ChatColor.AQUA);
      } else {
        bar.refreshGlow(player, ChatColor.GOLD);
      }
    }
    updateBar(bar);
  }

  public void doBarDeath(Player player) {
    if (statusBars.containsKey(player)) {
      StatusBar bossBar = statusBars.get(player);
      if (!bossBar.isDead()) {
        bossBar.setDead(true);
        updateBarTitle(bossBar, deathMessages.get(random.nextInt(deathMessages.size())));
        bossBar.getBarrierBar().setProgress(0);
        bossBar.getHealthBar().setProgress(0);
        bossBar.setLifeTicks(25);
        if (plugin.getChampionManager().getChampion(player).getSaveData().isGlowEnabled()) {
          if (bossBar.getTarget() != null && bossBar.getTarget().getEntity() != null) {
            bossBar.refreshGlow(player, ChatColor.DARK_RED);
          }
        }
      }
    }
  }

  public void tickBars() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      StatusBar bar = statusBars.get(p);
      if (bar != null) {
        updateBar(bar);
      }
      SkillBar skillBar = skillBars.get(p);
      if (skillBar != null) {
        updateSkillBar(skillBar);
      }
    }
  }

  public LivingEntity getBarTarget(Player player) {
    StatusBar bar = statusBars.get(player);
    if (bar == null || bar.getTarget() == null) {
      return null;
    }
    return bar.getTarget().getEntity();
  }

  public void disableBars(Player player) {
    StatusBar bar = statusBars.get(player);
    if (bar == null || bar.getTarget() == null) {
      return;
    }
    bar.setHidden(true);
    bar.clearGlow(player);
  }

  private void updateBar(StatusBar bossBar) {
    if (bossBar.isHidden()) {
      return;
    }
    if (bossBar.getTarget() == null) {
      bossBar.setHidden(true);
      return;
    }
    bossBar.setLifeTicks(bossBar.getLifeTicks() - 1);
    if (bossBar.getLifeTicks() < 1) {
      bossBar.clearGlow(bossBar.getHealthBar().getPlayers().get(0));
      bossBar.setHidden(true);
      bossBar.setTarget(null);
      return;
    }
    if (bossBar.isDead()) {
      return;
    }
    if (bossBar.getTarget().getEntity() == null || !bossBar.getTarget().getEntity().isValid()) {
      bossBar.clearGlow(bossBar.getHealthBar().getPlayers().get(0));
      bossBar.setHidden(true);
      bossBar.setTarget(null);
      return;
    }
    updateBarTitle(bossBar, createBarTitle(bossBar.getTarget()));
    updateBarrierProgress(bossBar);
    updateHealthProgress(bossBar);
  }

  private void updateSkillBar(SkillBar skillBar) {
    if (!skillBar.getSkillBar().isVisible()) {
      return;
    }
    if (skillBar.getLifeTicks() < 1) {
      skillBar.getSkillBar().setVisible(false);
      return;
    }
    skillBar.setLifeTicks(skillBar.getLifeTicks() - 1);
    updateSkillProgress(skillBar);
  }

  private void updateHealthProgress(StatusBar bar) {
    double health = bar.getTarget().getEntity().getHealth();
    double maxHealth = bar.getTarget().getEntity().getAttribute(GENERIC_MAX_HEALTH).getValue();
    bar.getHealthBar().setProgress(Math.min(health / maxHealth, 1D));
  }

  private void updateBarrierProgress(StatusBar bar) {
    if (StatUtil.getMaximumBarrier(bar.getTarget()) < 1) {
      bar.getBarrierBar().setVisible(false);
      return;
    }
    double maxBarrier = StatUtil.getMaximumBarrier(bar.getTarget());
    bar.getBarrierBar().setProgress(Math.min(bar.getTarget().getBarrier() / maxBarrier, 1D));
  }

  private void updateSkillProgress(SkillBar bar) {
    bar.getSkillBar().setProgress(PlayerDataUtil.getSkillProgress(bar.getOwner(), bar.getLifeSkillType()));
  }

  private String createBarTitle(StrifeMob barOwner) {
    String name;
    if (barOwner.getEntity() instanceof Player) {
      name = (barOwner.getEntity().getName()) + ChatColor.GRAY + " Lv" + ((Player) barOwner.getEntity()).getLevel();
    } else if (StringUtils.isNotBlank(barOwner.getEntity().getCustomName())) {
      name = barOwner.getEntity().getCustomName();
    } else {
      name = WordUtils.capitalizeFully(barOwner.getEntity().getType().toString().replaceAll("_", " "));
    }
    name += "   ";
    if (!barOwner.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED) && barOwner.getStat(StrifeStat.BARRIER) > 0) {
      name = name + ChatColor.WHITE + StrifePlugin.INT_FORMAT.format(barOwner.getBarrier()) + "❤ ";
    }
    name = name + ChatColor.RED + StrifePlugin.INT_FORMAT.format(barOwner.getEntity().getHealth()) + "❤";
    return name;
  }

  private void updateBarTitle(StatusBar bossBar, String title) {
    if (bossBar.getBarrierBar().isVisible()) {
      bossBar.getBarrierBar().setTitle(title);
      bossBar.getHealthBar().setTitle(null);
    } else {
      bossBar.getBarrierBar().setTitle(null);
      bossBar.getHealthBar().setTitle(title);
    }
  }

  public void clearBars() {
    for (Player p : statusBars.keySet()) {
      statusBars.get(p).getBarrierBar().removeAll();
      statusBars.get(p).getHealthBar().removeAll();
    }
    statusBars.clear();
    for (Player p : skillBars.keySet()) {
      skillBars.get(p).getSkillBar().removeAll();
    }
    skillBars.clear();
  }

  private static BossBar makeSkillBar() {
    return StrifePlugin.getInstance().getServer()
        .createBossBar("skillbar", BarColor.GREEN, BarStyle.SOLID);
  }

  private static BossBar makeHealthBar() {
    return StrifePlugin.getInstance().getServer()
        .createBossBar("healthbar", BarColor.RED, BarStyle.SOLID);
  }

  private static BossBar makeBarrierBar() {
    return StrifePlugin.getInstance().getServer()
        .createBossBar("barrierBar", BarColor.WHITE, BarStyle.SOLID);
  }
}
