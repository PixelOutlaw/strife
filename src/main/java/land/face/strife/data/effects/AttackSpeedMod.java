package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class AttackSpeedMod extends Effect {

  private float ratio;
  private float delaySeconds;
  private boolean hardReset;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity() instanceof Player) {
      getPlugin().getAttackSpeedManager().resetAttack(target, ratio, delaySeconds, hardReset);
    }
  }
}