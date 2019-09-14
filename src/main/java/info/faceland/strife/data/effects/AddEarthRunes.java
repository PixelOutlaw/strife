package info.faceland.strife.data.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import java.util.UUID;

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