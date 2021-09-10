package land.face.strife.data.buff;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.Getter;
import lombok.Setter;

public class LoadedBuff {

  private final String id;
  private final Map<StrifeStat, Float> stats;
  @Getter
  private final Set<StrifeTrait> traits = new HashSet<>();
  @Getter
  private final Set<LoreAbility> loreAbilities = new HashSet<>();
  @Getter
  private final Set<TriggerType> triggers = new HashSet<>();
  @Getter
  private final String tag;
  private final int maxStacks;
  private final float seconds;

  public LoadedBuff(String id, Map<StrifeStat, Float> stats, String tag, int maxStacks, float seconds) {
    this.id = id;
    this.tag = tag;
    this.stats = stats;
    this.maxStacks = maxStacks;
    this.seconds = seconds;
  }

  public String getId() {
    return id;
  }

  public Map<StrifeStat, Float> getStats() {
    return stats;
  }

  public int getMaxStacks() {
    return maxStacks;
  }

  public double getSeconds() {
    return seconds;
  }

  public static Buff toRunningBuff(StrifeMob target, UUID source, float duration, LoadedBuff buff) {
    return new Buff(buff.id, target, source, duration, buff.tag, buff.stats, buff.traits,
        buff.loreAbilities, buff.triggers, buff.maxStacks, true);
  }

}
