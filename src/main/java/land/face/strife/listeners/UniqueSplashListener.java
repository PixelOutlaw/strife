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
import land.face.strife.util.StatUtil;
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
    if (!(event.getEntity().getShooter() instanceof LivingEntity)) {
      return;
    }
    LivingEntity attackEntity = (LivingEntity) event.getEntity().getShooter();
    StrifeMob attacker = plugin.getStrifeMobManager().getStatMob(attackEntity);
    for (Entity e : event.getAffectedEntities()) {
      if (!(e instanceof LivingEntity)) {
        continue;
      }
      LivingEntity defendEntity = (LivingEntity) e;
      StrifeMob defender = plugin.getStrifeMobManager().getStatMob(defendEntity);

      double evasionMultiplier = StatUtil.getMinimumEvasionMult(StatUtil.getEvasion(defender),
          StatUtil.getAccuracy(attacker));
      evasionMultiplier = evasionMultiplier + (DamageUtil.rollDouble() * (1 - evasionMultiplier));

      if (evasionMultiplier <= 0.5) {
        DamageUtil.doEvasion(attacker, defender);
        event.setCancelled(true);
        return;
      }

      if (plugin.getBlockManager().rollBlock(defender, false)) {
        plugin.getBlockManager().blockFatigue(defender, 1.0, false, false);
        DamageUtil.doBlock(attacker, defender);
        event.setCancelled(true);
        return;
      }

      Set<LivingEntity> targets = new HashSet<>();
      TargetResponse response = new TargetResponse(targets);

      plugin.getEffectManager().processEffectList(attacker, response, hitEffects);
    }
  }
}
