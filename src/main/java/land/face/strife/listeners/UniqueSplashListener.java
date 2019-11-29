package land.face.strife.listeners;

import land.face.strife.data.StrifeMob;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.EffectManager;
import land.face.strife.managers.StrifeMobManager;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class UniqueSplashListener implements Listener {

  private final StrifeMobManager strifeMobManager;
  private final BlockManager blockManager;
  private final EffectManager effectManager;

  public UniqueSplashListener(StrifeMobManager strifeMobManager, BlockManager blockManager,
      EffectManager effectManager) {
    this.strifeMobManager = strifeMobManager;
    this.blockManager = blockManager;
    this.effectManager = effectManager;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAbilityPotionSplash(PotionSplashEvent event) {
    if (!event.getEntity().hasMetadata("EFFECT_PROJECTILE")) {
      return;
    }
    if (!(event.getEntity().getShooter() instanceof LivingEntity)) {
      return;
    }
    String[] effects = event.getEntity().getMetadata("EFFECT_PROJECTILE").get(0).asString()
        .split("~");
    if (effects.length == 0) {
      return;
    }
    LivingEntity attackEntity = (LivingEntity) event.getEntity().getShooter();
    StrifeMob attacker = strifeMobManager.getStatMob(attackEntity);
    for (Entity e : event.getAffectedEntities()) {
      if (!(e instanceof LivingEntity)) {
        continue;
      }
      LivingEntity defendEntity = (LivingEntity) e;
      StrifeMob defender = strifeMobManager.getStatMob(defendEntity);

      double evasionMultiplier = StatUtil
          .getMinimumEvasionMult(StatUtil.getEvasion(defender), StatUtil.getAccuracy(attacker));
      evasionMultiplier = evasionMultiplier + (DamageUtil.rollDouble() * (1 - evasionMultiplier));
      if (evasionMultiplier <= 0.5) {
        DamageUtil.doEvasion(attackEntity, defendEntity);
        event.setCancelled(true);
        return;
      }

      if (blockManager.rollBlock(defender, false)) {
        blockManager.blockFatigue(defendEntity.getUniqueId(), 1.0, false);
        blockManager.bumpRunes(defender);
        DamageUtil.doBlock(attackEntity, defendEntity);
        event.setCancelled(true);
        return;
      }

      for (String s : effects) {
        effectManager.execute(effectManager.getEffect(s), attacker, defender.getEntity());
      }
    }
  }
}
