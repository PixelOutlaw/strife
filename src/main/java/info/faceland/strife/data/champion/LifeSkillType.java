package info.faceland.strife.data.champion;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting"),
  ENCHANTING("Enchanting", "enchant"),
  FISHING("Fishing", "fishing"),
  MINING("Mining", "mining"),
  SNEAK("Sneak", "sneak"),
  SWORDSMANSHIP("Swordsmanship", "sword"),
  AXE_MASTERY("Axe Mastery", "axe"),
  DUAL_WIELDING("Dual Wielding", "dual"),
  SHIELD_MASTERY("Shield Mastery", "shield"),
  ARCHERY("Archery", "archery"),
  MAGECRAFT("Magecraft", "magic");

  public final static LifeSkillType[] types = LifeSkillType.values();

  private final String prettyName;
  private final String dataName;

  LifeSkillType(String prettyName, String dataName) {
    this.prettyName = prettyName;
    this.dataName = dataName;
  }

  public String getName() {
    return prettyName;
  }

  public String getDataName() {
    return dataName;
  }
}
