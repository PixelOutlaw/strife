package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import me.libraryaddict.disguise.DisguiseAPI;

public class Undisguise extends Effect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    DisguiseAPI.undisguiseToAll(target.getEntity());
  }

}