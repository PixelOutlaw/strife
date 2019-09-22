package info.faceland.strife.data.champion;

import org.bukkit.ChatColor;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting", ChatColor.YELLOW),
  ENCHANTING("Enchanting", "enchant", ChatColor.LIGHT_PURPLE),
  FISHING("Fishing", "fishing", ChatColor.AQUA),
  MINING("Mining", "mining", ChatColor.DARK_GREEN),
  FARMING("Farming", "farming", ChatColor.GOLD),
  SNEAK("Sneak", "sneak", ChatColor.GRAY),
  SWORDSMANSHIP("Swordsmanship", "sword", ChatColor.RED),
  AXE_MASTERY("Axe Mastery", "axe", ChatColor.RED),
  DUAL_WIELDING("Dual Wielding", "dual", ChatColor.GREEN),
  SHIELD_MASTERY("Shield Mastery", "shield", ChatColor.YELLOW),
  ARCHERY("Archery", "archery", ChatColor.DARK_GREEN),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", ChatColor.BLUE),
  NATURAL_MAGICS("Natural Magics", "natural-magic", ChatColor.DARK_AQUA),
  BLACK_MAGICS("Black Magics", "dark-magics", ChatColor.DARK_PURPLE),
  CELESTIAL_MAGICS("Celestial Magics", "light-magic", ChatColor.WHITE);

  public final static LifeSkillType[] types = LifeSkillType.values();

  private final String prettyName;
  private final String dataName;
  private final ChatColor color;

  LifeSkillType(String prettyName, String dataName, ChatColor color) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.color = color;
  }

  public String getName() {
    return prettyName;
  }

  public String getDataName() {
    return dataName;
  }

  public ChatColor getColor() {
    return color;
  }
}
