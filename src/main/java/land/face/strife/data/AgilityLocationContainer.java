package land.face.strife.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Getter
@Setter
public class AgilityLocationContainer {

  private final Map<UUID, Integer> progress = new HashMap<>();
  private final Set<UUID> recentPlayers = new HashSet<>();

  private String name;
  private float exp;
  private float difficulty;
  private List<Location> locations = new ArrayList<>();

  public static void checkStart(AgilityLocationContainer cont, Player player, Location loc) {
    if (!cont.getProgress().containsKey(player.getUniqueId())) {
      if (loc.distanceSquared(cont.getLocations().get(0)) < 2) {
        cont.getProgress().put(player.getUniqueId(), 0);
        if (cont.getLocations().get(1) != null) {
          player.spawnParticle(Particle.FIREWORKS_SPARK, cont.getLocations().get(1), 10, 0.5, 0.5,
              0.5, 0);
        }
      }
    }
  }

  public static boolean setProgress(AgilityLocationContainer cont, Player player, Location loc) {
    if (!cont.getProgress().containsKey(player.getUniqueId())) {
      return false;
    }
    if (cont.getLocations().size() == 1) {
      Bukkit.getLogger().warning("Agility set " + cont.getName() + "has only 1 location");
      return false;
    }
    int progress = cont.getProgress().get(player.getUniqueId());
    if (loc.distanceSquared(cont.getLocations().get(progress + 1)) < 2) {
      if (progress + 2 == cont.getLocations().size()) {
        cont.getProgress().remove(player.getUniqueId());
        player.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        return true;
      } else {
        cont.getProgress().put(player.getUniqueId(), progress + 1);
        player.playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1, 2);
        player.spawnParticle(Particle.FIREWORKS_SPARK,
            cont.getLocations().get(progress + 2), 10, 0.5, 0.5, 0.5, 0);
        return false;
      }
    }
    if (loc.distanceSquared(cont.getLocations().get(progress)) < 2) {
      return false;
    }
    cont.getProgress().remove(player.getUniqueId());
    player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.7f);
    return false;
  }
}
