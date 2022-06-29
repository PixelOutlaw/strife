package land.face.strife.data.champion;

import java.awt.Color;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting", "\uD86D\uDF5A", ChatColor.YELLOW),
  ENCHANTING("Enchanting", "enchant", "\uD86D\uDF63", ChatColor.of(new Color(113, 79, 236))),
  FISHING("Fishing", "fishing", "\uD86D\uDF64", ChatColor.AQUA),
  MINING("Mining", "mining", "\uD86D\uDF59", ChatColor.DARK_GREEN),
  FARMING("Gathering", "farming", "\uD86D\uDF57", ChatColor.of(new Color(255, 192, 87))),
  COOKING("Cooking", "cooking", "\uD86D\uDF59", ChatColor.of(new Color(219, 117, 74))),
  ALCHEMY("Alchemy", "alchemy", "\uD86D\uDF59", ChatColor.GREEN),
  SNEAK("Sneak", "sneak", "\uD86D\uDF59", ChatColor.GRAY),
  AGILITY("Agility", "agility", "\uD86D\uDF5D", ChatColor.DARK_AQUA),
  TRADING("Trading", "trading", "\uD86D\uDF59", ChatColor.DARK_GREEN),
  FLYING("Flying", "flying", "\uD86D\uDF58", ChatColor.of(new Color(114, 187, 255))),
  SWORDSMANSHIP("Swordsmanship", "sword", "\uD86D\uDF54", ChatColor.RED, true),
  DAGGER_MASTERY("Dagger Mastery", "dagger", "", ChatColor.of(new Color(204, 246, 102)), true),
  AXE_MASTERY("Axe Mastery", "axe", "\uD86D\uDF65", ChatColor.RED, true),
  BLUNT_WEAPONS("Blunt Weapons", "blunt", "\uD86D\uDF5E", ChatColor.RED, true),
  DUAL_WIELDING("Dual Wielding", "dual", "\uD86D\uDF56", ChatColor.GREEN, true),
  SHIELD_MASTERY("Shield Mastery", "shield", "\uD86D\uDF55", ChatColor.YELLOW, true),
  ARCHERY("Archery", "archery", "\uD86D\uDF5C", ChatColor.DARK_GREEN, true),
  MARKSMANSHIP("Marksmanship", "marksmanship", "\uD86D\uDF5B", ChatColor.of(new Color(99, 231, 113)), true),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", "\uD86D\uDF61", ChatColor.BLUE, true),
  NATURAL_MAGICS("Natural Magics", "natural-magic", "\uD86D\uDF60", ChatColor.DARK_AQUA, true),
  BLACK_MAGICS("Black Magics", "dark-magics", "\uD86D\uDF5F", ChatColor.DARK_PURPLE, true),
  CELESTIAL_MAGICS("Holy Magic", "light-magic", "\uD86D\uDF62", ChatColor.of(new Color(255, 235, 214)), true);

  public final static LifeSkillType[] types = LifeSkillType.values();

  @Getter
  private final String prettyName;
  @Getter
  private final String dataName;
  @Getter
  private final String character;
  @Getter
  private final ChatColor color;
  @Getter
  private final boolean combat;

  LifeSkillType(String prettyName, String dataName, String character, ChatColor color, boolean combat) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.character = character;
    this.color = color;
    this.combat = combat;
  }

  LifeSkillType(String prettyName, String dataName, String character, ChatColor color) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.character = character;
    this.color = color;
    this.combat = false;
  }
}
