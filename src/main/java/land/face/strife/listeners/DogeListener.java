package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import java.util.List;
import java.util.Random;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.StrifeMobManager;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DogeListener implements Listener {

  private final StrifeMobManager strifeMobManager;
  private final Random random;

  private static final List<String> DOGE_MEMES = List.of(
      FaceColor.ORANGE + "2 the moon??",
      FaceColor.CYAN + "wow",
      FaceColor.LIGHT_GREEN + "wow",
      FaceColor.PINK + "wow",
      FaceColor.PURPLE + "wow",
      FaceColor.CYAN + "oof",
      FaceColor.LIGHT_GREEN + "oof",
      FaceColor.PINK + "oof",
      FaceColor.PURPLE + "oof",
      FaceColor.CYAN + "much pain",
      FaceColor.LIGHT_GREEN + "much pain",
      FaceColor.PINK + "much pain",
      FaceColor.PURPLE + "much pain",
      FaceColor.CYAN + "many ouch",
      FaceColor.LIGHT_GREEN + "many ouch",
      FaceColor.PINK + "many ouch",
      FaceColor.PURPLE + "many ouch",
      FaceColor.CYAN + "no u",
      FaceColor.LIGHT_GREEN + "no u",
      FaceColor.PINK + "no u",
      FaceColor.PURPLE + "no u",
      FaceColor.RED + "no u",
      FaceColor.CYAN + "2damage4me",
      FaceColor.LIGHT_GREEN + "2damage4me",
      FaceColor.PINK + "2damage4me",
      FaceColor.PURPLE + "2damage4me"
  );

  public DogeListener(StrifeMobManager strifeMobManager) {
    this.strifeMobManager = strifeMobManager;
    this.random = new Random(System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDogeProc(EntityDamageEvent event) {
    if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
      return;
    }
    StrifeMob attacker = strifeMobManager.getStatMob((LivingEntity) event.getEntity());
    if (random.nextDouble() <= attacker.getStat(StrifeStat.DOGE) / 100) {
      event.getEntity().sendMessage(StringUtils.repeat(" ", random.nextInt(5)) +
          DOGE_MEMES.get(random.nextInt(DOGE_MEMES.size())));
    }
  }
}
