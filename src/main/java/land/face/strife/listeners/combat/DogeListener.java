package land.face.strife.listeners.combat;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
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

  private static final String[] DOGE_MEMES =
      {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
          "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects",
          "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};

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
      MessageUtils.sendMessage(event.getEntity(), DOGE_MEMES[random.nextInt(DOGE_MEMES.length)]);
    }
  }
}
