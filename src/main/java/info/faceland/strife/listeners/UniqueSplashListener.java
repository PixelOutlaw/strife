package info.faceland.strife.listeners;

import static info.faceland.strife.attributes.StrifeAttribute.BLOCK;
import static info.faceland.strife.attributes.StrifeAttribute.EARTH_DAMAGE;
import static info.faceland.strife.attributes.StrifeAttribute.MAX_EARTH_RUNES;
import static info.faceland.strife.util.DamageUtil.doBlock;
import static info.faceland.strife.util.DamageUtil.doEvasion;
import static info.faceland.strife.util.DamageUtil.rollDouble;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.EntityStatCache;
import info.faceland.strife.managers.BlockManager;
import info.faceland.strife.managers.EffectManager;
import info.faceland.strife.util.StatUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class UniqueSplashListener implements Listener {

  private final EntityStatCache entityStatCache;
  private final BlockManager blockManager;
  private final EffectManager effectManager;

  public UniqueSplashListener(EntityStatCache entityStatCache, BlockManager blockManager,
      EffectManager effectManager) {
    this.entityStatCache = entityStatCache;
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
    AttributedEntity attacker = entityStatCache.getAttributedEntity(attackEntity);
    for (Entity e : event.getAffectedEntities()) {
      if (!(e instanceof LivingEntity)) {
        continue;
      }
      LivingEntity defendEntity = (LivingEntity) e;
      AttributedEntity defender = entityStatCache.getAttributedEntity(defendEntity);

      double evasionMultiplier = StatUtil.getEvasion(attacker, defender);
      evasionMultiplier = evasionMultiplier + (rollDouble() * (1 - evasionMultiplier));
      if (evasionMultiplier <= 0.5) {
        doEvasion(attackEntity, defendEntity);
        event.setCancelled(true);
        return;
      }
      if (blockManager.rollBlock(defendEntity.getUniqueId(), defender.getAttribute(BLOCK), false)) {
        blockManager.blockFatigue(defendEntity.getUniqueId(), 1.0, false);
        if (defender.getAttribute(EARTH_DAMAGE) > 0) {
          blockManager.bumpRunes(defendEntity.getUniqueId(),
              (int) defender.getAttribute(MAX_EARTH_RUNES));
        }
        doBlock(attackEntity, defendEntity);
        event.setCancelled(true);
        return;
      }
      for (String s : effects) {
        if (effectManager.getEffect(s) == null) {
          continue;
        }
        effectManager.getEffect(s).execute(attacker, defender);
      }
    }
  }
}
