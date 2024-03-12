package land.face.strife.data.champion;

import land.face.strife.data.StrifeMob;

public enum SkillRank {
  NOVICE, // 0-19
  APPRENTICE, // 20-39
  JOURNEYMAN, // 40-59
  EXPERT, // 50-79
  MASTER; // 80+

  public static boolean check(StrifeMob mob, LifeSkillType type, SkillRank rank) {
    return check(mob.getChampion(), type, rank);
  }

  public static boolean check(Champion champion, LifeSkillType type, SkillRank rank) {
    ChampionSaveData data = champion.getSaveData();
    return switch (rank) {
      case NOVICE -> true;
      case APPRENTICE -> data.getSkillLevel(type) >= 20;
      case JOURNEYMAN -> data.getSkillLevel(type) >= 40;
      case EXPERT -> data.getSkillLevel(type) >= 60;
      case MASTER -> data.getSkillLevel(type) >= 80;
    };
  }
}
