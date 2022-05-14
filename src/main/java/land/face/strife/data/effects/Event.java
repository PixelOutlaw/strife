package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.EventEffectEvent;
import lombok.Setter;
import org.bukkit.Bukkit;

public class Event extends Effect {

  @Setter
  private Map<String, String> dataKeys = new HashMap<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    EventEffectEvent event = new EventEffectEvent(caster, target, dataKeys);
    Bukkit.getPluginManager().callEvent(event);
  }
}