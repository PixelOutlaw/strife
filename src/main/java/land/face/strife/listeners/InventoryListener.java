package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.stream.Collectors;
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

  private final StrifePlugin plugin;
  private final String NO_MOVE_ABILITY;

  public InventoryListener(StrifePlugin plugin) {
    this.plugin = plugin;
    NO_MOVE_ABILITY = PaletteUtil.color(plugin.getSettings()
        .getString("language.abilities.cant-move-ability", ""));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onChangeHeldItem(PlayerItemHeldEvent event) {
    if (isIcon(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().triggerAbility(event.getPlayer(), event.getNewSlot()), 0L);
      event.setCancelled(true);
      return;
    }
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getStrifeMobManager().updateEquipmentStats(event.getPlayer());
      plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    }, 1L);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInvyClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) {
      return;
    }
    if (event.getInventory().getHolder() instanceof MenuHolder) {
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
    if (event.isCancelled()) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getStrifeMobManager().updateEquipmentStats(event.getPlayer());
      plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    }, 1L);
  }

  @EventHandler
  public void onRightClickWhileHoldingIcon(PlayerInteractEvent event) {
    if (event.getAction() == Action.PHYSICAL) {
      return;
    }
    if (event.getAction() == Action.RIGHT_CLICK_AIR
        || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      final Player player = event.getPlayer();
      if (isIcon(event.getPlayer().getEquipment().getItemInMainHand())) {
        MessageUtils.sendMessage(player, NO_MOVE_ABILITY);
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeathDrops(PlayerDeathEvent event) {
    event.getDrops().removeAll(
        event.getDrops().stream().filter(this::isIcon).collect(Collectors.toList()));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (isIcon(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
      MessageUtils.sendMessage(event.getPlayer(), NO_MOVE_ABILITY);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerDropItem(PlayerSwapHandItemsEvent event) {
    if (isIcon(event.getMainHandItem()) || isIcon(event.getOffHandItem())) {
      event.setCancelled(true);
      MessageUtils.sendMessage(event.getPlayer(), NO_MOVE_ABILITY);
    }
  }

  private boolean isIcon(ItemStack stack) {
    return plugin.getAbilityIconManager().isAbilityIcon(stack);
  }
}
