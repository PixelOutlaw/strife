package land.face.strife.events;

import land.face.strife.data.StrifeMob;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UniqueSpawnEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  private final StrifeMob strifeMob;

  public UniqueSpawnEvent(StrifeMob strifeMob) {
    this.strifeMob = strifeMob;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public StrifeMob getStrifeMob() {
    return strifeMob;
  }

}
