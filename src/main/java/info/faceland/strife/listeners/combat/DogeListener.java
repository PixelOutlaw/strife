package info.faceland.strife.listeners.combat;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.managers.AttributedEntityManager;
import java.util.Random;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DogeListener implements Listener {

  private final AttributedEntityManager attributedEntityManager;
  private final Random random;

  private static final String[] DOGE_MEMES =
      {"<aqua>wow", "<green>wow", "<light purple>wow", "<aqua>much pain", "<green>much pain",
          "<light purple>much pain", "<aqua>many disrespects", "<green>many disrespects",
          "<light purple>many disrespects", "<red>no u", "<red>2damage4me"};

  public DogeListener(AttributedEntityManager attributedEntityManager) {
    this.attributedEntityManager = attributedEntityManager;
    this.random = new Random(System.currentTimeMillis());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDogeProc(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player) || event.isCancelled()) {
      return;
    }
    AttributedEntity attacker = attributedEntityManager
        .getAttributedEntity((LivingEntity) event.getEntity());
    if (random.nextDouble() <= attacker.getAttribute(StrifeAttribute.DOGE) / 100) {
      MessageUtils.sendMessage(event.getEntity(), DOGE_MEMES[random.nextInt(DOGE_MEMES.length)]);
    }
  }
}
