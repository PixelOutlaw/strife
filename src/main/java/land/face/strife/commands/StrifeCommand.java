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
package land.face.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendActionBar;
import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
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

@CommandAlias("strife")
public class StrifeCommand extends BaseCommand {

  private final StrifePlugin plugin;
  private final String REVEAL_SUCCESS;
  private final String REVEAL_FAIL;
  private final String REVEAL_PREFIX;
  private final String REVEAL_REPLACEMENT;
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
    SET_LEVEL_MSG = plugin.getSettings()
        .getString("language.command.set-level-msg", "{c}Your level in &f{n} {c}is now &f{a}{c}!");
    PVP_WIN_MSG = plugin.getSettings()
        .getString("language.pvp.gain-score-action-bar", "&aGained PvP Rating! &f{0} (+{1})");
    PVP_LOSE_MSG = plugin.getSettings()
        .getString("language.pvp.lose-score-action-bar", "&eLost PvP Rating... &f{0} ({1})");
  }

  @Subcommand("unique|spawn|summon")
  @CommandCompletion("@uniques")
  @CommandPermission("strife.admin")
  public void uniqueSummonCommand(String entityId) {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    Player sender = getCurrentCommandIssuer().getIssuer();
    plugin.getUniqueEntityManager().spawnUnique(entityId, sender.getLocation());
  }

  @Subcommand("defeat")
  @CommandPermission("strife.admin")
  public void defeatCommand(Player winner, Player loser, @Default("10") double weight) {
    ChampionSaveData winData = plugin.getChampionManager().getChampion(winner).getSaveData();
    ChampionSaveData loseData = plugin.getChampionManager().getChampion(loser).getSaveData();

    EloResponse response = EloUtil
        .getEloChange(winData.getPvpScore(), loseData.getPvpScore(), (float) weight);

    float winDiff = response.getNewWinnerValue() - winData.getPvpScore();
    float loseDiff = response.getNewLoserValue() - loseData.getPvpScore();

    winData.setPvpScore(response.getNewWinnerValue());
    loseData.setPvpScore(response.getNewLoserValue());

    sendActionBar(winner, PVP_WIN_MSG.replace("{0}",
        String.valueOf(Math.round(response.getNewWinnerValue()))).replace("{1}", String.valueOf(Math.round(winDiff))));
    sendActionBar(loser, PVP_LOSE_MSG.replace("{0}",
        String.valueOf(Math.round(response.getNewLoserValue()))).replace("{1}", String.valueOf(Math.round(loseDiff))));
  }

  @Subcommand("cd|cooldown")
  public void cooldownCommand(Player target) {
    target.resetCooldown();
  }

  @Subcommand("reload")
  @CommandPermission("strife.admin")
  public void reloadCommand() {
    // Save player data before reload continues
    plugin.getStorage().saveAll();

    // Normal enable/disable
    plugin.disable();
    plugin.enable();

    for (Player player : Bukkit.getOnlinePlayers()) {
      plugin.getStatUpdateManager().updateVanillaAttributes(player);
    }

    sendMessage(getCurrentCommandIssuer().getIssuer(),
        plugin.getSettings().getString("language.command.reload", "&aStrife reloaded!"));
  }

  @Subcommand("profile")
  @CommandPermission("strife.admin")
  public void profileCommand(Player target) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    Champion champion = plugin.getChampionManager().getChampion(target);
    sendMessage(sender, "&6----------------------------------");
    sendMessage(sender, "&7Unused Stat Points: &f%amount%",
        new String[][]{{"%amount%", "" + champion.getUnusedStatPoints()}});
    sendMessage(sender, "&6----------------------------------");
    for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
      sendMessage(sender, ChatColor.GRAY + stat.getKey() + " - " + champion.getAttributeLevel(stat));
    }
    sendMessage(sender, "&6----------------------------------");
  }

  @Subcommand("mobinfo|info")
  @CommandPermission("strife.info")
  public void infoCommand() {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    List<LivingEntity> targets = new ArrayList<>(
        TargetingUtil.getEntitiesInLine(((Player) sender).getEyeLocation(), 30));
    targets.remove(sender);
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

  @Subcommand("reset")
  @CommandPermission("strife.admin")
  public void resetCommand(Player target) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    Champion champion = plugin.getChampionManager().getChampion(target);
    for (StrifeAttribute attribute : plugin.getAttributeManager().getAttributes()) {
      champion.setLevel(attribute, 0);
    }
    champion.setHighestReachedLevel(target.getLevel());
    champion.setUnusedStatPoints(target.getLevel());
    champion.getSaveData().getPathMap().clear();
    plugin.getPathManager().buildPathBonus(champion);
    sendMessage(sender, "You reset %player%", new String[][]{{"%player%", target.getDisplayName()}});
    sendMessage(target, "&aYour stats have been reset!");
    sendMessage(target, "&6You have unspent levelpoints! Use &f/levelup &6to spend them!");
    plugin.getChampionManager().update(target);
    plugin.getStatUpdateManager().updateVanillaAttributes(champion.getPlayer());
  }

  @Subcommand("clear|wipe")
  @CommandPermission("strife.admin")
  public void clearCommand(Player target) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    target.setExp(0f);
    target.setLevel(0);
    Champion champion = plugin.getChampionManager().getChampion(target);
    for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
      champion.setLevel(stat, 0);
    }
    champion.setUnusedStatPoints(0);
    champion.setHighestReachedLevel(0);
    sendMessage(sender, "You cleared <white>%player%", new String[][]{{"%player%", target.getDisplayName()}});
    sendMessage(target, "&aYour stats have been wiped :O");
    plugin.getChampionManager().update(target);
    plugin.getStatUpdateManager().updateVanillaAttributes(champion.getPlayer());
  }

  @Subcommand("raise|levelup")
  @CommandPermission("strife.admin")
  public void raiseCommand(Player target, @Default("1") int newLevel) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    int oldLevel = target.getLevel();
    if (newLevel <= oldLevel) {
      sendMessage(sender, "&cNew level must be higher than old level.");
      return;
    }
    target.setExp(0f);
    target.setLevel(newLevel);
    Champion champion = plugin.getChampionManager().getChampion(target);
    plugin.getChampionManager().update(target);
    sendMessage(sender, "&aYou raised &f%player% &ato level &f%level%.",
        new String[][]{{"%player%", target.getDisplayName()}, {"%level%", "" + newLevel}});
    sendMessage(target, "&aAn administrator has raised your level");
    plugin.getStatUpdateManager().updateVanillaAttributes(champion.getPlayer());
  }

  @Subcommand("ability set")
  @CommandPermission("strife.admin")
  public void setAbilityCommand(Player target, String abilityId) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    Ability ability = plugin.getAbilityManager().getAbility(abilityId.replace("_", " "));
    if (ability == null) {
      sendMessage(sender, "<red>Invalid ability ID: " + abilityId);
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

  @Subcommand("ability remove")
  @CommandPermission("strife.admin")
  public void removeAbilityCommand(Player target, int slot) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    AbilitySlot abilitySlot = AbilitySlot.fromSlot(slot);
    if (abilitySlot == AbilitySlot.INVALID) {
      sendMessage(sender, "<red>Invalid slot: " + slot);
      return;
    }
    plugin.getChampionManager().getChampion(target).getSaveData().setAbility(abilitySlot, null);
    plugin.getAbilityIconManager().setAllAbilityIcons(target);
  }

  @Subcommand("ability menu")
  @CommandPermission("strife.admin")
  public void menuAbilityCommand(Player target) {
    plugin.getAbilityPicker().open(target);
  }

  @Subcommand("ability submenu")
  @CommandPermission("strife.admin")
  public void submenuAbilityCommand(Player target, String menu) {
    plugin.getSubmenu(menu).open(target);
  }

  @Subcommand("bind")
  @CommandCompletion("@loreabilities")
  @CommandPermission("strife.admin")
  public void bindCommand(Player target, String loreAbilityId) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    LoreAbility ability = plugin.getLoreAbilityManager().getLoreAbilityFromId(loreAbilityId);
    if (ability == null) {
      sendMessage(sender, "<red>Invalid loreAbility ID: " + loreAbilityId);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(target);
    boolean success = plugin.getChampionManager().addBoundLoreAbility(champion, ability);
    if (success) {
      sendMessage(sender, "&aBound loreAbility " + loreAbilityId + " to player " + target.getName());
    } else {
      sendMessage(sender, "&cLoreAbility " + loreAbilityId + " already exists on " + target.getName());
    }
  }

  @Subcommand("unbind")
  @CommandCompletion("@loreabilities")
  @CommandPermission("strife.admin")
  public void unbindCommand(Player target, String loreAbilityId) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    LoreAbility ability = plugin.getLoreAbilityManager().getLoreAbilityFromId(loreAbilityId);
    if (ability == null) {
      sendMessage(sender, "<red>Invalid loreAbility ID: " + loreAbilityId);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(target);
    boolean success = plugin.getChampionManager().removeBoundLoreAbility(champion, ability);
    if (success) {
      sendMessage(sender, "&aUnbound loreAbility " + loreAbilityId + " to player " + target.getName());
    } else {
      sendMessage(sender, "&cLoreAbility " + loreAbilityId + " doesn't exist on " + target.getName());
    }
  }

  @Subcommand("setskill")
  @CommandCompletion("@skills @range:1-99")
  @CommandPermission("strife.admin")
  public void skillCommand(Player target, String skill, int newLevel) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    if (newLevel > 99 || newLevel < 1) {
      sendMessage(sender, "&cSkill must be between level 1 and 99.");
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

  @Subcommand("addskillxp|skillxp")
  @CommandCompletion("@players @skills @range:1-100000 @nothing @nothing")
  @CommandPermission("strife.admin")
  public void addSkillXp(Player target, String skill, int amount, boolean exact, boolean silent) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    String skillName = skill.toUpperCase();
    LifeSkillType type;
    try {
      type = LifeSkillType.valueOf(skillName.toUpperCase());
    } catch (Exception e) {
      sendMessage(sender, "&cUnknown skill " + skill + "???");
      return;
    }
    plugin.getSkillExperienceManager().addExperience(target, type, amount, exact, !silent);
  }

  @Subcommand("addxp")
  @CommandPermission("strife.admin")
  public void addXpCommand(Player player, double amount) {
    plugin.getExperienceManager().addExperience(player, amount, true);
    sendMessage(player, "&aYou gained &f" + (int) amount + " &aXP!");
  }

  @Subcommand("addxp")
  @CommandCompletion("@boosts @players @range:1-1000000")
  @CommandPermission("strife.admin")
  public void startBoostCommand(String boostId, String creator, int duration) {
    boolean success = plugin.getBoostManager().startBoost(creator, boostId, duration);
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    if (!success) {
      sendMessage(sender, "&cBoost with that ID doesn't exist, or this boost is running");
    }
  }

  @Subcommand("reveal")
  @CommandPermission("strife.admin")
  public void reveal(Player target) {
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
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

  @Subcommand("togglexp")
  public void toggleExp() {
    if (!getCurrentCommandIssuer().isPlayer()) {
      return;
    }
    CommandSender sender = getCurrentCommandIssuer().getIssuer();
    Champion champion = plugin.getChampionManager().getChampion((Player) sender);
    champion.getSaveData().setDisplayExp(!champion.getSaveData().isDisplayExp());
    sendMessage(sender, "&aDisplay XP: &f" + champion.getSaveData().isDisplayExp());
  }
}
