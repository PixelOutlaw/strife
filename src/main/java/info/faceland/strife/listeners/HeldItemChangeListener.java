package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class HeldItemChangeListener implements Listener {

  private StrifePlugin plugin;
  private final String NO_MOVE_ABILITY;

  public HeldItemChangeListener(StrifePlugin plugin) {
    this.plugin = plugin;
    NO_MOVE_ABILITY = TextUtils
        .color(plugin.getSettings().getString("language.abilities.cant-move-ability", ""));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onChangeHeldItem(PlayerItemHeldEvent event) {
    if (plugin.getAbilityIconManager().triggerAbility(event.getPlayer(), event.getNewSlot())) {
      event.setCancelled(true);
      return;
    }
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getChampionManager().updateEquipmentStats(event.getPlayer());
      plugin.getStatUpdateManager().updateAttributes(event.getPlayer());
    }, 1L);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInvyClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) {
      return;
    }
    if (event.getClick() == ClickType.NUMBER_KEY) {
      if (isIcon(event.getWhoClicked().getInventory().getItem(event.getHotbarButton())) || isIcon(
          event.getCurrentItem())) {
        MessageUtils.sendMessage(event.getWhoClicked(), NO_MOVE_ABILITY);
        event.setCancelled(true);
      }
      return;
    }
    if (isIcon(event.getCursor()) || isIcon(event.getCurrentItem())) {
      MessageUtils.sendMessage(event.getWhoClicked(), NO_MOVE_ABILITY);
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onChangeHeldItem(PlayerSwapHandItemsEvent event) {
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getChampionManager().updateEquipmentStats(event.getPlayer());
      plugin.getStatUpdateManager().updateAttributes(event.getPlayer());
    }, 1L);
  }

  private boolean isIcon(ItemStack stack) {
    return plugin.getAbilityIconManager().isAbilityIcon(stack);
  }
}
