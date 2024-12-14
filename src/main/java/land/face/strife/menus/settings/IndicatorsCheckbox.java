package land.face.strife.menus.settings;

import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class IndicatorsCheckbox extends MenuItem {

  private final StrifePlugin plugin;
  private final ItemStack description;
  private final ItemStack descriptionSelected;

  IndicatorsCheckbox(StrifePlugin plugin, ItemStack description) {
    super("", new ItemStack(Material.BARRIER));
    this.plugin = plugin;
    this.descriptionSelected = description.clone();
    this.description = description.clone();
    this.description.editMeta(m -> m.setItemModel(new NamespacedKey("faceland", "icons/empty")));
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    var champ = plugin.getChampionManager().getChampion(player);
    if (champ.getSaveData().isShowIndicators()) {
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
    var champ = plugin.getChampionManager().getChampion(event.getPlayer());
    if (champ.getSaveData().isShowIndicators()) {
      champ.getSaveData().setShowIndicators(false);
      event.getPlayer().playSound(event.getPlayer(), Sound.UI_HUD_BUBBLE_POP, 1,1);
    } else {
      champ.getSaveData().setShowIndicators(true);
      event.getPlayer().playSound(event.getPlayer(), Sound.UI_HUD_BUBBLE_POP, 1,1);
    }
    event.setWillUpdate(true);
  }
}
