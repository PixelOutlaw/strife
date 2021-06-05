package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class Food extends Effect {

  private double amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (!(target.getEntity() instanceof Player)) {
      return;
    }
    float food = applyMultipliers(caster, (float) amount);
    ((Player) target.getEntity())
        .setFoodLevel((int) Math.min(20, ((Player) target.getEntity()).getFoodLevel() + food));
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }
}