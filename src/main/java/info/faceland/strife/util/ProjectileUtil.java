package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.WitherSkull;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ProjectileUtil {

  public final static String ATTACK_SPEED_META = "AS_MULT";
  public final static String SNEAK_ATTACK_META = "SNEAK_SHOT";

  public static void createMagicMissile(Player shooter, double attackMult, double power,
      double xOff, double yOff, double zOff) {
    ShulkerBullet magicProj = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.5, 0), ShulkerBullet.class);
    magicProj.setShooter(shooter);

    Vector vec = shooter.getLocation().getDirection();
    xOff = vec.getX() * power + xOff;
    yOff = vec.getY() * power + yOff + 0.25;
    zOff = vec.getZ() * power + zOff;
    magicProj.setVelocity(new Vector(xOff, yOff, zOff));
    setProjctileAttackSpeedMeta(magicProj, attackMult);
    if (shooter.isSneaking()) {
      setProjectileSneakMeta(magicProj);
    }
  }

  public static void createGhastBall(LivingEntity shooter, double attackMult, double power,
      double radius) {
    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.7f, 1.1f);
    Fireball fireball = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.5, 0), Fireball.class);
    fireball.setShooter(shooter);
    fireball.setBounce(false);
    fireball.setIsIncendiary(false);
    fireball.setYield((float) (2 + radius * 0.5));

    Vector vec = shooter.getLocation().getDirection().multiply(0.05 * power);
    fireball.setVelocity(vec);
    setProjctileAttackSpeedMeta(fireball, attackMult);
  }

  public static void createWitherSkull(LivingEntity shooter, double attackMult, double power,
      double radius) {
    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 1.1f);
    WitherSkull skull = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.5, 0), WitherSkull.class);
    skull.setShooter(shooter);
    skull.setYield((float) (2 + radius * 0.5));

    Vector vec = shooter.getLocation().getDirection().multiply(0.05 * power);
    skull.setVelocity(vec);
    setProjctileAttackSpeedMeta(skull, attackMult);
  }

  public static void setProjctileAttackSpeedMeta(Projectile proj, double attackMult) {
    proj.setMetadata(ATTACK_SPEED_META,
        new FixedMetadataValue(StrifePlugin.getInstance(), attackMult));
  }

  public static void setProjectileSneakMeta(Projectile projectile) {
    projectile.setMetadata(SNEAK_ATTACK_META,
        new FixedMetadataValue(StrifePlugin.getInstance(), true));
  }
}
