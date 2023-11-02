package land.face.strife.patch;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.UniqueEntity;
import land.face.strife.patch.FloatBehindGoalPatcher.FloatBehindGoal;
import land.face.strife.util.SpecialStatusUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Mob;

public class GoalPatcher {

  public static void addGoals(Mob mob, UniqueEntity uniqueEntity) {
    for (String goal : uniqueEntity.getAddGoals()) {
      switch (mob.getType()) {
        case BAT -> addGoal(mob, goal, 1.75f);
        case TROPICAL_FISH, PUFFERFISH, COD, SALMON -> FishPatcher.patchFish((Fish) mob);
        default -> addGoal(mob, goal, 2.25f);
      }
    }
  }

  public static void addGoal(Mob mob, String id, float range) {
    switch (id) {
      case "strife:attack_target" -> {
        AttackTargetGoal goal = new AttackTargetGoal(mob, range);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 1, goal);
        }
      }
      case "strife:target_players" -> {
        TargetPlayersGoal goal = new TargetPlayersGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 2, goal);
        }
      }
      case "strife:chase_target" -> {
        ChaseTargetGoal goal = new ChaseTargetGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 2, goal);
        }
      }
      case "strife:watch_target" -> {
        WatchTargetGoal goal = new WatchTargetGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goal);
        }
      }
      case "strife:origin_lock" -> {
        OriginLockGoal goal = new OriginLockGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goal);
        }
      }
      case "strife:follow_master" -> {
        FollowMasterGoal goal = new FollowMasterGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goal);
        }
      }
      case "strife:flee_nearest" -> {
        FleeNearestGoal goal = new FleeNearestGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 1, goal);
        }
      }
      case "strife:target_factions" -> {
        String uniqueId = SpecialStatusUtil.getUniqueId(mob);
        //Bukkit.getLogger().info("a");
        if (!StringUtils.isBlank(uniqueId)) {
          //Bukkit.getLogger().info("b");
          UniqueEntity ue = StrifePlugin.getInstance().getUniqueEntityManager()
              .getUnique(SpecialStatusUtil.getUniqueId(mob));
          if (ue != null) {
            TargetEnemyFaction goal = new TargetEnemyFaction(mob, ue.getEnemyUniques());
            //Bukkit.getLogger().info("c" + ue.getEnemyUniques());
            if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
              Bukkit.getMobGoals().addGoal(mob, 1, goal);
            }
          }
        }
      }
      case "strife:float_behind_master" -> {
        FloatBehindGoal goal = new FloatBehindGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goal);
        }
      }
      default -> Bukkit.getLogger().warning("Custom goal " + id + " not found! Not adding...");
    }
  }

  public static void removeGoals(Mob mob, List<String> removeKeys) {
    if (removeKeys.isEmpty()) {
      return;
    }
    Bukkit.getMobGoals().getAllGoals(mob).forEach(goal -> {
      String goalName = goal.getKey().getNamespacedKey().toString();
      if (removeKeys.contains(goalName)) {
        Bukkit.getMobGoals().removeGoal(mob, goal);
      }
    });
  }
}
