package land.face.strife.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.CreateModelAnimation;
import land.face.strife.data.effects.CreateModelAnimation.AnimationType;
import land.face.strife.managers.PrayerManager.Prayer;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil.OriginLocation;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionListener implements Listener {

  private final StrifePlugin plugin;
  public static final Map<String, PotionFx> POTION_VFX = new HashMap<>();
  public static final Set<UUID> CD = new HashSet<>();
  private boolean ignorePotion = false;

  public static boolean ignoreFx = false;
  public static boolean ignoreStride = false;

  public PotionListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPotion(EntityPotionEffectEvent event) {
    if (event.getEntity().hasMetadata("NPC") || event.getEntity().isInvulnerable()) {
      return;
    }
    if (!(event.getAction() == Action.ADDED || event.getAction() == Action.CHANGED)) {
      return;
    }
    if (event.isCancelled() || ignorePotion) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
      return;
    }
    switch (event.getNewEffect().getType().getName()) {
      case "SLOW" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
        if (mob.hasTrait(StrifeTrait.UNSTOPPABLE)) {
          event.setCancelled(true);
          return;
        }
        if (ignoreStride) {
          if (mob.hasTrait(StrifeTrait.STRIDE) || plugin.getPrayerManager()
              .isPrayerActive(livingEntity, Prayer.EIGHT)) {
            event.setCancelled(true);
            reduceEffect(event.getNewEffect(), livingEntity);
          }
        }
      }
      case "UNLUCK" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
        if (mob.hasTrait(StrifeTrait.TOXIC_MASCULINITY)) {
          event.setCancelled(true);
        } else if (plugin.getPrayerManager().isPrayerActive(livingEntity, Prayer.EIGHT)) {
          event.setCancelled(true);
          reduceEffect(event.getNewEffect(), livingEntity);
        }
      }
      case "WEAKNESS" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
        if (mob.hasTrait(StrifeTrait.HIGHLY_MOTIVATED)) {
          event.setCancelled(true);
        } else if (plugin.getPrayerManager().isPrayerActive(livingEntity, Prayer.EIGHT)) {
          event.setCancelled(true);
          reduceEffect(event.getNewEffect(), livingEntity);
        }
      }
      case "RAID_OMEN" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
        if (mob.hasTrait(StrifeTrait.LEMON_SCENTED)) {
          event.setCancelled(true);
        }
      }
      case "WITHER" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
        if (mob.hasTrait(StrifeTrait.EXTRA_FRESH)) {
          event.setCancelled(true);
        }
      }
      case "HUNGER" -> {
        if (plugin.getPrayerManager().isPrayerActive(livingEntity, Prayer.EIGHT)) {
          event.setCancelled(true);
          reduceEffect(event.getNewEffect(), livingEntity);
        }
      }
      case "POISON" -> {
        StrifeMob mob = plugin.getStrifeMobManager().getStatMob(livingEntity);
        if (mob.hasTrait(StrifeTrait.ANTI_POISON)) {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void onEffectExpire(EntityPotionEffectEvent event) {
    if (!(event.getCause() == Cause.EXPIRATION && event.getAction() == Action.REMOVED)) {
      return;
    }
    if ("POISON".equals(event.getOldEffect().getType().getName())) {
      if (event.getOldEffect().getAmplifier() > 0) {
        int newAmp = event.getOldEffect().getAmplifier() - 1;
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            ((LivingEntity) event.getEntity()).addPotionEffect(
                new PotionEffect(PotionEffectType.POISON, 100, newAmp, false, true)), 0L);
      }
      return;
    }
    if ("TRIAL_OMEN".equals(event.getOldEffect().getType().getName())) {
      if (event.getOldEffect().getAmplifier() > 0) {
        int newAmp = event.getOldEffect().getAmplifier() - 1;
        Bukkit.getScheduler().runTaskLater(plugin, () -> ((LivingEntity) event.getEntity()).addPotionEffect(
                new PotionEffect(PotionEffectType.TRIAL_OMEN, 30 * 20 * 60, newAmp, false, true)), 0L);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEffectGainz(EntityPotionEffectEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getAction() != Action.ADDED && event.getAction() != Action.CHANGED) {
      return;
    }
    if (ignoreFx || CD.contains(event.getEntity().getUniqueId())) {
      return;
    }

    if (event.getNewEffect().getDuration() < 10 || event.getNewEffect().getAmplifier() >= 10) {
      return;
    }
    if (!(event.getEntity() instanceof LivingEntity le)) {
      return;
    }
    if (plugin.getStealthManager().isStealthed(le)) {
      return;
    }

    int newEffectLevel = event.getNewEffect().getAmplifier();
    int oldEffectLevel = le.hasPotionEffect(event.getNewEffect().getType()) ?
        le.getPotionEffect(event.getNewEffect().getType()).getAmplifier() : 0;

    if (newEffectLevel <= oldEffectLevel) {
      return;
    }
    String name = event.getNewEffect().getType().getName();
    PotionFx potionFx = POTION_VFX.get(name);
    if (potionFx == null) {
      return;
    }
    float scale = potionFx.isScaleWithEffect() ? 0.9f + 0.1f * newEffectLevel : 0.8f;
    // Color color = new Color(modelAnimation.getColor().asRGB());
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
    potionFx.getCreateModelAnimation().setScale(scale);
    plugin.getEffectManager()
        .execute(potionFx.getCreateModelAnimation(), mob, new TargetResponse(Set.of(le)));
    le.getWorld().playSound(le.getLocation(), potionFx.getSoundEffect(), SoundCategory.HOSTILE, 0.85f,
        potionFx.getSoundPitch());
    CD.add(le.getUniqueId());
    Bukkit.getScheduler().runTaskLater(plugin, () -> CD.remove(le.getUniqueId()), 1L);
  }

  public static void buildPotionVfx() {
    POTION_VFX.clear();
    POTION_VFX.put("INCREASE_DAMAGE", buildBuff("#ff0000", true, "littleroom:littleroom.magnus.bonfire_lit", 2));
    POTION_VFX.put("SPEED", buildBuff("#2dffcf", true, "littleroom:littleroom.magnus.bonfire_lit", 2));
    POTION_VFX.put("DAMAGE_RESISTANCE", buildBuff("#2584ff", true, "littleroom:littleroom.magnus.bonfire_lit", 2));
    POTION_VFX.put("SATURATION", buildBuff("#ffcc15", true, "littleroom:littleroom.magnus.bonfire_lit", 2));
    POTION_VFX.put("REGENERATION", buildBuff("#ff66b2", true, "littleroom:littleroom.magnus.bonfire_lit", 2));

    POTION_VFX.put("RAID_OMEN", buildBuff("#ab6900", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("BLINDNESS", buildBuff("#3d2230", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("DARKNESS", buildBuff("#3d2230", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("HUNGER", buildBuff("#8dd111", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("SLOW", buildBuff("#463dff", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("WEAKNESS", buildBuff("#ead1dc", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("WITHER", buildBuff("#270717", false, "minecraft:block.respawn_anchor.deplete", 2));
    POTION_VFX.put("UNLUCK", buildBuff("#c4000b", false, "minecraft:block.respawn_anchor.deplete", 2));
  }

  private static PotionFx buildBuff(String color, boolean buff, String sound, float pitch) {

    CreateModelAnimation effect = new CreateModelAnimation();
    effect.setFriendly(true);
    effect.setModelId(buff ? "fx_buff" : "fx_debuff");
    effect.getAnimations().add("a_skills");
    effect.setStartAnimation(null);
    effect.setEndAnimation(null);
    effect.setLerpIn(0);
    effect.setLerpOut(0);
    effect.setSpeed(1f);
    effect.setScale(0.8f);
    effect.setLifespan(-1);
    effect.setEntityLock(true);
    effect.setAnimationType(AnimationType.RANDOM_ROTATION);
    effect.setRotationLock(false);
    effect.setStrictDuration(true);
    effect.setOrigin(buff ? OriginLocation.GROUND : OriginLocation.ABOVE_HEAD);
    java.awt.Color c = java.awt.Color.decode(color);
    effect.setColor(org.bukkit.Color.fromRGB(c.getRed(), c.getGreen(), c.getBlue()));

    PotionFx potionFx = new PotionFx();
    potionFx.setScaleWithEffect(buff);
    potionFx.setSoundEffect(sound);
    potionFx.setSoundPitch(pitch);
    potionFx.setCreateModelAnimation(effect);

    return potionFx;
  }

  private void reduceEffect(PotionEffect potionEffect, LivingEntity livingEntity) {
    if (potionEffect.getAmplifier() == 0) {
      return;
    }
    PotionEffect newEffect = potionEffect.withAmplifier(potionEffect.getAmplifier() - 1);
    ignorePotion = true;
    newEffect.apply(livingEntity);
    ignorePotion = false;
  }
}

