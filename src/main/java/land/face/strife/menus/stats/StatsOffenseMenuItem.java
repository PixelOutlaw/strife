/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.menus.stats;

import static land.face.strife.menus.stats.StatsMenu.INT_FORMAT;
import static land.face.strife.menus.stats.StatsMenu.TWO_DECIMAL;
import static land.face.strife.menus.stats.StatsMenu.breakLine;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.StatUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsOffenseMenuItem extends MenuItem {

  private final StatsMenu statsMenu;

  private final Map<Player, ItemStack> cachedIcon = new HashMap<>();
  private final float POISON_FLAT_DAMAGE;
  private final float POISON_LEVEL_DAMAGE;


  StatsOffenseMenuItem(StatsMenu statsMenu) {
    super(FaceColor.ORANGE.s() + FaceColor.BOLD.s() + "Damage Stats", new ItemStack(Material.BARRIER));
    this.statsMenu = statsMenu;
    ItemStackExtensionsKt.setCustomModelData(getIcon(), 50);
    POISON_FLAT_DAMAGE = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.poison-flat-damage");
    POISON_LEVEL_DAMAGE = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.mechanics.poison-level-damage");
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = statsMenu.getInspectionTargetMap().get(commandSender);
    if (!player.isValid()) {
      return getIcon();
    }
    if (cachedIcon.containsKey(player)) {
      return cachedIcon.get(player);
    }
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(player);
    Map<StrifeStat, Float> bases = StrifePlugin.getInstance().getMonsterManager()
        .getBaseStats(EntityType.PLAYER, player.getLevel());

    AttackType type;
    if (player.getEquipment().getItemInMainHand().getType() == Material.BOW) {
      type = AttackType.PROJECTILE;
    } else if (ItemUtil.isWandOrStaff(player.getEquipment().getItemInMainHand())) {
      type = AttackType.PROJECTILE;
    } else {
      type = AttackType.MELEE;
    }

    Map<DamageType, Float> damageMap = DamageUtil.buildDamageMap(mob, null, new DamageModifiers());
    DamageUtil.applyAttackTypeMods(mob, type, damageMap);

    List<Entry<DamageType, Float>> retardMap = new ArrayList<>(damageMap.entrySet());
    retardMap.sort(Entry.comparingByValue());
    Collections.reverse(retardMap);

    ItemStack itemStack = getIcon().clone();
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(getDisplayName());
    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    List<String> lore = new ArrayList<>();

    float totalMultiplier = 1f;
    if (type == AttackType.PROJECTILE) {
      totalMultiplier *= 1f + mob.getStat(StrifeStat.PROJECTILE_DAMAGE) / 100;
    }
    float elementalMult = 1 + mob.getStat(StrifeStat.ELEMENTAL_MULT) / 100;
    float physical = totalMultiplier * damageMap.getOrDefault(DamageType.PHYSICAL, 0f);
    float fire = totalMultiplier * damageMap.getOrDefault(DamageType.FIRE, 0f) * elementalMult;
    float ice = totalMultiplier * damageMap.getOrDefault(DamageType.ICE, 0f) * elementalMult;
    float lightning = totalMultiplier * damageMap.getOrDefault(DamageType.LIGHTNING, 0f) * elementalMult;
    float earth = totalMultiplier * damageMap.getOrDefault(DamageType.EARTH, 0f) * elementalMult;
    float light = totalMultiplier * damageMap.getOrDefault(DamageType.LIGHT, 0f) * elementalMult;
    float shadow = totalMultiplier * damageMap.getOrDefault(DamageType.DARK, 0f)  * elementalMult;
    float trueDmg = damageMap.getOrDefault(DamageType.TRUE_DAMAGE, 0f) - 1;
    float total = physical + fire + ice + lightning + earth + light + shadow + trueDmg;

    lore.add(breakLine);

    lore.add(addStat("Total Damage: ", total, INT_FORMAT));
    StringBuilder damageDisplay = new StringBuilder();

    for (Entry<DamageType, Float> entry : retardMap) {
      if (entry.getValue() < 1) {
        continue;
      }
      switch (entry.getKey()) {
        case PHYSICAL -> addIfApplicable(damageDisplay, physical, FaceColor.RED, FaceColor.WHITE + "儀");
        case FIRE -> addIfApplicable(damageDisplay, fire, FaceColor.ORANGE, FaceColor.WHITE + "儂");
        case ICE -> addIfApplicable(damageDisplay, ice, FaceColor.CYAN, FaceColor.WHITE + "儃");
        case LIGHTNING -> addIfApplicable(damageDisplay, lightning, FaceColor.YELLOW, FaceColor.WHITE + "億");
        case LIGHT -> addIfApplicable(damageDisplay, light, FaceColor.WHITE, FaceColor.WHITE + "儆");
        case EARTH -> addIfApplicable(damageDisplay, earth, FaceColor.BROWN, FaceColor.WHITE + "儅");
        case DARK -> addIfApplicable(damageDisplay, shadow, FaceColor.PURPLE, FaceColor.WHITE + "儇");
        case TRUE_DAMAGE -> addIfApplicable(damageDisplay, trueDmg, FaceColor.LIGHT_GRAY, FaceColor.WHITE + "儈");
      }
    }

    lore.add(damageDisplay.toString());
    float critMult = mob.getStat(StrifeStat.CRITICAL_DAMAGE) / 100;
    if (!mob.hasTrait(StrifeTrait.NO_CRIT_MULT) || critMult < 0.1) {
      float critDamage = physical + fire + ice + lightning + earth + light + shadow;
      float critChance = Math.min(1, Math.max(mob.getStat(StrifeStat.CRITICAL_RATE), 0) / 100);
      total += critChance * critDamage * critMult;
    }
    if (mob.getStat(StrifeStat.BLEED_CHANCE) > 0) {
      float bleedBonus = 0.5f * (1 + mob.getStat(StrifeStat.BLEED_DAMAGE) / 100);
      bleedBonus *= mob.getStat(StrifeStat.BLEED_CHANCE) / 100;
      total += (physical + physical * (1 + critMult)) * bleedBonus;
    }
    if (mob.getStat(StrifeStat.POISON_CHANCE) > 0) {
      float poisonDmg = POISON_FLAT_DAMAGE + mob.getLevel() * POISON_LEVEL_DAMAGE;
      poisonDmg *= 5;
      poisonDmg *= (float) Math.pow(mob.getStat(StrifeStat.POISON_CHANCE) / 100, 2);
      total += poisonDmg;
    }
    if (mob.getStat(StrifeStat.MULTISHOT) > 0) {
      total *= (float) (1 + (0.3 * (mob.getStat(StrifeStat.MULTISHOT) / 100)));
    }
    float dps = total * (1 / StatUtil.getAttackTime(mob));
    lore.add(addStat("Estimated DPS: ", dps, " Damage", INT_FORMAT));
    lore.add(addStat("Accuracy Rating: ", 90 + mob.getStat(StrifeStat.ACCURACY), INT_FORMAT));
    String as = addStat("Attack Speed: ", StatUtil.getAttackTime(mob), "s", TWO_DECIMAL);
    float attackSpeed = mob.getStat(StrifeStat.ATTACK_SPEED);
    String bonus = FaceColor.GRAY + " (" + (attackSpeed >= 0 ? "+" : "") + INT_FORMAT.format(attackSpeed) + "%)";
    lore.add(as + bonus);
    lore.add(addStat("Ability Damage: ", 100 + mob.getStat(StrifeStat.ABILITY_DAMAGE), "%", INT_FORMAT));
    List<String> loreSection = new ArrayList<>();
    loreSection.add(breakLine);
    if (mob.getStat(StrifeStat.MULTISHOT) > 0) {
      if (mob.getStat(StrifeStat.DOGE) > 0) {
        loreSection.add(addStat("MultiTHOT: ", mob.getStat(StrifeStat.MULTISHOT), "%", INT_FORMAT));
      } else {
        loreSection.add(addStat("Multishot: ", mob.getStat(StrifeStat.MULTISHOT), "%", INT_FORMAT));
      }
    }
    if (mob.getStat(StrifeStat.CRITICAL_RATE) > 0 && !mob.hasTrait(StrifeTrait.NO_CRIT_MULT)) {
      loreSection.add(
          addStat("Critical Chance: ", mob.getStat(StrifeStat.CRITICAL_RATE), "%", INT_FORMAT));
      loreSection.add(
          addStat("Critical Multiplier: ", StatUtil.getCriticalMultiplier(mob), "x", TWO_DECIMAL));
    }
    double aPen = mob.getStat(StrifeStat.ARMOR_PENETRATION) - bases.getOrDefault(StrifeStat.ARMOR_PENETRATION, 0f);
    if (aPen != 0) {
      loreSection.add(addStat("Armor Penetration: " + ChatColor.WHITE + plus(aPen), aPen, INT_FORMAT));
    }
    if (mob.getStat(StrifeStat.BLEED_CHANCE) > 0) {
      loreSection.add(addStat("Bleed Chance: ", mob.getStat(StrifeStat.BLEED_CHANCE), "%", INT_FORMAT));
    }
    if (mob.getStat(StrifeStat.BLEED_DAMAGE) > 0) {
      loreSection.add(addStat("Bleed Damage: " + ChatColor.WHITE + "+",
          mob.getStat(StrifeStat.BLEED_DAMAGE), "%", INT_FORMAT));
    }
    if (mob.getStat(StrifeStat.POISON_CHANCE) > 0) {
      loreSection.add(addStat("Poison Chance: ", mob.getStat(StrifeStat.POISON_CHANCE), "%", INT_FORMAT));
      loreSection.add(addStat("Poison Duration: " + ChatColor.WHITE + "+", mob.getStat(StrifeStat.POISON_DURATION), "%", INT_FORMAT));
    }
    if (mob.getStat(StrifeStat.RAGE_ON_HIT) > 0 || mob.getStat(StrifeStat.RAGE_ON_KILL) > 0
        || mob.getStat(StrifeStat.RAGE_WHEN_HIT) > 0) {
      loreSection.add(breakLine);
      loreSection.add(addStat("Maximum Rage: ", mob.getStat(StrifeStat.MAXIMUM_RAGE), INT_FORMAT));
      loreSection.add(addStat("Rage On Hit: ", mob.getStat(StrifeStat.RAGE_ON_HIT), INT_FORMAT));
      loreSection.add(addStat("Rage On Kill: ", mob.getStat(StrifeStat.RAGE_ON_KILL), INT_FORMAT));
      loreSection.add(addStat("Rage When Hit: ", mob.getStat(StrifeStat.RAGE_WHEN_HIT), INT_FORMAT));
    }
    if (loreSection.size() > 1) {
      lore.addAll(loreSection);
    }
    if (mob.getStat(StrifeStat.HP_ON_HIT) > 0 || mob.getStat(StrifeStat.LIFE_STEAL) > 0
        || mob.getStat(StrifeStat.HP_ON_KILL) > 0) {
      lore.add(breakLine);
      if (mob.getStat(StrifeStat.LIFE_STEAL) > 0) {
        lore.add(addStat("Life Steal: ", mob.getStat(StrifeStat.LIFE_STEAL), "%", INT_FORMAT));
      }
      if (mob.getStat(StrifeStat.HP_ON_HIT) > 0) {
        lore.add(addStat("Life On Hit: ", mob.getStat(StrifeStat.HP_ON_HIT), INT_FORMAT));
      }
      if (mob.getStat(StrifeStat.HP_ON_KILL) > 0) {
        lore.add(addStat("Life On Kill: ", mob.getStat(StrifeStat.HP_ON_KILL), INT_FORMAT));
      }
    }
    lore.add(breakLine);
    if (earth > 0) {
      lore.add(addStat("Maximum Earth Runes: ", mob.getStat(StrifeStat.MAX_EARTH_RUNES), INT_FORMAT));
    }
    lore.add(addStat("Elemental Status Chance: ", mob.getStat(StrifeStat.ELEMENTAL_STATUS), "%", INT_FORMAT));
    lore.add(breakLine);
    lore.add(StringExtensionsKt.chatColorize("&8&oUse &7&o/help stats &8&ofor info!"));
    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);

    cachedIcon.put(player, itemStack);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> cachedIcon.remove(player), 2);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }

  private static void addIfApplicable(StringBuilder builder, float value, FaceColor color, String symbol) {
    if (value < 0.5) {
      return;
    }
    builder.append(color.s()).append(" ").append((int) value).append(symbol);
  }

  private String addStat(String name, double value, DecimalFormat format) {
    return FaceColor.ORANGE.getColor() + name + FaceColor.WHITE.getColor() + format.format(value);
  }

  private String addStat(String name, double value, String extra, DecimalFormat format) {
    return FaceColor.ORANGE.getColor() + name + FaceColor.WHITE.getColor() + format.format(value) + extra;
  }

  private String plus(double num) {
    return num >= 0 ? "+" : "";
  }
}
