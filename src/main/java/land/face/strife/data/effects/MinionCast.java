package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.Ability;
import land.face.strife.util.TargetingUtil;

public class MinionCast extends Effect {

  private String uniqueId;
  private Ability ability;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (StrifeMob minion : target.getMinions()) {
      if (StringUtils.isNotBlank(uniqueId) && !uniqueId.equals(minion.getUniqueEntityId())) {
        continue;
      }
      getPlugin().getAbilityManager().execute(ability, minion, getPlugin().getStrifeMobManager()
          .getStatMob(TargetingUtil.getMobTarget(minion)));
    }
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public void setAbility(Ability ability) {
    this.ability = ability;
  }
}
