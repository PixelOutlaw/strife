package land.face.strife.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.Effect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;

public class FangUtil {

  private static final Map<EvokerFangs, Boolean> NO_DAMAGE_FANGS = new WeakHashMap<>();
  private static final Set<String> HIT_DELAY_IDS = new HashSet<>();

  public static void createFang(StrifeMob owner, Location location, List<Effect> effects, String id) {
    location.setYaw(location.getYaw() + 90);
    EvokerFangs fangs = location.getWorld().spawn(location, EvokerFangs.class);
    fangs.setOwner(owner.getEntity());
    NO_DAMAGE_FANGS.put(fangs, true);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> playFangEffects(fangs, owner, effects, id), 9L);
  }

  public static boolean isNoDamageFang(EvokerFangs fangs) {
    return NO_DAMAGE_FANGS.containsKey(fangs);
  }

  private static void playFangEffects(EvokerFangs fangs, StrifeMob owner, List<Effect> effects, String effectId) {
    Set<LivingEntity> targets = TargetingUtil.getEntitiesInArea(fangs.getLocation().add(0, 0.3, 0), 0.75);
    targets.removeIf(t -> HIT_DELAY_IDS.contains(t.getUniqueId() + effectId));

    if (targets.isEmpty()) {
      return;
    }

    TargetResponse response = new TargetResponse(targets);
    StrifePlugin.getInstance().getEffectManager().executeEffectList(owner, response, effects);

    for (LivingEntity livingEntity : targets) {
      String delayKey = livingEntity.getUniqueId() + effectId;
      HIT_DELAY_IDS.add(delayKey);
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> HIT_DELAY_IDS.remove(delayKey), 10L);
    }
  }

}
