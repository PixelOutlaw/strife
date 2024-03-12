package land.face.strife.managers;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.Spawner;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public class SpawnerManager {

  private final StrifePlugin plugin;

  @Getter
  private final Map<String, Spawner> spawnerMap = new HashMap<>();

  private final MarkerSet mobSet = MarkerSet.builder().label("\uD83D\uDC80 Enemies").defaultHidden(true).build();

  private final String mobHtml = "<div style='position: relative; display: inline-block; transform: translate(-50%, 100%);'><div style='font-size: 1.4em;'>\uD83D\uDC80<div style='font-size: 0.8em; text-align: center; transform: translate(0%, -100%); color: white; text-shadow: -1px -1px 0 black,-1px -1px 0 black,-1px 0px 0 black,-1px 1px 0 black,-1px 1px 0 black,-1px -1px 0 black,-1px -1px 0 black,-1px 0px 0 black,-1px 1px 0 black,-1px 1px 0 black,0px -1px 0 black,0px -1px 0 black,0px 0px 0 black,0px 1px 0 black,0px 2px 0 black,1px -1px 0 black,1px -1px 0 black,1px 0px 0 black,1px 1px 0 black,1px 2px 0 black,1px -1px 0 black,2px -1px 0 black,1px 0px 0 black,1px 1px 0 black,1px 1px 0 black'>[lvl]</div></div></div>";

  private boolean iconsBuilt = false;

  public SpawnerManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void setSpawnerMap(Map<String, Spawner> spawnerMap) {
    this.spawnerMap.clear();
    this.spawnerMap.putAll(spawnerMap);
    purgeIcons();
    iconsBuilt = false;
    loadItems();
    Bukkit.getScheduler().runTaskTimer(plugin, (task) -> {
      if (iconsBuilt) {
        task.cancel();
        return;
      }
      setupMapIcons();
    }, 200, 200);
  }

  public void addSpawner(String id, Spawner spawner) {
    this.spawnerMap.put(id, spawner);
  }

  public void removeSpawner(String id) {
    this.spawnerMap.remove(id);
  }

  public void addRespawnTime(LivingEntity livingEntity) {
    for (Spawner spawner : spawnerMap.values()) {
      spawner.addRespawnTimeIfApplicable(livingEntity);
    }
  }

  public void setupMapIcons() {
    Bukkit.getLogger().info("[Strife] Attempting to place markers...");
    BlueMapAPI.getInstance().flatMap(api -> api.getWorld("quest_world")).ifPresent(world -> {
      for (BlueMapMap map : world.getMaps()) {
        map.getMarkerSets().put("mob-set", mobSet);
      }
      Bukkit.getLogger().info("[Strife] Successfully placed markers! Woo!");
      iconsBuilt = true;
    });
  }

  public void loadItems() {
    for (Spawner spawner : spawnerMap.values()) {
      int lvl = spawner.getUniqueEntity().getBaseLevel();
      String html = mobHtml.replace("[lvl]", Integer.toString(lvl));

      HtmlMarker marker = new HtmlMarker(
          spawner.getUniqueEntity().getName(),
          new Vector3d(spawner.getLocation().getX(), spawner.getLocation().getY() + 5, spawner.getLocation().getZ()),
          html
      );
      marker.setAnchor(0, 60);
      marker.setMinDistance(0);
      marker.setMaxDistance(1200);

      mobSet.put(spawner.getId(), marker);
    }
  }

  public void purgeIcons() {
    if (iconsBuilt) {
      mobSet.getMarkers().clear();
      BlueMapAPI.getInstance().flatMap(api -> api.getWorld("quest_world")).ifPresent(world -> {
        for (BlueMapMap map : world.getMaps()) {
          map.getMarkerSets().remove("mob-set");
        }
      });
    }
  }

  public void cancelAll() {
    for (Spawner spawner : spawnerMap.values()) {
      spawner.cancel();
    }
  }
}
