package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class ItemMovementAndDropListener implements Listener {

  private StrifePlugin plugin;
  private final String NO_MOVE_ABILITY;
  private final String NO_HOTBAR_DROP;

  public ItemMovementAndDropListener(StrifePlugin plugin) {
    this.plugin = plugin;
    NO_MOVE_ABILITY = TextUtils.color(
        plugin.getSettings().getString("language.abilities.cant-move-ability", ""));
    NO_HOTBAR_DROP = TextUtils.color(
        plugin.getSettings().getString("language.generic.no-dropping-from-hotbar", ""));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onChangeHeldItem(PlayerItemHeldEvent event) {
    if (isIcon(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
          () -> plugin.getAbilityIconManager()
              .triggerAbility(event.getPlayer(), event.getNewSlot()), 1L);
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
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getChampionManager().updateEquipmentStats(plugin.getChampionManager().getChampion(event.getPlayer()));
      plugin.getStatUpdateManager().updateAttributes(event.getPlayer());
    }, 1L);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    for (int i = 0; i < 10; i++) {
      if (event.getItemDrop().getItemStack()
          .isSimilar(event.getPlayer().getInventory().getItem(i))) {
        event.setCancelled(true);
        MessageUtils.sendMessage(event.getPlayer(), NO_HOTBAR_DROP);
        return;
      }
    }
  }

  private boolean isIcon(ItemStack stack) {
    return plugin.getAbilityIconManager().isAbilityIcon(stack);
  }
}