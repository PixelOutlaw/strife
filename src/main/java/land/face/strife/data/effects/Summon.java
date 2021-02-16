package land.face.strife.data.effects;

import static land.face.strife.data.champion.PlayerEquipmentCache.ITEM_SLOTS;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.listeners.SpawnListener;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.EquipmentSlot;

public class Summon extends LocationEffect {

  private String uniqueEntity;
  private String soundEffect;
  private float lifeMult;
  private int amount;
  private double lifespanSeconds;
  private boolean mount;
  private boolean clone;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (int i = 0; i < amount; i++) {
      Location loc = target.getEntity().getLocation();
      applyAtLocation(caster, loc);
    }
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    if (caster.getMinions().size() >= caster.getStat(StrifeStat.MAX_MINIONS)) {
      return;
    }

    StrifeMob summonedEntity = StrifePlugin.getInstance().getUniqueEntityManager()
        .spawnUnique(uniqueEntity, location);

    if (summonedEntity == null || summonedEntity.getEntity() == null) {
      return;
    }

    LivingEntity summon = summonedEntity.getEntity();
    double lifespan = lifespanSeconds * (1 + (caster.getStat(StrifeStat.EFFECT_DURATION) / 100));
    caster.addMinion(summonedEntity, (int) lifespan);

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
        for (EquipmentSlot slot : ITEM_SLOTS) {
          if (slot == EquipmentSlot.HAND && ItemUtil
              .isWandOrStaff(caster.getEntity().getEquipment().getItem(EquipmentSlot.HAND))) {
            summonedEntity.getEntity().getEquipment().setItem(slot, SpawnListener.SKELETON_WAND);
          } else {
            summonedEntity.getEntity().getEquipment()
                .setItem(slot, caster.getEntity().getEquipment().getItem(slot));
          }
        }
      }, 2L);
    }

    summonedEntity.setStats(caster.getBaseStats());

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
      summon.addPassenger(caster.getEntity());
    }
    double maxHealth = summon.getMaxHealth() * (1 + (caster.getStat(StrifeStat.MINION_LIFE) / 100));
    summon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    summon.setHealth(maxHealth);
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
