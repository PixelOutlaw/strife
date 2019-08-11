package info.faceland.strife.listeners;

import info.faceland.strife.util.MoveUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerMove(PlayerMoveEvent event) {
    MoveUtil.setHasMoved(event.getPlayer());
  }
}
