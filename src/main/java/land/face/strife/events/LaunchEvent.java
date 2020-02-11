package land.face.strife.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LaunchEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  private final Player player;
  private final Location location;

  public LaunchEvent(Player player, Location location) {
    this.player = player;
    this.location = location;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public Player getPlayer() {
    return player;
  }

  public Location getLocation() {
    return location;
  }
}
