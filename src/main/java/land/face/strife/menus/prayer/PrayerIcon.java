package land.face.strife.menus.prayer;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.ticxo.modelengine.api.animation.keyframe.type.ScriptKeyframe;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.managers.PrayerManager.Prayer;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PrayerIcon extends MenuItem {

  private final Prayer prayer;
  private final StrifePlugin plugin;

  PrayerIcon(StrifePlugin plugin, Prayer prayer) {
    super("", new ItemStack(Material.BARRIER));
    this.plugin = plugin;
    this.prayer = prayer;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {

    ItemStack stack = new ItemStack(Material.PAPER);

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    if (plugin.getPrayerManager().getPrayerLevelReq().get(prayer) >
        mob.getChampion().getLifeSkillLevel(LifeSkillType.PRAYER)) {
      ItemStackExtensionsKt.setCustomModelData(stack, 800);
      ItemStackExtensionsKt.setDisplayName(stack, plugin.getPrayerManager().getPrayerNames().get(prayer));
      List<String> lore = new ArrayList<>();
      lore.addAll(plugin.getPrayerManager().getPrayerLore().get(prayer));
      lore.add("");
      lore.add(FaceColor.ORANGE + "[Not Unlocked]");
      TextUtils.setLore(stack, lore, false);
      return stack;
    }
    List<String> lore = new ArrayList<>();
    ItemStackExtensionsKt.setDisplayName(stack, plugin.getPrayerManager().getPrayerNames().get(prayer));
    lore.addAll(plugin.getPrayerManager().getPrayerLore().get(prayer));
    lore.add("");
    lore.add(FaceColor.WHITE + "Faith: " + (int) mob.getPrayer() + " / " + (int) mob.getMaxPrayer());
    boolean isActive = plugin.getPrayerManager().isPrayerActive(player, prayer);
    if (isActive) {
      ItemStackExtensionsKt.setCustomModelData(stack, plugin.getPrayerManager().getPrayerModelData().get(prayer));
      lore.add("");
      lore.add(FaceColor.YELLOW + "Click To Deactivate!");
    } else if (plugin.getPrayerManager().getPrayerActivationCost().get(prayer) > mob.getPrayer()) {
      ItemStackExtensionsKt.setCustomModelData(stack, 801);
      lore.add("");
      lore.add(FaceColor.RED + "You don't have enough Faith!");
    } else {
      ItemStackExtensionsKt.setCustomModelData(stack, plugin.getPrayerManager().getPrayerModelData().get(prayer) + 1);
      lore.add("");
      lore.add(FaceColor.GREEN + "Click To Activate!");
    }
    TextUtils.setLore(stack, lore, false);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.DOUBLE_CLICK) {
      return;
    }

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (plugin.getPrayerManager().getPrayerLevelReq().get(prayer) >
        mob.getChampion().getLifeSkillLevel(LifeSkillType.PRAYER)) {
      return;
    }
    if (plugin.getPrayerManager().isPrayerActive(event.getPlayer(), prayer)) {
      boolean success = plugin.getPrayerManager().disablePrayer(event.getPlayer(), prayer);
      event.setWillUpdate(success);
    } else {
      if (plugin.getPrayerManager().getPrayerActivationCost().get(prayer) > mob.getPrayer()) {
        event.setWillUpdate(true);
        return;
      }
      boolean success = plugin.getPrayerManager().activatePrayer(event.getPlayer(), prayer);
      event.setWillUpdate(success);
    }
  }
}
