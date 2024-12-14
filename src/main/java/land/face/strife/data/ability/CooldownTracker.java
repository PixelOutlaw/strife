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

@Getter
@Setter
public class CooldownTracker extends BukkitRunnable {

  private static final long TICK_TIME = 4L;

  private final Ability ability;
  private final AbilitySlot slot;
  private final StrifeMob holder;
  private long startTime;

  private int duration;
  private int maxDuration;
  private int chargesLeft;
  private boolean toggleState;

  private long logoutTime;

  public CooldownTracker(StrifeMob holder, Ability ability, AbilitySlot slot) {
    this.holder = holder;
    this.ability = ability;
    this.slot = slot;
    float cdReduction = 1f - StatUtil.getStat(holder, StrifeStat.COOLDOWN_REDUCTION) / 100f;
    float cooldown = holder.getEntity().isSneaking() ? ability.getSneakCooldown() : ability.getCooldown();
    float newCooldown = Math.max(ability.getMinCooldown(), cdReduction * cooldown);
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
      float cooldown = holder.getEntity().isSneaking() ? ability.getSneakCooldown() : ability.getCooldown();
      float newCooldown = Math.max(ability.getMinCooldown(), cdReduction * cooldown);
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
    switch (slot) {
      case SLOT_A, SLOT_B, SLOT_C, SLOT_D -> {
        // yay
      }
      default -> {
        return;
      }
    }
    if (toggleState) {
      ItemUtil.sendAbilityIconPacket(ability.getAbilityIconData().getSimpleStack(),
          player, slot.getSlotIndex(), 1, 1, 1, toggleState
      );
      return;
    }
    double percent = (double) duration / maxDuration;
    percent = Math.max(0, Math.min(percent, 1));
    var stack = percent < 0.02 ?
        ability.getAbilityIconData().getFullStack() : ability.getAbilityIconData().getSimpleStack();
    ItemUtil.sendAbilityIconPacket(stack,
        player, slot.getSlotIndex(), percent, chargesLeft, ability.getMaxCharges(), toggleState
    );
  }
}
