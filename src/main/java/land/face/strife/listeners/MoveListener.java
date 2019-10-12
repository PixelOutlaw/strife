package land.face.strife.listeners;

import land.face.strife.util.MoveUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerMoveHorizontally(PlayerMoveEvent event) {
    if (event.getFrom().getBlockX() != event.getTo().getBlockX()
        || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
      MoveUtil.setHasMoved(event.getPlayer());
    }
  }
}
