package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.menus.revive.ReviveMenu;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;

public class Revive extends Effect {

  private double percentLostExpRestored;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (!(target.getEntity() instanceof Player)) {
      return;
    }
    SoulTimer soul = StrifePlugin.getInstance().getSoulManager()
        .getSoul((Player) target.getEntity());
    if (soul == null) {
      return;
    }
    getPlugin().getReviveMenu().postNewReviveData(
        target.getEntity().getUniqueId(),
        PlayerDataUtil.getName(caster.getEntity()),
        (int) (percentLostExpRestored * soul.getLostExp())
    );
    getPlugin().getReviveMenu().open((Player) target.getEntity());
  }

  public void setPercentLostExpRestored(double percentLostExpRestored) {
    this.percentLostExpRestored = percentLostExpRestored;
  }

}
