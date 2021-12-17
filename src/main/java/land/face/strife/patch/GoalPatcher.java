package land.face.strife.patch;

import java.util.List;
import land.face.strife.data.UniqueEntity;
import land.face.strife.patch.FloatBehindGoalPatcher.FloatBehindGoal;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Mob;

public class GoalPatcher {

  public static void addGoals(Mob mob, UniqueEntity uniqueEntity) {
    for (String goal : uniqueEntity.getAddGoals()) {
      switch (mob.getType()) {
        case BAT -> addGoal(mob, goal, uniqueEntity.getAttackSound(), 1.75f,
            uniqueEntity.getFollowRange(), uniqueEntity.isAggressiveAi(), true);
        case TROPICAL_FISH, PUFFERFISH, COD, SALMON -> FishPatcher.patchFish((Fish) mob);
        default -> addGoal(mob, goal, uniqueEntity.getAttackSound(), 2.25f,
            uniqueEntity.getFollowRange(), uniqueEntity.isAggressiveAi(), false);
      }
    }
  }

  public static void addGoal(Mob mob, String id, Sound sound, float attackRange, float followRange,
      boolean aggressive, boolean flying) {
    switch (id) {
      case "strife:return_home" -> {
        ReturnHomeGoal returnHomeGoal = new ReturnHomeGoal(mob, Math.pow(followRange + 3, 2));
        if (!Bukkit.getMobGoals().hasGoal(mob, returnHomeGoal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 0, returnHomeGoal);
        }
      }
      case "strife:attack_target" -> {
        AttackTargetGoal goal = new AttackTargetGoal(mob, sound, attackRange);
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
      case "strife:follow_master" -> {
        FollowMasterGoal goal = new FollowMasterGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goal);
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
    Bukkit.getMobGoals().getAllGoals(mob).forEach(goal -> {
      String goalName = goal.getKey().getNamespacedKey().toString();
      if (removeKeys.contains(goalName)) {
        Bukkit.getMobGoals().removeGoal(mob, goal);
      }
    });
  }
}
