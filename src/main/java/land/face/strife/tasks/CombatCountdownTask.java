package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils.ToastStyle;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.CombatChangeEvent;
import land.face.strife.events.CombatChangeEvent.NewCombatState;
import land.face.strife.managers.BossBarManager;
import land.face.strife.managers.GuiManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatCountdownTask extends BukkitRunnable {

  // TICKS EVERY 4 SERVER TICKS, 0.2s
  private static final int BUMP_TICKS = 60;
  private static final int DEATH_TICKS = 8;
  private final StrifeMob parentMob;
  private StrifeMob targetMob;
  @Getter
  private boolean pvp = false;
  private int ticks;

  private int lastHealthStage = -1;
  private int lastBarrierStage = -1;

  // 128 8 3
  private static final String healthBarBase = "ᛤ";

  private final Player player;

  private boolean targetWasAlive;

  private final BossBarManager manager;
  private int prayerYes = 0, prayerNo = 1;

  public CombatCountdownTask(StrifeMob parentMob, StrifeMob targetMob) {
    manager = StrifePlugin.getInstance().getBossBarManager();
    this.parentMob = parentMob;
    this.targetMob = targetMob;
    ticks = BUMP_TICKS;
    targetWasAlive = targetMob != null;
    player = parentMob.getEntity() instanceof Player ? (Player) parentMob.getEntity() : null;
    CombatChangeEvent cce = new CombatChangeEvent(parentMob, NewCombatState.ENTER);
    if (player != null) {
      ToastUtils.sendToast(player, StatUtil.COMBAT_ENTER_TOAST, ItemUtils.BLANK, ToastStyle.INFO);
    }
    Bukkit.getPluginManager().callEvent(cce);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob;
    if (mob == null || mob.getEntity() == null) {
      cancel();
      return;
    }

    if (ticks % 5 == 0) {
      if (StrifePlugin.getInstance().getPrayerManager().isPrayerActive(mob.getEntity())) {
        prayerYes++;
      } else {
        prayerNo++;
      }
    }

    ticks--;
    if (ticks < 1) {
      endTask(mob);
      return;
    }

    if (targetMob == null || targetMob.getEntity() == null) {
      return;
    }

    if (targetMob.getEntity().getWorld() != mob.getEntity().getWorld()) {
      if ("Graveyard".equals(targetMob.getEntity().getWorld())) {
        targetWasAlive = false;
        updateStatus(targetMob);
        targetMob = null;
      } else {
        clearBars();
      }
    } else {
      updateStatus(targetMob);
      if (!targetWasAlive) {
        targetMob = null;
      }
    }
  }

  private void endTask(StrifeMob parent) {
    CombatChangeEvent cce = new CombatChangeEvent(parent, NewCombatState.EXIT);
    if (player != null) {
      ToastUtils.sendToast(player, StatUtil.COMBAT_EXIT_TOAST, ItemUtils.BLANK, ToastStyle.INFO);
      awardSkillExp(parent);
      StrifeMob target = targetMob;
      if (target != null && target.getEntity() != null && target.getEntity().isValid()) {
        clearBars();
      }
      StrifePlugin.getInstance().getAttackSpeedManager().wipeAttackRecord(player);
    }
    Bukkit.getPluginManager().callEvent(cce);
    cancel();
    parent.endCombat();
  }

  private void updateStatus(StrifeMob strifeMob) {
    if (player == null) {
      return;
    }
    if (strifeMob.getEntity().isValid()) {
      manager.updateBar(player, 2, 0, createBarTitle(strifeMob), 9999999);
      int hpState = (int) (138f * (strifeMob.getEntity().getHealth()) / strifeMob.getMaxLife());
      int barrierState = (int) (138f * (strifeMob.getBarrier()) / strifeMob.getMaxBarrier());
      if (hpState != lastHealthStage || barrierState != lastBarrierStage) {
        lastHealthStage = hpState;
        lastBarrierStage = barrierState;
        String s = healthBarBase + GuiManager.HEALTH_BAR_TARGET.get(138 - hpState);
        if (barrierState > 0) {
          s += GuiManager.BARRIER_BAR_TARGET.get(barrierState);
        }
        manager.updateBar(player, 3, 0, s, 9999999);
      }
    } else if (targetWasAlive) {
      targetWasAlive = false;
      ticks = Math.max(DEATH_TICKS, ticks);
      String title;
      if (StrifePlugin.RNG.nextFloat() < 0.025) {
        title = DamageUtil.sillyDeathMsgs.get(StrifePlugin.RNG.nextInt(DamageUtil.sillyDeathMsgs.size()));
      } else {
        title = DamageUtil.deathMessage;
      }
      String s = healthBarBase + GuiManager.HEALTH_BAR_TARGET.get(138);
      manager.updateBar(player, 3, 0, s, 60);
      manager.updateBar(player, 2, 0, title, 60);
    }
  }

  public void clearBars() {
    if (player == null) {
      return;
    }
    manager.updateBar(player, 2, 0, "", 0);
    manager.updateBar(player, 3, 0, "", 0);
  }

  public void bump(StrifeMob mob) {
    if (mob != null) {
      targetMob = mob;
      targetWasAlive = mob.getEntity().isValid();
    }
    ticks = BUMP_TICKS;
  }

  public void setPvp() {
    pvp = true;
  }

  private String createBarTitle(StrifeMob target) {
    String name;
    if (target.getEntity() instanceof Player) {
      name = FaceColor.WHITE + target.getEntity().getName() + FaceColor.LIGHT_GRAY + " Lv"
          + ((Player) target.getEntity()).getLevel();
    } else if (StringUtils.isNotBlank(target.getEntity().getCustomName())) {
      name = target.getEntity().getCustomName();
    } else {
      name = WordUtils.capitalizeFully(
          target.getEntity().getType().toString().replaceAll("_", " "));
    }
    name += "   ";
    if (target.getStat(StrifeStat.BARRIER) > 0) {
      name = name + FaceColor.WHITE + StrifePlugin.INT_FORMAT.format(target.getBarrier()) + "♡ ";
    }
    name = name + FaceColor.RED + StrifePlugin.INT_FORMAT.format(target.getEntity().getHealth())
        + "♡";
    if (target.getFrost() > 100) {
      name += "  " + FaceColor.CYAN + (target.getFrost() / 100) + "❄";
    }
    if (target.getCorruption() > 0.9) {
      name += "  " + FaceColor.PURPLE + (int) target.getCorruption() + "\uD83D\uDC80";
    }
    if (target.getEntity().hasPotionEffect(PotionEffectType.POISON)) {
      name += "  " + FaceColor.GREEN +
          (target.getEntity().getPotionEffect(PotionEffectType.POISON).getAmplifier() + 1) + "☠";
    }
    return name;
  }

  public void awardSkillExp(StrifeMob mob) {
    Champion champion = StrifePlugin.getInstance().getChampionManager().getChampionSoft((Player) mob.getEntity());
    if (champion == null) {
      return;
    }
    if (champion.getDetailsContainer().getExpValues() == null) {
      return;
    }
    float xpTotal = 0;
    for (LifeSkillType type : champion.getDetailsContainer().getExpValues().keySet()) {
      xpTotal += champion.getDetailsContainer().getExpValues().get(type);
    }
    // ROUGHLY 10X xp = double XP gain
    xpTotal += (float) Math.pow(champion.getDetailsContainer().getTotalExp(), 0.3f);
    // MODIFY THIS TO ADJUST
    xpTotal += 1.0f;
    xpTotal *= (float) prayerYes / (prayerYes + prayerNo);
    if (xpTotal > 1) {
      StrifePlugin.getInstance().getSkillExperienceManager().addExperience(
          (Player) mob.getEntity(), LifeSkillType.PRAYER, xpTotal, false, false);
    }
    for (LifeSkillType type : champion.getDetailsContainer().getExpValues().keySet()) {
      StrifePlugin.getInstance().getSkillExperienceManager().addExperience((Player) mob.getEntity(), type,
          champion.getDetailsContainer().getExpValues().get(type), false, false);
    }
    champion.getDetailsContainer().clearAll();
  }
}
