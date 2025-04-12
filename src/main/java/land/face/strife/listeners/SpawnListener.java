package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import land.face.strife.StrifePlugin;
import land.face.strife.util.SpecialStatusUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
  public void onJockeySpawn(CreatureSpawnEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getSpawnReason() == SpawnReason.JOCKEY) {
      event.setCancelled(true);
      return;
    }
    if (event.getSpawnReason() != SpawnReason.CUSTOM) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        switch (event.getEntityType()) {
          case ZOMBIFIED_PIGLIN, SKELETON, ZOMBIE -> {
            if (event.getEntity().getVehicle() != null) {
              event.getEntity().getVehicle().remove();
            }
          }
        }
      }, 1L);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
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

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSpawnerMobSpawn(CreatureSpawnEvent event) {
    if (event.isCancelled() || event.getSpawnReason() != SpawnReason.SPAWNER) {
      return;
    }
    if (event.getEntity().getType() != EntityType.VILLAGER) {
      ChunkUtil.setDespawnOnUnload(event.getEntity());
    }
  }

  static ItemStack buildSkeletonWand() {
    ItemStack wand = new ItemStack(Material.BOW);
    ItemUtils.setItemModel(wand, "faceland:equipment/unique/staff/mob_staff");
    return wand;
  }
}
