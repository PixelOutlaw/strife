package land.face.strife.data.buff;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Getter @Setter
public class Buff extends BukkitRunnable {

  private final String id;
  private final WeakReference<StrifeMob> buffOwner;
  private final UUID source;
  private final String actionBarTag;

  private final Map<StrifeStat, Float> buffStats;
  private final Set<LoreAbility> abilities;
  private final Set<StrifeTrait> traits;
  private final int maxStacks;
  private int stacks;
  private int usesRemaining;
  private final boolean stacksMultiplyStats;
  private float secondsRemaining;
  private boolean rebuildBarrier;
  private TriggerType useType;

  public Buff(String id, StrifeMob owner, UUID source, float duration, String actionBarTag,
      Map<StrifeStat, Float> buffStats, Set<StrifeTrait> traits, Set<LoreAbility> loreAbilities, int usesRemaining,
      TriggerType useType, int maxStacks, boolean stacksMultiplyStats) {
    this.id = id;
    this.source = source;
    this.actionBarTag = actionBarTag;
    this.buffOwner = new WeakReference<>(owner);
    this.buffStats = buffStats;
    this.traits = new HashSet<>(traits);
    this.abilities = new HashSet<>(loreAbilities);
    this.stacks = 1;
    this.usesRemaining = usesRemaining;
    this.maxStacks = maxStacks;
    this.stacksMultiplyStats = stacksMultiplyStats;
    this.secondsRemaining = duration;
    this.useType = useType;
    if (rebuildBarrier) {
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
          () -> StatUtil.getStat(owner, StrifeStat.BARRIER), 0L);
    }
    runTaskTimer(StrifePlugin.getInstance(), 2L, 4L);
  }

  @Override
  public void run() {
    if (buffOwner.get() == null) {
      cancel();
      return;
    }
    /*
    if (StringUtils.isNotBlank(actionBarTag)
        && buffOwner.get().getEntity().getType() == EntityType.PLAYER) {
      String msg = TextUtils.color("&9&l" + actionBarTag +
          "&b&l" + Math.round(secondsRemaining) + "s");
      AdvancedActionBarUtil.addMessage((Player) buffOwner.get().getEntity(),
          actionBarTag + source, msg, 4);
    }
    */
    secondsRemaining -= 0.2F;
    if (secondsRemaining < 0) {
      buffOwner.get().removeBuff(this);
    }
  }

  public Map<StrifeStat, Float> getTotalStats() {
    Map<StrifeStat, Float> stackedStats = new HashMap<>(buffStats);
    if (stacks == 1) {
      return stackedStats;
    }
    stackedStats.replaceAll((stat, value) -> value * stacks);
    return stackedStats;
  }

  public void refreshBuff(double duration) {
    secondsRemaining = Math.max((float) duration, secondsRemaining);
  }

  public void bumpBuff(double duration) {
    secondsRemaining = Math.max((float) duration, secondsRemaining);
    stacks = Math.min(stacks + 1, maxStacks);
    // Bukkit.getLogger().warning(" Stacks: " + stacks + "/" + maxStacks);
  }
}
