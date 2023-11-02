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

@Getter @Setter
public class LoadedBuff {

  private final String id;
  private final Map<StrifeStat, Float> stats;
  private final Set<StrifeTrait> traits = new HashSet<>();
  private final Set<LoreAbility> loreAbilities = new HashSet<>();
  private final String tag;
  private final int usesRemaining;
  private final int maxStacks;
  private final float seconds;
  private final TriggerType useType;

  public LoadedBuff(String id, Map<StrifeStat, Float> stats, String tag, int usesRemaining, TriggerType useType,
      int maxStacks, float seconds) {
    this.id = id;
    this.tag = tag;
    this.stats = stats;
    this.usesRemaining = usesRemaining;
    this.maxStacks = maxStacks;
    this.seconds = seconds;
    this.useType = useType;
  }

  public static Buff toRunningBuff(StrifeMob target, UUID source, float duration, LoadedBuff buff) {
    return new Buff(buff.id, target, source, duration, buff.tag, buff.stats, buff.traits, buff.loreAbilities,
        buff.usesRemaining, buff.useType, buff.maxStacks, true);
  }

}
