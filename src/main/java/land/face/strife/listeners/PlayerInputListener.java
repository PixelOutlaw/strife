package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.events.OpenEquipmentEvent;
import land.face.dinvy.events.PlayerInputEvent;
import land.face.dinvy.windows.equipment.EquipmentMenu;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInputListener implements Listener {

  private final StrifePlugin plugin;

  public PlayerInputListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void openEquipmentInvyEvent(OpenEquipmentEvent event) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (mob.isInCombat()) {
      PaletteUtil.sendMessage(event.getPlayer(), FaceColor.ORANGE + "You cannot change your equipment in combat!");
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void inputDrop(PlayerInputEvent event) {
    switch (event.getInputType()) {
      case DROP_ITEM -> {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
          if (mob.isInCombat()) {
            PaletteUtil.sendMessage(event.getPlayer(),
                FaceColor.ORANGE + "You cannot change your equipment in combat!");
            return;
          }
          event.getPlayer().updateInventory();
          EquipmentMenu menu = new EquipmentMenu(DeluxeInvyPlugin.getInstance());
          menu.open(event.getPlayer());
        }, 0L);
      }
      case SWAP_HAND -> {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (event.getPlayer().hasPermission("prayer.enabled")) {
            plugin.getPrayerManager().getPrayerMenu().open(event.getPlayer());
          } else {
            MessageUtils.sendMessage(event.getPlayer(),
                "&eYou cannot drop items from your hotbar. Drop items from your inventory, or use &f/trash &eor &f/trade&e.");
          }
        }, 0L);
      }
    }
  }
}