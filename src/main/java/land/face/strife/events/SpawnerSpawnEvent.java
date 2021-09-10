package land.face.strife.events;

import land.face.strife.data.Spawner;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SpawnerSpawnEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  @Getter
  private final StrifeMob strifeMob;
  @Getter
  private final Spawner spawner;

  public SpawnerSpawnEvent(StrifeMob strifeMob, Spawner spawner) {
    this.strifeMob = strifeMob;
    this.spawner = spawner;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

}
