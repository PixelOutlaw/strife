package info.faceland.strife.listeners;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.effects.EvokerFangEffect;
import info.faceland.strife.managers.EffectManager;
import info.faceland.strife.managers.StrifeMobManager;
import java.util.Objects;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EvokerFangEffectListener implements Listener {

  private final StrifeMobManager strifeMobManager;
  private final EffectManager effectManager;

  public EvokerFangEffectListener(StrifeMobManager strifeMobManager, EffectManager effectManager) {
    this.strifeMobManager = strifeMobManager;
    this.effectManager = effectManager;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onEvokerFangHit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof EvokerFangs)) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity)) {
      return;
    }
    if (!event.getDamager().hasMetadata(EvokerFangEffect.FANG_META)) {
      return;
    }
    String[] effects = event.getDamager().getMetadata(EvokerFangEffect.FANG_META).get(0).asString()
        .split("~");
    if (effects.length == 0) {
      return;
    }
    EvokerFangs fangs = (EvokerFangs) event.getDamager();
    StrifeMob attacker = strifeMobManager.getStatMob(Objects.requireNonNull(fangs.getOwner()));
    for (String s : effects) {
      effectManager.execute(effectManager.getEffect(s), attacker, (LivingEntity) event.getEntity());
    }
    event.setCancelled(true);
  }
}
