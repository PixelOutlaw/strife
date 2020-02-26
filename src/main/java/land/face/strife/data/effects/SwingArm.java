package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.inventory.EquipmentSlot;

public class SwingArm extends Effect {

  private EquipmentSlot slot;
  private boolean random;
  private int delay;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    EquipmentSlot trueSlot;
    if (random) {
      trueSlot = Math.random() > 0.5 ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
    } else {
      trueSlot = slot;
    }
    PlayerDataUtil.swingHand(target.getEntity(), trueSlot, delay);
  }

  public void setSlot(EquipmentSlot slot) {
    this.slot = slot;
  }

  public void setDelay(int delay) {
    this.delay = delay;
  }

  public void setRandom(boolean random) {
    this.random = random;
  }
}
