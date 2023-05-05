package land.face.strife.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.Effect;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.ProjectileUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class UniqueSplashListener implements Listener {

  private final StrifePlugin plugin;

  public UniqueSplashListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAbilityPotionSplash(PotionSplashEvent event) {
    List<Effect> hitEffects = ProjectileUtil.getHitEffects(event.getEntity());
    if (hitEffects == null || hitEffects.isEmpty()) {
      return;
    }
    if (!(event.getEntity().getShooter() instanceof LivingEntity attackEntity)) {
      return;
    }
    StrifeMob attacker = plugin.getStrifeMobManager().getStatMob(attackEntity);
    for (Entity e : event.getAffectedEntities()) {
      if (!(e instanceof LivingEntity defendEntity)) {
        continue;
      }
      StrifeMob defender = plugin.getStrifeMobManager().getStatMob(defendEntity);

      if (DamageUtil.isEvaded(attacker, defender, null)) {
        DamageUtil.doEvasion(attacker, defender);
        event.setCancelled(true);
        return;
      }

      if (plugin.getBlockManager().rollBlock(defender, 1.0f, false, true, false)) {
        event.setCancelled(true);
        return;
      }

      Set<LivingEntity> targets = new HashSet<>();
      TargetResponse response = new TargetResponse(targets);

      plugin.getEffectManager().processEffectList(attacker, response, hitEffects);
    }
  }
}
