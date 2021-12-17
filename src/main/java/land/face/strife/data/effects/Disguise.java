package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.DisguiseUtil;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;

public class Disguise extends Effect {

  @Setter
  private me.libraryaddict.disguise.disguisetypes.Disguise disguise;
  @Setter
  private int duration = -1;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (duration > 0) {
      DisguiseUtil.applyTempDisguise(target.getEntity(), disguise, duration);
    } else {
      DisguiseAPI.disguiseToPlayers(target.getEntity(), disguise, Bukkit.getOnlinePlayers());
    }
  }
}