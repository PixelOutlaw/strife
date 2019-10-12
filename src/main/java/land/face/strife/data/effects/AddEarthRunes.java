package land.face.strife.data.effects;

import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;

public class AddEarthRunes extends Effect {

  private int amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    UUID uuid = target.getEntity().getUniqueId();
    int runes = StrifePlugin.getInstance().getBlockManager().getEarthRunes(uuid);
    StrifePlugin.getInstance().getBlockManager().setEarthRunes(target,runes + amount);
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }
}