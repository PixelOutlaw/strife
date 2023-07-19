package land.face.strife.data.effects;

import static land.face.strife.data.champion.EquipmentCache.EQUIPMENT_SLOTS;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.listeners.SpawnListener;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.MinionTask;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Camel;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MinionSummon extends LocationEffect {

  private String uniqueEntity;
  private String soundEffect;
  private float lifeMult;
  private int amount;
  private double lifespanSeconds;
  private boolean mount;
  private boolean clone;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());
    applyAtLocation(caster, loc);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {

    double lifespan;
    if (lifespanSeconds == -1) {
      lifespan = Integer.MAX_VALUE;
    } else {
      lifespan = lifespanSeconds * (1 + (caster.getStat(StrifeStat.EFFECT_DURATION) / 100));
    }

    for (int i = 0; i < amount; i++) {
      StrifeMob summonedEntity = StrifePlugin.getInstance().getUniqueEntityManager()
          .spawnUnique(uniqueEntity, location);

      if (summonedEntity == null || summonedEntity.getEntity() == null) {
        return;
      }

      LivingEntity summon = summonedEntity.getEntity();
      caster.addMinion(summonedEntity, (int) lifespan, false);

      if (clone) {
        Disguise disguise;
        if (caster.getEntity().getType() == EntityType.PLAYER) {
          disguise = new PlayerDisguise((Player) caster.getEntity());
          ((PlayerDisguise) disguise).setName("<Inherit>");
        } else {
          disguise = new MobDisguise(DisguiseType.getType(caster.getEntity().getType()));
        }
        disguise.setReplaceSounds(true);
        disguise.setDynamicName(true);

        DisguiseAPI.disguiseToAll(summonedEntity.getEntity(), disguise);

        Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
          for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
            if (slot == EquipmentSlot.HAND && ItemUtil
                .isWandOrStaff(caster.getEntity().getEquipment().getItem(EquipmentSlot.HAND))) {
              summonedEntity.getEntity().getEquipment().setItem(slot, SpawnListener.SKELETON_WAND);
            } else {
              summonedEntity.getEntity().getEquipment()
                  .setItem(slot, caster.getEntity().getEquipment().getItem(slot));
            }
          }
        }, 2L);
        summonedEntity.setStats(caster.getBaseStats());
      }

      if (caster.getEntity() instanceof Mob && summon instanceof Mob) {
        ((Mob) summon).setTarget(((Mob) caster.getEntity()).getTarget());
      }

      if (summon instanceof Tameable && caster.getEntity() instanceof Player) {
        ((Tameable) summon).setOwner((Player) caster.getEntity());
      }

      if (soundEffect != null) {
        PlaySound sound = (PlaySound) StrifePlugin.getInstance().getEffectManager()
            .getEffect(soundEffect);
        sound.applyAtLocation(caster, summon.getLocation());
      }

      if (mount) {
        switch (summon.getType()) {
          case HORSE -> {
            ((Horse) summon).getInventory().setSaddle(new ItemStack(Material.SADDLE));
            ((Horse) summon).setTamed(true);
          }
          case ZOMBIE_HORSE -> ((ZombieHorse) summon).setTamed(true);
          case SKELETON_HORSE -> ((SkeletonHorse) summon).setTamed(true);
          case CAMEL -> ((Camel) summon).getInventory().setSaddle(new ItemStack(Material.SADDLE));
          case PIG -> ((Pig) summon).setSaddle(true);
        }
        summon.addPassenger(caster.getEntity());
      }
    }
    MinionTask.expireMinions(caster);
  }

  public void setUniqueEntity(String uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setLifespanSeconds(double lifespanSeconds) {
    this.lifespanSeconds = lifespanSeconds;
  }

  public void setMount(boolean mount) {
    this.mount = mount;
  }

  public void setSoundEffect(String soundEffect) {
    this.soundEffect = soundEffect;
  }

  public void setLifeMult(float lifeMult) {
    this.lifeMult = lifeMult;
  }

  public void setClone(boolean clone) {
    this.clone = clone;
  }
}
