package info.faceland.strife.data;

import static org.bukkit.Bukkit.getLogger;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.tasks.EffectTask;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Ability {

  private String id;
  private String name;
  private TargetType targetType;
  private double range;
  private List<Effect> effects;
  private long readyTime;
  private int cooldown;

  private static final String ON_COOLDOWN = TextUtils.color("&f&lAbility On Cooldown!");
  private static final String NO_TARGET = TextUtils.color("&e&lNo Target Found!");
  private static final String TEST_CHICKEN = ChatColor.RED + "TEST CHICKEN PLS IGNORE";

  public Ability(String name, List<Effect> effects, TargetType targetType, double range, int cooldown) {
    this.name = name;
    this.cooldown = cooldown;
    this.effects = effects;
    this.targetType = targetType;
    this.range = range;
    this.readyTime = System.currentTimeMillis();
  }

  public void execute(final AttributedEntity caster) {
    getLogger().info("executing ability");
    if (System.currentTimeMillis() < readyTime) {
      getLogger().info("nvm on cd");
      if (caster instanceof Player) {
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ON_COOLDOWN, (Player) caster);
      }
      return;
    }
    LivingEntity target = getTarget(caster);
    if (target == null) {
      getLogger().info("No target, ignoring");
      if (caster instanceof Player) {
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, NO_TARGET, (Player) caster);
      }
      return;
    }
    readyTime = System.currentTimeMillis() + cooldown * 1000;
    List<Effect> taskActions = new ArrayList<>();
    int waitTicks = 0;
    for (Effect action : effects) {
      if (!(action instanceof Wait)) {
        taskActions.add(action);
      } else {
        waitTicks += ((Wait) action).getTickDelay();
        new EffectTask(caster, target, taskActions).runTaskLater(StrifePlugin.getInstance(), waitTicks);
        taskActions.clear();
      }
    }
    new EffectTask(caster, target, taskActions).run();
    if (TEST_CHICKEN.equals(target.getCustomName())) {
      target.remove();
    }
  }

  private LivingEntity getTarget(AttributedEntity caster) {
    switch (targetType) {
      case SELF:
        return caster.getEntity();
      case OTHER:
        return selectFirstEntityInSight(caster.getEntity(), (int) range);
      case RANGE:
        LivingEntity target = selectFirstEntityInSight(caster.getEntity(), (int) range);
        if (target == null) {
          target = getBackupEntity(caster.getEntity());
        }
        return target;
    }
    return null;
  }

  private LivingEntity selectFirstEntityInSight(LivingEntity caster, int range) {
    if (caster instanceof Creature && ((Creature) caster).getTarget() != null) {
      return ((Creature) caster).getTarget();
    }
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    ArrayList<Block> sightBlock = (ArrayList<Block>) caster.getLineOfSight(null, range);
    ArrayList<Location> sight = new ArrayList<>();
    for (Block b : sightBlock) {
      sight.add(b.getLocation());
    }
    for (Location loc : sight) {
      for (Entity entity : entities) {
        if (!(entity instanceof LivingEntity)) {
          continue;
        }
        if (Math.abs(entity.getLocation().getX() - loc.getX()) < 1.3) {
          if (Math.abs(entity.getLocation().getY() - loc.getY()) < 1.5) {
            if (Math.abs(entity.getLocation().getZ() - loc.getZ()) < 1.3) {
              return (LivingEntity) entity;
            }
          }
        }
      }
    }
    return null;
  }

  private LivingEntity getBackupEntity(LivingEntity caster) {
    Block block = caster.getTargetBlock(null, (int) range);
    Location location;
    if (block != null) {
      location = block.getLocation().clone();
      location.subtract(caster.getLocation().getDirection().normalize().multiply(0.65));
    } else {
      location = caster.getLocation().clone();
      location.add(location.getDirection().normalize().multiply(range - 0.65));
    }
    LivingEntity chicken = (Chicken) location.getWorld().spawnEntity(location, EntityType.CHICKEN);
    chicken.setCustomNameVisible(true);
    chicken.setCustomName(TEST_CHICKEN);
    return chicken;
  }

  public enum TargetType {
    SELF, OTHER, RANGE, NONE
  }
}
