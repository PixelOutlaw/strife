package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.Effect;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.EffectManager;
import land.face.strife.managers.StrifeMobManager;
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
    String hitEffects = ProjectileUtil.getHitEffects(event.getEntity());
    if (StringUtils.isBlank(hitEffects)) {
      return;
    }
    if (!(event.getEntity().getShooter() instanceof LivingEntity)) {
      return;
    }
    String[] effects = hitEffects.split("~");
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
        DamageUtil.doEvasion(attacker, defender);
        event.setCancelled(true);
        return;
      }

      if (blockManager.rollBlock(defender, false)) {
        blockManager.blockFatigue(defendEntity, 1.0, false);
        blockManager.bumpRunes(defender);
        DamageUtil.doBlock(attacker, defender);
        event.setCancelled(true);
        return;
      }

      TargetResponse response = new TargetResponse();
      Set<LivingEntity> targets = new HashSet<>();
      response.setEntities(targets);

      List<Effect> effectList = new ArrayList<>();
      for (String s : effects) {
        Effect effect = effectManager.getEffect(s);
        if (effect != null) {
          effectList.add(effect);
        }
      }

      effectManager.processEffectList(attacker, response, effectList);
    }
  }
}
