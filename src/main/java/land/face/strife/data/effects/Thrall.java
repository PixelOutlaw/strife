package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.listeners.SpawnListener;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.ItemUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Thrall extends Effect {

  private int lifeSeconds = 30;
  private String name = "&8«&7Thrall&8»";

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (!(target.getEntity() instanceof Player)) {
      return;
    }
    SoulTimer soul = StrifePlugin.getInstance().getSoulManager().getSoul((Player)
        target.getEntity());
    if (soul == null) {
      return;
    }

    LivingEntity entity = soul.getLocation().getWorld().spawn(soul.getLocation(), Skeleton.class);
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(entity);

    if (mob == null || mob.getEntity() == null) {
      return;
    }

    soul.getOwner().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 600, 0));
    MessageUtils.sendMessage(soul.getOwner(), "&5&oPowerful Black Magics has taken control of your body! Your soul is bound to this grave!");

    mob.setStats(soul.getStatMap());
    mob.getEntity().setCustomName(StringExtensionsKt.chatColorize(name));
    mob.getEntity().setCustomNameVisible(true);

    PlayerDisguise disguise = new PlayerDisguise(soul.getOwner());
    disguise.setName("<Inherit>");
    disguise.setReplaceSounds(true);
    disguise.setDynamicName(true);

    DisguiseAPI.disguiseToAll(mob.getEntity(), disguise);

    entity.getEquipment().setHelmet(soul.getHead());
    if (entity.getEquipment().getHelmet() == null) {
      entity.getEquipment().setHelmet(new ItemStack(Material.STONE_BUTTON));
    }
    entity.getEquipment().setHelmetDropChance(0);
    entity.getEquipment().setChestplate(soul.getBody());
    entity.getEquipment().setChestplateDropChance(0);
    entity.getEquipment().setLeggings(soul.getLegs());
    entity.getEquipment().setLeggingsDropChance(0);
    entity.getEquipment().setBoots(soul.getBoots());
    entity.getEquipment().setBootsDropChance(0);
    entity.getEquipment().setItemInMainHand(soul.getMainHand());
    entity.getEquipment().setItemInMainHandDropChance(0);
    entity.getEquipment().setItemInOffHand(soul.getOffHand());
    entity.getEquipment().setItemInOffHandDropChance(0);

    if (ItemUtil.isWandOrStaff(entity.getEquipment().getItemInMainHand())) {
      entity.getEquipment().setItemInMainHand(SpawnListener.SKELETON_WAND);
    }

    getPlugin().getStatUpdateManager().updateVanillaAttributes(mob);

    float durationSeconds =
        (float) lifeSeconds * (1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100);
    caster.addMinion(mob, (int) durationSeconds);

    // force override damage and health. Minion stats 10% as effective
    // hence divided by 1000
    mob.forceSetStat(StrifeStat.MINION_MULT_INTERNAL,
        caster.getStat(StrifeStat.MINION_DAMAGE) / 1000);
    double maxHealth = entity.getMaxHealth() *
        (1 + (caster.getStat(StrifeStat.MINION_LIFE) / 1000));
    entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    entity.setHealth(maxHealth);

    getPlugin().getSoulManager().removeSoul(soul);

    new BukkitRunnable() {
      public void run() {
        if (entity.isValid()) {
          Player p = soul.getOwner();
          if (p.isOnline() && p.isValid()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 25, 4));
          }
        } else {
          cancel();
        }
      }
    }.runTaskTimer(getPlugin(), 0, 20);

    List<StrifeMob> minionList = new ArrayList<>(caster.getMinions());

    int excessMinions = minionList.size() - (int) caster.getStat(StrifeStat.MAX_MINIONS);
    if (excessMinions > 0) {
      minionList.sort(Comparator.comparingDouble(StrifeMob::getMinionRating));
      while (excessMinions > 0) {
        minionList.get(excessMinions - 1).minionDeath();
        //Bukkit.getLogger().info("commit die: " + minionList.get(excessMinions - 1).getEntity().getName());
        excessMinions--;
      }
    }
  }

  public void setLifeSeconds(int lifeSeconds) {
    this.lifeSeconds = lifeSeconds;
  }

  public void setName(String name) {
    this.name = name;
  }
}
