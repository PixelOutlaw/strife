package land.face.strife.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.events.LandEvent;
import land.face.strife.events.LaunchEvent;
import land.face.strife.util.MoveUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

  private Map<UUID, Boolean> groundedLastTick = new HashMap<>();

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onPlayerMoveHorizontally(PlayerMoveEvent event) {
    if (groundedLastTick.getOrDefault(event.getPlayer().getUniqueId(), true) != event.getPlayer()
        .isOnGround()) {
      if (event.getPlayer().isOnGround()) {
        LandEvent ev = new LandEvent(event.getPlayer(), event.getPlayer().getLocation());
        Bukkit.getPluginManager().callEvent(ev);
      } else {
        LaunchEvent ev = new LaunchEvent(event.getPlayer(), event.getPlayer().getLocation());
        Bukkit.getPluginManager().callEvent(ev);
      }
    }
    groundedLastTick.put(event.getPlayer().getUniqueId(), event.getPlayer().isOnGround());
    if (event.getFrom().getX() != event.getTo().getX()
        || event.getFrom().getZ() != event.getTo().getZ()) {
      MoveUtil.setLastMoved(event.getPlayer());
    }
  }
}
