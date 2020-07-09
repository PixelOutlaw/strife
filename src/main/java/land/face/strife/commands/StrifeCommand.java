/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendActionBar;
import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.EloResponse;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.EloUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;
import se.ranzdo.bukkit.methodcommand.FlagArg;
import se.ranzdo.bukkit.methodcommand.Flags;

public class StrifeCommand {

  private final StrifePlugin plugin;
  private final String REVEAL_SUCCESS;
  private final String REVEAL_FAIL;
  private final String REVEAL_PREFIX;
  private final String REVEAL_REPLACEMENT;
  private final String XP_MSG;
  private final String SET_LEVEL_MSG;
  private final String PVP_WIN_MSG;
  private final String PVP_LOSE_MSG;

  public StrifeCommand(StrifePlugin plugin) {
    this.plugin = plugin;
    REVEAL_SUCCESS = plugin.getSettings()
        .getString("language.command.reveal-success", "Reveal success");
    REVEAL_FAIL = plugin.getSettings()
        .getString("language.command.reveal-fail", "Reveal failure");
    REVEAL_PREFIX = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.command.reveal-prefix", "&0&k"));
    REVEAL_REPLACEMENT = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.command.reveal-replace", "&f"));
    XP_MSG = plugin.getSettings()
        .getString("language.skills.xp-msg", "{c}Gained &f{n} {c}XP! &f(+{a}XP)");
    SET_LEVEL_MSG = plugin.getSettings()
        .getString("language.command.set-level-msg", "{c}Your level in &f{n} {c}is now &f{a}{c}!");
    PVP_WIN_MSG = plugin.getSettings()
        .getString("language.pvp.gain-score-action-bar", "&aGained PvP Rating! &f{0} (+{1})");
    PVP_LOSE_MSG = plugin.getSettings()
        .getString("language.pvp.lose-score-action-bar", "&eLost PvP Rating... &f{0} ({1})");
  }

  @Command(identifier = "strife defeat", permissions = "strife.command.strife.defeat", onlyPlayers = false)
  public void defeatCommand(CommandSender sender, @Arg(name = "winner") Player winner,
      @Arg(name = "loser") Player loser, @Arg(name = "weight") double weight) {
    ChampionSaveData winData = plugin.getChampionManager().getChampion(winner).getSaveData();
    ChampionSaveData loseData = plugin.getChampionManager().getChampion(loser).getSaveData();

    EloResponse response = EloUtil
        .getEloChange(winData.getPvpScore(), loseData.getPvpScore(), (float) weight);

    float winDiff = response.getNewWinnerValue() - winData.getPvpScore();
    float loseDiff = response.getNewLoserValue() - loseData.getPvpScore();

    winData.setPvpScore(response.getNewWinnerValue());
    loseData.setPvpScore(response.getNewLoserValue());

    sendActionBar(winner,
        PVP_WIN_MSG.replace("{0}", String.valueOf(Math.round(response.getNewWinnerValue())))
            .replace("{1}", String.valueOf(Math.round(winDiff))));
    sendActionBar(loser,
        PVP_LOSE_MSG.replace("{0}", String.valueOf(Math.round(response.getNewLoserValue())))
            .replace("{1}", String.valueOf(Math.round(loseDiff))));
  }

  @Command(identifier = "strife reload", permissions = "strife.command.strife.reload", onlyPlayers = false)
  public void reloadCommand(CommandSender sender) {
    // Save player data before reload continues
    plugin.getStorage().saveAll();

    // Normal enable/disable
    plugin.disable();
    plugin.enable();

    for (Player player : Bukkit.getOnlinePlayers()) {
      plugin.getStatUpdateManager().updateAttributes(player);
    }

    sendMessage(sender,
        plugin.getSettings().getString("language.command.reload", "&aStrife reloaded!"));
  }

  @Command(identifier = "strife profile", permissions = "strife.command.strife.profile", onlyPlayers = false)
  public void profileCommand(CommandSender sender, @Arg(name = "target") Player target) {
    Champion champion = plugin.getChampionManager().getChampion(target);
    sendMessage(sender, "<gold>----------------------------------");
    sendMessage(sender, "<gray>Unused Stat Points: <white>%amount%",
        new String[][]{{"%amount%", "" + champion.getUnusedStatPoints()}});
    sendMessage(sender, "<gold>----------------------------------");
    for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
      sendMessage(sender,
          ChatColor.GRAY + stat.getKey() + " - " + champion.getAttributeLevel(stat));
    }
    sendMessage(sender, "<gold>----------------------------------");
  }

  @Command(identifier = "strife mobinfo", permissions = "strife.command.strife.info")
  public void infoCommand(CommandSender sender) {
    List<LivingEntity> targets = new ArrayList<>(
        TargetingUtil.getEntitiesInLine(((Player) sender).getEyeLocation(), 30));
    if (targets.isEmpty()) {
      sendMessage(sender, "&eNo target found...");
      return;
    }
    TargetingUtil.DISTANCE_COMPARATOR.setLoc(((Player) sender).getLocation());
    targets.sort(TargetingUtil.DISTANCE_COMPARATOR);
    StrifeMob targetMob = plugin.getStrifeMobManager().getStatMob(targets.get(0));
    sendMessage(sender, "&aUniqueID: " + targetMob.getUniqueEntityId());
    sendMessage(sender, "&aGroups: " + Arrays.toString(targetMob.getFactions().toArray()));
  }

  @Command(identifier = "strife reset", permissions = "strife.command.strife.reset", onlyPlayers = false)
  public void resetCommand(CommandSender sender, @Arg(name = "target") Player target) {
    Champion champion = plugin.getChampionManager().getChampion(target);
    for (StrifeAttribute attribute : plugin.getAttributeManager().getAttributes()) {
      champion.setLevel(attribute, 0);
    }
    champion.setHighestReachedLevel(target.getLevel());
    champion.setUnusedStatPoints(target.getLevel());
    sendMessage(sender, "<green>You reset <white>%player%<green>.",
        new String[][]{{"%player%", target.getDisplayName()}});
    sendMessage(target, "<green>Your stats have been reset.");
    sendMessage(target, "&6You have unspent levelpoints! Use &f/levelup &6to spend them!");
    plugin.getChampionManager().updateAll(champion);
    plugin.getStatUpdateManager().updateAttributes(champion.getPlayer());
  }

  @Command(identifier = "strife clear", permissions = "strife.command.strife.clear", onlyPlayers = false)
  public void clearCommand(CommandSender sender, @Arg(name = "target") Player target) {
    target.setExp(0f);
    target.setLevel(0);
    Champion champion = plugin.getChampionManager().getChampion(target);
    for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
      champion.setLevel(stat, 0);
    }
    champion.setUnusedStatPoints(0);
    champion.setHighestReachedLevel(0);
    sendMessage(sender, "<green>You cleared <white>%player%<green>.",
        new String[][]{{"%player%", target.getDisplayName()}});
    sendMessage(target, "<green>Your stats have been cleared.");
    plugin.getChampionManager().updateAll(champion);
    plugin.getStatUpdateManager().updateAttributes(champion.getPlayer());
  }

  @Command(identifier = "strife raise", permissions = "strife.command.strife.raise", onlyPlayers = false)
  public void raiseCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "level") int newLevel) {
    int oldLevel = target.getLevel();
    if (newLevel <= oldLevel) {
      sendMessage(sender, "<red>New level must be higher than old level.");
    }
    target.setExp(0f);
    target.setLevel(newLevel);
    Champion champion = plugin.getChampionManager().getChampion(target);
    plugin.getChampionManager().updateAll(champion);
    sendMessage(sender, "<green>You raised <white>%player%<green> to level <white>%level%<green>.",
        new String[][]{{"%player%", target.getDisplayName()}, {"%level%", "" + newLevel}});
    sendMessage(target, "<green>Your level has been raised.");
    plugin.getStatUpdateManager().updateAttributes(champion.getPlayer());
  }

  @Command(identifier = "strife ability set", permissions = "strife.command.strife.binding", onlyPlayers = false)
  public void setAbilityCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "ability") String id) {
    Ability ability = plugin.getAbilityManager().getAbility(id.replace("_", " "));
    if (ability == null) {
      sendMessage(sender, "<red>Invalid ability ID: " + id);
      return;
    }
    if (ability.getAbilityIconData() == null) {
      sendMessage(sender, "<red>Invalid ability - No ability icon data");
      return;
    }
    if (ability.getAbilityIconData().getAbilitySlot() == null) {
      sendMessage(sender, "<red>Invalid ability - No ability slot set");
      return;
    }
    if (ability.getAbilityIconData().getStack() == null) {
      sendMessage(sender, "<red>Cannot use this command for an ability without an icon!");
      return;
    }
    plugin.getChampionManager().getChampion(target).getSaveData()
        .setAbility(ability.getAbilityIconData().getAbilitySlot(), ability);
    plugin.getAbilityIconManager().setAllAbilityIcons(target);
  }

  @Command(identifier = "strife ability remove", permissions = "strife.command.strife.binding", onlyPlayers = false)
  public void removeAbilityCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "slot") int slot) {
    AbilitySlot abilitySlot = AbilitySlot.fromSlot(slot);
    if (abilitySlot == AbilitySlot.INVALID) {
      sendMessage(sender, "<red>Invalid slot: " + slot);
      return;
    }
    plugin.getChampionManager().getChampion(target).getSaveData().setAbility(abilitySlot, null);
    plugin.getAbilityIconManager().setAllAbilityIcons(target);
  }

  @Command(identifier = "strife ability menu", permissions = "strife.command.strife.binding", onlyPlayers = false)
  public void menuAbilityCommand(CommandSender sender, @Arg(name = "target") Player target) {
    plugin.getAbilityPicker().open(target);
  }

  @Command(identifier = "strife ability submenu", permissions = "strife.command.strife.binding", onlyPlayers = false)
  public void submenuAbilityCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "menu") String menu) {
    plugin.getSubmenu(menu).open(target);
  }

  @Command(identifier = "strife bind", permissions = "strife.command.strife.binding", onlyPlayers = false)
  public void bindCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "loreAbility") String id) {
    LoreAbility ability = plugin.getLoreAbilityManager().getLoreAbilityFromId(id);
    if (ability == null) {
      sendMessage(sender, "<red>Invalid loreAbility ID: " + id);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(target);
    boolean success = plugin.getChampionManager().addBoundLoreAbility(champion, ability);
    if (success) {
      sendMessage(sender, "&aBound loreAbility " + id + " to player " + target.getName());
    } else {
      sendMessage(sender, "&cLoreAbility " + id + " already exists on " + target.getName());
    }
  }

  @Command(identifier = "strife unbind", permissions = "strife.command.strife.binding", onlyPlayers = false)
  public void unbindCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "loreAbility") String abilityId) {
    LoreAbility ability = plugin.getLoreAbilityManager().getLoreAbilityFromId(abilityId);
    if (ability == null) {
      sendMessage(sender, "<red>Invalid loreAbility ID: " + abilityId);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(target);
    boolean success = plugin.getChampionManager().removeBoundLoreAbility(champion, ability);
    if (success) {
      sendMessage(sender, "&aUnbound loreAbility " + abilityId + " to player " + target.getName());
    } else {
      sendMessage(sender, "&cLoreAbility " + abilityId + " doesn't exist on " + target.getName());
    }
  }

  @Command(identifier = "strife setskill", permissions = "strife.command.strife.setskill", onlyPlayers = false)
  public void skillCommand(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "skill") String skill, @Arg(name = "level") int newLevel) {
    if (!target.hasPermission("strife.weirdlevels") && (newLevel > 60 || newLevel < 0)) {
      sendMessage(sender, "<red>Skill must be between level 0 and 60.");
      return;
    }
    LifeSkillType type;
    try {
      type = LifeSkillType.valueOf(skill.toUpperCase());
    } catch (Exception e) {
      sendMessage(sender, "<red>Unknown skill " + skill + "??");
      return;
    }
    ChatColor color = type.getColor();
    String name = type.getName();

    plugin.getChampionManager().getChampion(target).getSaveData().setSkillLevel(type, newLevel);
    sendMessage(target, SET_LEVEL_MSG
        .replace("{c}", "" + color)
        .replace("{n}", name)
        .replace("{a}", Integer.toString(newLevel))
    );
    sendMessage(sender, "Set " + name + " level of " + target.getName() + " to " + newLevel);
  }

  @Command(identifier = "strife addskillxp", permissions = "strife.command.strife.setskill", onlyPlayers = false)
  @Flags(identifier = {"s", "d"},
      description = {"scale experience with stats", "silent xp display"})
  public void addSkillXp(CommandSender sender, @Arg(name = "target") Player target,
      @Arg(name = "skill") String skill, @Arg(name = "xpAmount") int amount,
      @FlagArg("s") boolean scaleXp,
      @FlagArg("d") boolean silentDisplay) {
    String skillName = skill.toUpperCase();
    LifeSkillType type;
    try {
      type = LifeSkillType.valueOf(skillName.toUpperCase());
    } catch (Exception e) {
      sendMessage(sender, "<red>Unknown skill " + skill + "???");
      return;
    }
    plugin.getSkillExperienceManager().addExperience(target, type, amount, !scaleXp, !silentDisplay);
  }

  @Command(identifier = "strife addxp", permissions = "strife.command.strife.addxp", onlyPlayers = false)
  public void addXpCommand(CommandSender sender, @Arg(name = "target") Player player,
      @Arg(name = "amount") double amount) {
    plugin.getExperienceManager().addExperience(player, amount, true);
    sendMessage(player, "&aYou gained &f" + (int) amount + " &aXP!");
  }

  @Command(identifier = "strife startBoost", permissions = "strife.command.strife.boosts", onlyPlayers = false)
  public void startBoostCommand(CommandSender sender, @Arg(name = "boostId") String boostId,
      @Arg(name = "creator") String creator, @Arg(name = "duration") int duration) {
    boolean success = plugin.getBoostManager().startBoost(creator, boostId, duration);
    if (!success) {
      sendMessage(sender, "&cBoost with that ID doesn't exist, or this boost is running");
    }
  }

  @Command(identifier = "strife togglexp")
  public void toggleExp(CommandSender sender) {
    Champion champion = plugin.getChampionManager().getChampion((Player) sender);
    champion.getSaveData().setDisplayExp(!champion.getSaveData().isDisplayExp());
    sendMessage(sender, "&aDisplay XP: &f" + champion.getSaveData().isDisplayExp());
  }

  @Command(identifier = "strife reveal", permissions = "strife.command.strife.reveal", onlyPlayers = false)
  public void reveal(CommandSender sender, @Arg(name = "target") Player target) {
    if (target.getEquipment() == null) {
      sendMessage(sender, "&cTarget's equipment is null, somehow.");
    }
    ItemStack item = target.getEquipment().getItemInMainHand();
    if (item.getItemMeta() == null) {
      sendMessage(target, REVEAL_FAIL);
      return;
    }
    if (item.getItemMeta().getLore() == null || item.getItemMeta().getLore().size() == 0) {
      sendMessage(target, REVEAL_FAIL);
      return;
    }
    List<String> lore = item.getItemMeta().getLore();
    for (int i = 0; i < lore.size(); i++) {
      String loreString = lore.get(i);
      if (loreString.contains(REVEAL_PREFIX)) {
        lore.set(i, lore.get(i).replace(REVEAL_PREFIX, REVEAL_REPLACEMENT));
        ItemStackExtensionsKt.setLore(item, lore);
        target.updateInventory();
        sendMessage(target, REVEAL_SUCCESS);
        return;
      }
    }
    sendMessage(target, REVEAL_FAIL);
  }
}
