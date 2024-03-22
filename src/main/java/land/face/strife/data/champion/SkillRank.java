package land.face.strife.data.champion;

import land.face.strife.data.StrifeMob;

public enum SkillRank {
  NOVICE, // 0-19
  APPRENTICE, // 20-39
  JOURNEYMAN, // 40-59
  EXPERT, // 50-79
  MASTER; // 80+

  public static boolean isRank(StrifeMob mob, LifeSkillType type, SkillRank rank) {
    return isRank(mob.getChampion(), type, rank);
  }

  public static boolean isRank(Champion champion, LifeSkillType type, SkillRank rank) {
    if (champion == null) {
      return false;
    }
    return isRank(champion.getSaveData().getSkillLevel(type), rank);
  }

  public static boolean isRank(SkillRank currentRank, SkillRank checkRank) {
    return currentRank.ordinal() >= checkRank.ordinal();
  }

  public static boolean isRank(int level, SkillRank rank) {
    return switch (rank) {
      case NOVICE -> true;
      case APPRENTICE -> level >= 20;
      case JOURNEYMAN -> level >= 40;
      case EXPERT -> level >= 60;
      case MASTER -> level >= 80;
    };
  }

  public static SkillRank getRank(StrifeMob mob, LifeSkillType type) {
    return getRank(mob.getChampion(), type);
  }

  public static SkillRank getRank(Champion champion, LifeSkillType type) {
    if (champion != null) {
      ChampionSaveData data = champion.getSaveData();
      if (data.getSkillLevel(type) < 20) {
        return NOVICE;
      }
      if (data.getSkillLevel(type) < 40) {
        return APPRENTICE;
      }
      if (data.getSkillLevel(type) < 60) {
        return JOURNEYMAN;
      }
      if (data.getSkillLevel(type) < 80) {
        return EXPERT;
      }
      return MASTER;
    }
    return NOVICE;
  }
}
