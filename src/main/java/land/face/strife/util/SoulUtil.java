package land.face.strife.util;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SoulUtil {

  public static Hologram createSoul(Player player, String text, Location location) {
    Hologram holo = DHAPI.createHologram(UUID.randomUUID().toString(),
        location.clone(), false, List.of(""));
    holo.hideAll();
    holo.addPage();
    DHAPI.setHologramLines(holo, 1, List.of(text, "#ICON: PLAYER_HEAD (" + player.getName()+ ")"));
    return holo;
  }
}
