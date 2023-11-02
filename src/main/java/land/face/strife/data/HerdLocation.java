package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.HashSet;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.events.HerdEvent;
import land.face.strife.managers.IndicatorManager.IndicatorStyle;
import land.face.strife.util.SpecialStatusUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@Getter
@Setter
public class HerdLocation {

  private final String id;
  private final Location location;
  private final String chunkKey;
  private final double range;
  private final Set<String> uniqueEntities;

  public HerdLocation(String id, Location location, double range, Set<String> uniqueEntities) {
    this.id = id;
    this.location = location;
    this.range = range;
    this.uniqueEntities = uniqueEntities;
    chunkKey = ChunkUtil.buildChunkKey(location.getChunk());
  }

  public void runCheck() {
    if (!ChunkUtil.isChuckLoaded(chunkKey)) {
      return;
    }
    for (Entity e : location.getNearbyEntities(range, range, range)) {
      String uniqueId = SpecialStatusUtil.getUniqueId(e);
      if (StringUtils.isBlank(uniqueId)) {
        continue;
      }
      if (!uniqueEntities.contains(uniqueId)) {
        continue;
      }
      HerdEvent event = new HerdEvent((LivingEntity) e, uniqueId, SpecialStatusUtil.getHerdedBy(e));
      Bukkit.getPluginManager().callEvent(event);

      despawnParticles((LivingEntity) e);
      Set<Player> players = new HashSet<>(e.getLocation().getNearbyEntitiesByType(Player.class, 15));
      for (Player p : players) {
        StrifePlugin.getInstance().getIndicatorManager().addIndicator(p, (LivingEntity) e,
            IndicatorStyle.FLOAT_UP_MEDIUM, 10, "<green><bold>Noice!", 0.3f, 1.6f, 0.8f);
      }
      e.remove();
    }
  }

  private static void despawnParticles(LivingEntity livingEntity) {
    double height = livingEntity.getHeight() / 2;
    double width = livingEntity.getWidth() / 2;
    Location loc = livingEntity.getLocation().clone().add(0, height, 0);
    livingEntity.getWorld().spawnParticle(Particle.CLOUD, loc, (int) (10 * (height + width)), width,
        height, width, 0);
    livingEntity.getWorld().spawnParticle(Particle.TOTEM, loc, (int) (10 * (height + width)), width,
        height, width, 0);
  }
}
