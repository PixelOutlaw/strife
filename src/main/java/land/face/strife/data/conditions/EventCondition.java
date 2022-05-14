package land.face.strife.data.conditions;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.EventConditionEvent;
import land.face.strife.events.EventEffectEvent;
import land.face.strife.util.PlayerDataUtil;
import lombok.Setter;
import org.bukkit.Bukkit;

public class EventCondition extends Condition {

  @Setter
  private Map<String, String> dataKeys = new HashMap<>();

  public boolean isMet(StrifeMob attacker, StrifeMob target) {

    EventConditionEvent event = new EventConditionEvent(attacker, target, dataKeys, false);
    Bukkit.getPluginManager().callEvent(event);

    return event.isMet();
  }
}
