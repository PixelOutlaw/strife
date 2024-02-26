package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Silence extends Effect {

  private boolean silenceAttacks;
  private boolean silenceSpells;
  private boolean strictDuration;
  private int duration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    int amount;
    if (strictDuration) {
      amount = duration;
    } else {
      amount = (int) ((float) duration * (1f + StatUtil.getStat(caster, StrifeStat.EFFECT_DURATION) / 100f));
    }
    caster.bumpGlobalCooldown(amount * 50);
    if (target.getEntity() instanceof Player) {
      if (silenceAttacks) {
        ((Player) target.getEntity()).setCooldown(Material.DIAMOND_CHESTPLATE, Math.max(duration,
            ((Player) target.getEntity()).getCooldown(Material.DIAMOND_CHESTPLATE)));
      }
      if (silenceSpells) {
        ((Player) target.getEntity()).setCooldown(Material.GOLDEN_CHESTPLATE, Math.max(duration,
            ((Player) target.getEntity()).getCooldown(Material.GOLDEN_CHESTPLATE)));
      }
    }
  }
}
