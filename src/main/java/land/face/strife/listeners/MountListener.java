package land.face.strife.listeners;

import land.face.strife.StrifePlugin;
import land.face.strife.data.LoadedMount;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class MountListener implements Listener {

  private final StrifePlugin plugin;

  public MountListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void entityDismountEvent(EntityDismountEvent event) {
    if (event.getEntity() instanceof Player) {
      plugin.getPlayerMountManager().despawn((Player) event.getEntity());
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void entityDeath(EntityDeathEvent event) {
    if (event.getEntity() instanceof Horse) {
      ((Horse) event.getEntity()).getInventory().clear();
      event.getDrops().clear();
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onHorseInteract(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof Horse) {
      event.setCancelled(true);
    }
  }
}
