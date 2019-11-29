package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.Player;

public class Food extends Effect {

  private double amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double food = amount;
    if (!(target.getEntity() instanceof Player)) {
      return;
    }
    for (StrifeStat attr : getStatMults().keySet()) {
      food += getStatMults().get(attr) * caster.getStat(attr);
    }
    ((Player) target.getEntity())
        .setFoodLevel((int) Math.min(20, ((Player) target.getEntity()).getFoodLevel() + food));
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }
}