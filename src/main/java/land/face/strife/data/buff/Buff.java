package land.face.strife.data.buff;

import com.tealcube.minecraft.bukkit.facecore.utilities.AdvancedActionBarUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
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
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Buff extends BukkitRunnable {

  @Getter
  private final String id;
  private final WeakReference<StrifeMob> buffOwner;
  @Getter
  private final UUID source;
  private final String actionBarTag;

  @Getter
  private final Map<StrifeStat, Float> buffStats;
  @Getter
  private final Set<LoreAbility> abilities;
  @Getter
  private final Set<StrifeTrait> traits;
  @Getter
  private final Set<TriggerType> consumeTriggers;

  @Getter
  private final int maxStacks;
  @Getter
  @Setter
  private int stacks;
  @Getter
  private final boolean stacksMultiplyStats;

  @Getter
  @Setter
  private float secondsRemaining;

  public Buff(String id, StrifeMob owner, UUID source, float duration, String actionBarTag,
      Map<StrifeStat, Float> buffStats, Set<StrifeTrait> traits, Set<LoreAbility> loreAbilities,
      Set<TriggerType> consumeTriggers, int maxStacks, boolean stacksMultiplyStats) {
    this.id = id;
    this.source = source;
    this.actionBarTag = actionBarTag;
    this.buffOwner = new WeakReference<>(owner);
    this.buffStats = buffStats;
    this.traits = new HashSet<>(traits);
    this.abilities = new HashSet<>(loreAbilities);
    this.consumeTriggers = consumeTriggers;
    this.stacks = 1;
    this.maxStacks = maxStacks;
    this.stacksMultiplyStats = stacksMultiplyStats;
    this.secondsRemaining = duration;
    runTaskTimer(StrifePlugin.getInstance(), 2L, 4L);
  }

  @Override
  public void run() {
    if (buffOwner.get() == null) {
      cancel();
      return;
    }
    if (StringUtils.isNotBlank(actionBarTag)
        && buffOwner.get().getEntity().getType() == EntityType.PLAYER) {
      String msg = TextUtils.color("&9&l" + actionBarTag +
          "&b&l" + Math.round(secondsRemaining) + "s");
      AdvancedActionBarUtil.addMessage((Player) buffOwner.get().getEntity(),
          actionBarTag + source, msg, 4);
    }
    secondsRemaining -= 0.2;
    if (secondsRemaining < 0) {
      // This also cancels the buff!
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
    //Bukkit.getLogger().warning(" Stacks: " + stacks + "/" + maxStacks);
  }
}
