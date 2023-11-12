package land.face.strife.menus.prayer;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.ChampionSaveData.SelectedGod;
import land.face.strife.managers.PrayerManager.Prayer;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class GodPrayerIcon extends MenuItem {

  private final Prayer prayer;
  private final int godLevel;
  private final StrifePlugin plugin;

  GodPrayerIcon(StrifePlugin plugin, Prayer prayer, int godLevel) {
    super("", new ItemStack(Material.BARRIER));
    this.plugin = plugin;
    this.godLevel = godLevel;
    this.prayer = prayer;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    SelectedGod god = mob.getChampion().getSaveData().getSelectedGod();
    ItemStack stack = new ItemStack(Material.PAPER);
    if (god == null || god == SelectedGod.NONE ||
        mob.getChampion().getSaveData().getGodLevel().getOrDefault(god, 0) < godLevel) {
      ItemStackExtensionsKt.setCustomModelData(stack, 800);
      ItemStackExtensionsKt.setDisplayName(stack, FaceColor.WHITE + "Unknown Prayer");
      TextUtils.setLore(stack, List.of("", FaceColor.GRAY + " ? ? ?", ""), false);
      return stack;
    }
    List<String> lore = new ArrayList<>();
    switch (godLevel) {
      case 2 -> {
        ItemStackExtensionsKt.setDisplayName(stack, plugin.getPrayerManager().getGodPassiveOne().get(god).getName());
        lore.addAll(plugin.getPrayerManager().getGodPassiveOne().get(god).getDescription());
      }
      case 3 -> {
        ItemStackExtensionsKt.setDisplayName(stack, plugin.getPrayerManager().getGodPassiveTwo().get(god).getName());
        lore.addAll(plugin.getPrayerManager().getGodPassiveTwo().get(god).getDescription());
      }
      case 4 -> {
        ItemStackExtensionsKt.setDisplayName(stack, plugin.getPrayerManager().getGodPassiveThree().get(god).getName());
        lore.addAll(plugin.getPrayerManager().getGodPassiveThree().get(god).getDescription());
      }
    }
    lore.add("");
    lore.add(FaceColor.WHITE + "Faith: " + (int) mob.getPrayer() + " / " + (int) mob.getMaxPrayer());
    boolean isActive = plugin.getPrayerManager().isPrayerActive(player, prayer);
    if (isActive) {
      ItemStackExtensionsKt.setCustomModelData(stack, getActiveModelData(god, godLevel - 1));
      lore.add("");
      lore.add(FaceColor.YELLOW + "Click To Deactivate!");
    } else if (plugin.getPrayerManager().getPrayerActivationCost().get(prayer) > mob.getPrayer()) {
      ItemStackExtensionsKt.setCustomModelData(stack, 801);
      lore.add("");
      lore.add(FaceColor.RED + "You don't have enough Faith!");
    } else {
      // Inactive icon resource pack data: 870-872
      ItemStackExtensionsKt.setCustomModelData(stack, 870 + godLevel - 2);
      lore.add("");
      lore.add(FaceColor.GREEN + "Click To Activate!");
    }
    TextUtils.setLore(stack, lore, false);
    return stack;
  }

  private int getActiveModelData(SelectedGod selectedGod, int level) {
    switch (selectedGod) {
      case FACEGUY -> { switch (level) {
        case 1 -> { return 873; }
        case 2 -> { return 874; }
        case 3 -> { return 875; }
      }}
      case ZEXIR -> { switch (level) {
        case 1 -> { return 879; }
        case 2 -> { return 880; }
        case 3 -> { return 881; }
      }}
      case AURORA -> { switch (level) {
        case 1 -> { return 876; }
        case 2 -> { return 877; }
        case 3 -> { return 878; }
      }}
    }
    return 800;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.DOUBLE_CLICK) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    SelectedGod god = mob.getChampion().getSaveData().getSelectedGod();
    if (god == SelectedGod.NONE || mob.getChampion().getSaveData().getGodLevel().get(god) < godLevel) {
      return;
    }
    float cost = switch (godLevel) {
      case 2 -> plugin.getPrayerManager().getGodPassiveOne().get(god).getActivationCost();
      case 3 -> plugin.getPrayerManager().getGodPassiveTwo().get(god).getActivationCost();
      case 4 -> plugin.getPrayerManager().getGodPassiveThree().get(god).getActivationCost();
      default -> 10;
    };
    if (cost > mob.getPrayer()) {
      event.setWillUpdate(true);
      return;
    }
    if (plugin.getPrayerManager().isPrayerActive(event.getPlayer(), prayer)) {
      boolean success = plugin.getPrayerManager().disablePrayer(event.getPlayer(), prayer);
      event.setWillUpdate(success);
    } else {
      boolean success = plugin.getPrayerManager().activatePrayer(event.getPlayer(), prayer);
      event.setWillUpdate(success);
    }
  }
}
