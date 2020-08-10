package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.EvokerFangEffect;
import land.face.strife.managers.EffectManager;
import land.face.strife.managers.StrifeMobManager;
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
    String effects = EvokerFangEffect.FANG_EFFECT_MAP.getOrDefault(event.getDamager(), null);
    if (StringUtils.isBlank(effects)) {
      return;
    }

    String[] split = effects.split("~");
    if (split.length == 0) {
      return;
    }
    EvokerFangs fangs = (EvokerFangs) event.getDamager();
    StrifeMob attacker = strifeMobManager.getStatMob(Objects.requireNonNull(fangs.getOwner()));

    Set<LivingEntity> targets = new HashSet<>();
    TargetResponse response = new TargetResponse(targets);

    List<Effect> effectList = new ArrayList<>();
    for (String s : split) {
      Effect effect = effectManager.getEffect(s);
      if (effect != null) {
        effectList.add(effect);
      }
    }

    effectManager.processEffectList(attacker, response, effectList);
    event.setCancelled(true);
  }
}
