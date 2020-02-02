package land.face.strife.listeners;

import land.face.strife.StrifePlugin;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.StandardDamage;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.data.effects.StrifeParticle.ParticleStyle;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class EntityMagicListener implements Listener {

  private StrifePlugin plugin;
  private ItemStack skeletonWand;

  private StrifeParticle chaserParticle;
  private StandardDamage witchSpell;

  private static final String WITCH_SPELL_ID = "INTERNAL-WITCH-ATTACK";

  public EntityMagicListener(StrifePlugin plugin) {
    this.plugin = plugin;
    skeletonWand = SpawnListener.buildSkeletonWand();
    witchSpell = buildStandardDamage();
    chaserParticle = buildChaserParticle();
    StrifePlugin.getInstance().getChaserManager().loadChaser(WITCH_SPELL_ID, buildLoadedChaser());
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onWitchPotionThrow(ProjectileLaunchEvent e) {
    if (e.isCancelled() || !(e.getEntity().getShooter() instanceof Witch)) {
      return;
    }
    if (!(e.getEntity() instanceof ThrownPotion)) {
      return;
    }
    Witch witch = (Witch) e.getEntity().getShooter();
    e.setCancelled(true);
    witch.getWorld().playSound(witch.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.9f, 2f);
    StrifeMob witchMob = plugin.getStrifeMobManager().getStatMob(witch);
    plugin.getChaserManager().createChaser(witchMob, WITCH_SPELL_ID, new Vector(0,0,0),
        TargetingUtil.getOriginLocation(witch, OriginLocation.BELOW_HEAD), witch.getTarget());
  }

  @EventHandler
  public void onShulkerBulletHit(EntityDamageByEntityEvent e) {
    if (e.isCancelled() || !(e.getDamager() instanceof ShulkerBullet)) {
      return;
    }
    if ((((Projectile) e.getDamager()).getShooter() instanceof Shulker)) {
      return;
    }
    if (!(e.getEntity() instanceof LivingEntity)) {
      return;
    }
    LivingEntity t = (LivingEntity) e.getEntity();
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> t.removePotionEffect(PotionEffectType.LEVITATION), 1L);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onSkeletonSpell(ProjectileLaunchEvent e) {
    if (e.isCancelled() || (!(e.getEntity().getShooter() instanceof Skeleton))) {
      return;
    }
    if (!((Skeleton) e.getEntity().getShooter()).getEquipment().getItemInMainHand()
        .isSimilar(skeletonWand)) {
      return;
    }
    if (!(e.getEntity() instanceof Arrow)) {
      return;
    }
    e.setCancelled(true);
    Skeleton skelly = (Skeleton) e.getEntity().getShooter();
    skelly.getWorld().playSound(skelly.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
    ShulkerBullet magicProj = skelly.getWorld()
        .spawn(skelly.getEyeLocation().clone().add(0, -0.45, 0), ShulkerBullet.class);
    magicProj.setShooter(skelly);
    Vector vec = skelly.getLocation().getDirection();
    magicProj.setVelocity(new Vector(vec.getX() * 1.2, vec.getY() * 1.2 + 0.255, vec.getZ() * 1.2));
  }

  private LoadedChaser buildLoadedChaser() {
    LoadedChaser loadedChaser = new LoadedChaser();
    loadedChaser.setStrictDuration(true);
    loadedChaser.setStartSpeed(1f);
    loadedChaser.setRemoveAtSolids(true);
    loadedChaser.setSpeed(0.1f);
    loadedChaser.setMaxSpeed(1f);
    loadedChaser.setLifespan(200);
    loadedChaser.getParticles().add(chaserParticle);
    loadedChaser.getEffectList().add(witchSpell);
    return loadedChaser;
  }

  private StandardDamage buildStandardDamage() {
    StandardDamage standardDamage = new StandardDamage();
    standardDamage.setAttackMultiplier(1.0f);
    standardDamage.setHealMultiplier(1.0f);
    standardDamage.setAttackType(AttackType.MAGIC);
    standardDamage.setCanBeBlocked(true);
    standardDamage.setCanBeEvaded(true);
    return standardDamage;
  }

  private StrifeParticle buildChaserParticle() {
    StrifeParticle particle = new StrifeParticle();
    particle.setFriendly(true);
    particle.setParticle(Particle.SPELL_WITCH);
    particle.setRed(0.8);
    particle.setBlue(0.8);
    particle.setGreen(0.2);
    particle.setParticleOriginLocation(OriginLocation.CENTER);
    particle.setStyle(ParticleStyle.NORMAL);
    particle.setQuantity(5);
    particle.setSpeed(0.05F);
    particle.setSpread(0);
    return particle;
  }
}
