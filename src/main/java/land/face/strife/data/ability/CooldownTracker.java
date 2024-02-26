package land.face.strife.data.ability;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.AbilityCooldownEvent;
import land.face.strife.events.AbilityGainChargeEvent;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CooldownTracker extends BukkitRunnable {

  private static final long TICK_TIME = 4L;

  @Getter
  private final Ability ability;
  @Getter
  private final AbilitySlot slot;
  @Getter
  private final StrifeMob holder;
  @Getter
  @Setter
  private long startTime;

  @Getter
  @Setter
  private int duration;
  @Getter
  @Setter
  private int maxDuration;
  @Getter
  @Setter
  private int chargesLeft;
  @Getter
  @Setter
  private boolean toggleState;

  @Getter
  @Setter
  private long logoutTime;

  public CooldownTracker(StrifeMob holder, Ability ability, AbilitySlot slot) {
    this.holder = holder;
    this.ability = ability;
    this.slot = slot;
    float cdReduction = 1f - StatUtil.getStat(holder, StrifeStat.COOLDOWN_REDUCTION) / 100f;
    float newCooldown = Math.max(ability.getMinCooldown(), cdReduction * ability.getCooldown());
    maxDuration = (int) (newCooldown * 20 / TICK_TIME);
    duration = maxDuration;
    chargesLeft = ability.getMaxCharges();
    startTime = System.currentTimeMillis();
    toggleState = false;
    runTaskTimer(StrifePlugin.getInstance(), 0L, TICK_TIME);
  }

  @Override
  public void run() {
    if (holder == null || holder.getEntity() == null || !holder.getEntity().isValid()) {
      LogUtil.printDebug("Cooldown cancelled due to invalid entity");
      cancel();
    }
    if (toggleState) {
      updateIcon();
      return;
    }
    reduce(1);
  }

  public void reduce(float percent) {
    if (percent > 0) {
      reduce((int) ((float) maxDuration * percent));
    } else {
      reduce((int) ((float) duration * -percent));
    }
  }

  public void reduce(long milliseconds) {
    double seconds = (double) milliseconds / 1000;
    reduce((int) (seconds * 20 / TICK_TIME));
  }

  public void reduce(int ticks) {
    duration -= ticks;
    while (duration < 1) {
      chargesLeft++;
      if (chargesLeft >= ability.getMaxCharges()) {
        if (ability.getMaxCharges() > 1) {
          AbilityGainChargeEvent e = new AbilityGainChargeEvent(holder, this);
          StrifePlugin.getInstance().getServer().getPluginManager().callEvent(e);
        }
        cancel();
        AbilityCooldownEvent e = new AbilityCooldownEvent(holder, this);
        StrifePlugin.getInstance().getServer().getPluginManager().callEvent(e);
        break;
      } else {
        AbilityGainChargeEvent e = new AbilityGainChargeEvent(holder, this);
        StrifePlugin.getInstance().getServer().getPluginManager().callEvent(e);
      }
      float cdReduction = 1f - StatUtil.getStat(holder, StrifeStat.COOLDOWN_REDUCTION) / 100f;
      float newCooldown = Math.max(ability.getMinCooldown(), cdReduction * ability.getCooldown());
      maxDuration = (int) (newCooldown * 20 / TICK_TIME);
      duration += maxDuration;
    }
    updateIcon();
  }

  public void updateIcon() {
    if (!(holder.getEntity() instanceof Player player)) {
      return;
    }
    if (ability.getAbilityIconData() == null) {
      return;
    }
    if (slot == AbilitySlot.SLOT_A || slot == AbilitySlot.SLOT_B || slot == AbilitySlot.SLOT_C
        || slot == AbilitySlot.SLOT_D) {
      if (toggleState) {
        ItemUtil.sendAbilityIconPacket(ability.getAbilityIconData().getStack(),
            player, slot.getSlotIndex(),
            1, 1, toggleState);
        return;
      }
      double percent = (double) duration / maxDuration;
      ItemUtil.sendAbilityIconPacket(ability.getAbilityIconData().getStack(), player, slot.getSlotIndex(),
          percent, 1, toggleState);
    }
  }
}
