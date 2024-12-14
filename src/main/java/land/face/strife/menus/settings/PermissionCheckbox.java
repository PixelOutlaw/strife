package land.face.strife.menus.settings;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PermissionCheckbox extends MenuItem {

  private final StrifePlugin plugin;
  private final SettingsMenu menu;
  private final ItemStack description;
  private final ItemStack descriptionSelected;
  private final String permssion;
  private final boolean relogInform;

  PermissionCheckbox(StrifePlugin plugin, SettingsMenu menu,
      ItemStack description, String permission, boolean relogInform) {
    super("", new ItemStack(Material.BARRIER));
    this.plugin = plugin;
    this.menu = menu;
    this.permssion = permission;
    this.descriptionSelected = description.clone();
    this.description = description.clone();
    this.relogInform = relogInform;
    this.description.editMeta(m -> m.setItemModel(new NamespacedKey("faceland", "icons/empty")));
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (player.hasPermission(permssion)) {
      return descriptionSelected;
    } else {
      return description;
    }
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.DOUBLE_CLICK) {
      return;
    }
    if (StrifePlugin.getPermissions().has(event.getPlayer(), permssion)) {
      StrifePlugin.getPermissions().playerRemoveTransient(event.getPlayer(), permssion);
      Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
          "lp user " + event.getPlayer().getName() + " permission set " + permssion + " false");
      event.getPlayer().playSound(event.getPlayer(), Sound.UI_HUD_BUBBLE_POP, 1,1);
      Bukkit.getScheduler().runTaskLater(plugin, () -> menu.update(event.getPlayer()), 1L);
    } else {
      StrifePlugin.getPermissions().playerAddTransient(event.getPlayer(), permssion);
      Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
          "lp user " + event.getPlayer().getName() + " permission unset " + permssion);
      event.getPlayer().playSound(event.getPlayer(), Sound.UI_HUD_BUBBLE_POP, 1,1);
      Bukkit.getScheduler().runTaskLater(plugin, () -> menu.update(event.getPlayer()), 1L);
    }
    if (relogInform) {
      PaletteUtil.sendMessage(event.getPlayer(), "|yellow|You must log in again for this change to take effect!");
    }
    event.setWillUpdate(false);
  }
}
