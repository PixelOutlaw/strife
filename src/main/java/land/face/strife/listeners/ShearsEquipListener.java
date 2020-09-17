package land.face.strife.listeners;

import java.util.Objects;
import land.face.strife.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ShearsEquipListener implements Listener {

  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent e) {
    if (e.isCancelled()) {
      return;
    }
    if (Objects.requireNonNull(e.getPlayer().getInventory().getHelmet()).getType() != Material.AIR) {
      return;
    }
    if (e.useItemInHand().equals(Event.Result.DENY) || e.getAction() == Action.PHYSICAL
        || e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    if (e.getItem() == null || e.getItem().getType() != Material.SHEARS || ItemUtil.getCustomData(e.getItem()) == -1) {
      return;
    }
    if (!e.useInteractedBlock().equals(Event.Result.DENY)) {
      return;
    }
    Objects.requireNonNull(e.getPlayer().getEquipment()).setHelmet(e.getItem().clone());
    e.getItem().setType(Material.AIR);
  }
}
