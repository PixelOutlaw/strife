package land.face.strife.data.champion;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.awt.Color;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting", "\uD86D\uDF5A", FaceColor.YELLOW.getColor()),
  ENCHANTING("Enchanting", "enchant", "\uD86D\uDF63", ChatColor.of(new Color(113, 79, 236))),
  FISHING("Fishing", "fishing", "\uD86D\uDF64", FaceColor.CYAN.getColor()),
  MINING("Mining", "mining", "\uD86D\uDF59", FaceColor.GRAY.getColor()),
  FARMING("Gathering", "farming", "\uD86D\uDF57", FaceColor.ORANGE.getColor()),
  COOKING("Cooking", "cooking", "\uD86D\uDF67", FaceColor.BROWN.getColor()),
  ALCHEMY("Alchemy", "alchemy", "\uD86D\uDF66", FaceColor.TEAL.getColor()),
  SNEAK("Sneak", "sneak", "\uD86D\uDF69", FaceColor.GRAY.getColor()),
  AGILITY("Agility", "agility", "\uD86D\uDF5D", FaceColor.TEAL.getColor()),
  TRADING("Trading", "trading", "\uD86D\uDF68", FaceColor.GREEN.getColor()),
  FLYING("Flying", "flying", "\uD86D\uDF58", ChatColor.of(new Color(114, 187, 255))),
  PRAYER("Prayer", "prayer", "\uD86D\uDF6A", ChatColor.of(new Color(202, 255, 245))),

  SWORDSMANSHIP("Swordsmanship", "sword", "\uD86D\uDF54", FaceColor.ORANGE.getColor(), true),
  DAGGER_MASTERY("Dagger Mastery", "dagger", "", ChatColor.of(new Color(204, 246, 102)), true),
  AXE_MASTERY("Axe Mastery", "axe", "\uD86D\uDF65", FaceColor.RED.getColor(), true),
  BLUNT_WEAPONS("Blunt Weapons", "blunt", "\uD86D\uDF5E", FaceColor.YELLOW.getColor(), true),
  DUAL_WIELDING("Dual Wielding", "dual", "\uD86D\uDF56", FaceColor.LIME.getColor(), true),
  SHIELD_MASTERY("Shield Mastery", "shield", "\uD86D\uDF55", FaceColor.BROWN.getColor(), true),
  ARCHERY("Archery", "archery", "\uD86D\uDF5C", FaceColor.GREEN.getColor(), true),
  MARKSMANSHIP("Marksmanship", "marksmanship", "\uD86D\uDF5B", FaceColor.LIME.getColor(), true),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", "\uD86D\uDF61", FaceColor.BLUE.getColor(), true),
  NATURAL_MAGICS("Natural Magics", "natural-magic", "\uD86D\uDF60", FaceColor.GREEN.getColor(), true),
  BLACK_MAGICS("Black Magics", "dark-magics", "\uD86D\uDF5F", FaceColor.PURPLE.getColor(), true),
  CELESTIAL_MAGICS("Holy Magic", "light-magic", "\uD86D\uDF62", FaceColor.WHITE.getColor(), true);

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
