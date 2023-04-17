package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils.ToastStyle;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.lang.ref.WeakReference;
import java.util.Random;
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
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatCountdownTask extends BukkitRunnable {

  // TICKS EVERY 4 SERVER TICKS, 0.2s
  private static final int BUMP_TICKS = 60;
  private static final int DEATH_TICKS = 8;
  private final WeakReference<StrifeMob> parentMob;
  private WeakReference<StrifeMob> targetMob;
  private boolean pvp = false;
  private static final ItemStack combatStack = new ItemStack(Material.IRON_SWORD);
  private static final ItemStack exitStack = new ItemStack(Material.OXEYE_DAISY);
  private int ticks;

  private int lastHealthStage = -1;
  private int lastBarrierStage = -1;

  // 128 8 3
  private String healthBarBase = "ᛤ";
  private final Random random = new Random();
  private final Player player;

  private boolean targetWasAlive;

  private final BossBarManager manager;

  public CombatCountdownTask(StrifeMob parentMob, StrifeMob targetMob) {
    manager = StrifePlugin.getInstance().getBossBarManager();
    this.parentMob = new WeakReference<>(parentMob);
    this.targetMob = new WeakReference<>(targetMob);
    ticks = BUMP_TICKS;
    targetWasAlive = targetMob != null;
    player = parentMob.getEntity() instanceof Player ? (Player) parentMob.getEntity() : null;
    CombatChangeEvent cce = new CombatChangeEvent(parentMob, NewCombatState.ENTER);
    if (player != null) {
      ToastUtils.sendToast(player, "Entered Combat!", combatStack, ToastStyle.INFO);
    }
    Bukkit.getPluginManager().callEvent(cce);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null) {
      cancel();
      return;
    }

    ticks--;
    if (ticks < 1) {
      endTask(mob);
      return;
    }

    StrifeMob strifeMob = targetMob.get();
    if (strifeMob != null) {
      if (strifeMob.getEntity().getWorld() != parentMob.get().getEntity().getWorld()) {
        if ("Graveyard".equals(strifeMob.getEntity().getWorld())) {
          targetWasAlive = false;
          updateStatus(strifeMob);
          targetMob = new WeakReference<>(null);
        } else {
          clearBars();
        }
      } else {
        updateStatus(strifeMob);
        if (!targetWasAlive) {
          targetMob = new WeakReference<>(null);
        }
      }
    }
  }

  private void endTask(StrifeMob parent) {
    CombatChangeEvent cce = new CombatChangeEvent(parent, NewCombatState.EXIT);
    if (player != null) {
      ToastUtils.sendToast(player, "Exited Combat...", exitStack, ToastStyle.INFO);
      awardSkillExp(parent);
      if (targetMob.get() != null && targetMob.get().getEntity().isValid()) {
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
      if (Math.random() < 0.025) {
        title = DamageUtil.sillyDeathMsgs.get(random.nextInt(DamageUtil.sillyDeathMsgs.size()));
      } else {
        title = DamageUtil.deathMessage;
      }
      String s = healthBarBase + GuiManager.HEALTH_BAR_TARGET.get(138);
      manager.updateBar(player, 3, 0, s, 60);
      manager.updateBar(player, 2, 0, title, 60);
    }
  }

  private void clearBars() {
    if (player == null) {
      return;
    }
    manager.updateBar(player, 2, 0, "", 0);
    manager.updateBar(player, 3, 0, "", 0);
  }

  public void bump(StrifeMob mob) {
    if (mob != null) {
      targetMob = new WeakReference<>(mob);
      targetWasAlive = mob.getEntity().isValid();
    }
    ticks = BUMP_TICKS;
  }

  public void setPvp() {
    pvp = true;
  }

  public boolean isPvp() {
    return pvp;
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

  public static void awardSkillExp(StrifeMob mob) {
    Champion champion = mob.getChampion();
    if (champion == null) {
      return;
    }
    if (champion.getDetailsContainer().getExpValues() == null) {
      return;
    }
    for (LifeSkillType type : champion.getDetailsContainer().getExpValues().keySet()) {
      StrifePlugin.getInstance().getSkillExperienceManager().addExperience((Player) mob.getEntity(), type,
          champion.getDetailsContainer().getExpValues().get(type), false, false);
    }
    champion.getDetailsContainer().clearAll();
  }
}
