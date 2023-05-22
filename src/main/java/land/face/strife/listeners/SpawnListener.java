package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.util.SpecialStatusUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

public class SpawnListener implements Listener {

  private final StrifePlugin plugin;
  public static ItemStack SKELETON_WAND;

  public SpawnListener(StrifePlugin plugin) {
    this.plugin = plugin;
    SKELETON_WAND = buildSkeletonWand();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureSpawnHighest(CreatureSpawnEvent event) {
    if (event.isCancelled() || event.getEntity().hasMetadata("NPC") ||
        event.getEntity().hasMetadata("pet") ||
        event.getSpawnReason() == SpawnReason.COMMAND ||
        event.getSpawnReason() == SpawnReason.CUSTOM ||
        StringUtils.isBlank(SpecialStatusUtil.getUniqueId(event.getEntity()))) {
      return;
    }
    event.setCancelled(true);
    // See git history for world level based spawning
  }

  static ItemStack buildSkeletonWand() {
    ItemStack wand = new ItemStack(Material.BOW);
    ItemStackExtensionsKt.setCustomModelData(wand, 4000);
    return wand;
  }
}
