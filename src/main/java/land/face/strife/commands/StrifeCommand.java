/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendActionBar;
import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
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
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.menus.abilities.ReturnButton;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.EloUtil;
import land.face.strife.util.TargetingUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
  public void uniqueSummonCommand(Player sender, String entityId) {
    plugin.getUniqueEntityManager().spawnUnique(entityId, sender.getLocation());
  }

  @Subcommand("defeat")
  @CommandCompletion("@players @players @range:1-30")
  @CommandPermission("strife.admin")
  public void defeatCommand(CommandSender sender, OnlinePlayer winner, OnlinePlayer loser,
      @Default("10") double weight) {
    ChampionSaveData winData = plugin.getChampionManager().getChampion(winner.getPlayer())
        .getSaveData();
    ChampionSaveData loseData = plugin.getChampionManager().getChampion(loser.getPlayer())
        .getSaveData();

    EloResponse response = EloUtil
        .getEloChange(winData.getPvpScore(), loseData.getPvpScore(), (float) weight);

    float winDiff = response.getNewWinnerValue() - winData.getPvpScore();
    float loseDiff = response.getNewLoserValue() - loseData.getPvpScore();

    winData.setPvpScore(response.getNewWinnerValue());
    loseData.setPvpScore(response.getNewLoserValue());

    sendActionBar(winner.getPlayer(), PVP_WIN_MSG.replace("{0}",
        String.valueOf(Math.round(response.getNewWinnerValue())))
        .replace("{1}", String.valueOf(Math.round(winDiff))));
    sendActionBar(loser.getPlayer(), PVP_LOSE_MSG.replace("{0}",
        String.valueOf(Math.round(response.getNewLoserValue())))
        .replace("{1}", String.valueOf(Math.round(loseDiff))));
  }

  @Subcommand("cd|cooldown")
  @CommandCompletion("@players")
  public void cooldownCommand(CommandSender sender, OnlinePlayer target) {
    target.getPlayer().resetCooldown();
  }

  @Subcommand("swing")
  @CommandCompletion("@players")
  public void swinfCommand(CommandSender sender, OnlinePlayer target) {
    target.getPlayer().swingMainHand();
  }

  @Subcommand("reload")
  @CommandPermission("strife.admin")
  public void reloadCommand(CommandSender sender) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      MessageUtils.sendMessage(p,
          "&a&o&lATTENTION GAMER: &a&oThe RPG plugin is being reloaded, maybe to add things, maybe because a GM is being a dingus. Please wait...");
    }

    // Save player data before reload continues
    plugin.getStorage().saveAll();
    // Normal enable/disable
    plugin.disable();
    plugin.enable();

    for (Player player : Bukkit.getOnlinePlayers()) {
      plugin.getStatUpdateManager().updateVanillaAttributes(player);
    }
    for (Player p : Bukkit.getOnlinePlayers()) {
      MessageUtils.sendMessage(p,
          "&a&o&lATTENTION GAMER: &a&oOkay we're back now thanks for waiting :)");
    }
    sendMessage(sender,
        plugin.getSettings().getString("language.command.reload", "&aStrife reloaded!"));
  }

  @Subcommand("profile")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void profileCommand(CommandSender sender, OnlinePlayer target) {
    Champion champion = plugin.getChampionManager().getChampion(target.getPlayer());
    sendMessage(sender, "&6----------------------------------");
    sendMessage(sender, "&7Unused Stat Points: &f%amount%",
        new String[][]{{"%amount%", "" + champion.getUnusedStatPoints()}});
    sendMessage(sender, "&6----------------------------------");
    for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
      sendMessage(sender,
          ChatColor.GRAY + stat.getKey() + " - " + champion.getAttributeLevel(stat));
    }
    sendMessage(sender, "&6----------------------------------");
  }

  @Subcommand("mobinfo|info")
  @CommandPermission("strife.info")
  public void infoCommand(Player sender) {
    List<LivingEntity> targets = new ArrayList<>(
        TargetingUtil.getEntitiesInLine(sender.getEyeLocation(), 30, 2));
    targets.remove(sender);
    if (targets.isEmpty()) {
      sendMessage(sender, "&eNo target found...");
      return;
    }
    TargetingUtil.DISTANCE_COMPARATOR.setLoc((sender).getLocation());
    targets.sort(TargetingUtil.DISTANCE_COMPARATOR);
    StrifeMob targetMob = plugin.getStrifeMobManager().getStatMob(targets.get(0));
    sendMessage(sender, "&aUniqueID: " + targetMob.getUniqueEntityId());
    sendMessage(sender, "&aGroups: " + Arrays.toString(targetMob.getFactions().toArray()));
  }

  @Subcommand("reset")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void resetCommand(CommandSender sender, OnlinePlayer target) {
    Champion champion = plugin.getChampionManager().getChampion(target.getPlayer());
    for (StrifeAttribute attribute : plugin.getAttributeManager().getAttributes()) {
      champion.setLevel(attribute, 0);
    }
    champion.setHighestReachedLevel(target.getPlayer().getLevel());
    champion.setUnusedStatPoints(target.getPlayer().getLevel());
    champion.getSaveData().getPathMap().clear();
    plugin.getPathManager().buildPathBonus(champion);
    sendMessage(sender, "You reset %player%",
        new String[][]{{"%player%", target.getPlayer().getDisplayName()}});
    sendMessage(target.getPlayer(), "&aYour stats have been reset!");
    sendMessage(target.getPlayer(),
        "&6You have unspent levelpoints! Use &f/levelup &6to spend them!");
    plugin.getChampionManager().update(target.getPlayer());
    plugin.getStatUpdateManager().updateVanillaAttributes(champion.getPlayer());
  }

  @Subcommand("clear|wipe")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void clearCommand(CommandSender sender, OnlinePlayer target) {
    target.getPlayer().setExp(0f);
    target.getPlayer().setLevel(0);
    Champion champion = plugin.getChampionManager().getChampion(target.getPlayer());
    for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
      champion.setLevel(stat, 0);
    }
    champion.setUnusedStatPoints(0);
    champion.setHighestReachedLevel(0);
    sendMessage(sender, "You cleared <white>%player%",
        new String[][]{{"%player%", target.getPlayer().getDisplayName()}});
    sendMessage(target.getPlayer(), "&aYour stats have been wiped :O");
    plugin.getChampionManager().update(target.getPlayer());
    plugin.getStatUpdateManager().updateVanillaAttributes(champion.getPlayer());
  }

  @Subcommand("raise|levelup")
  @CommandCompletion("@players @range:1-100")
  @CommandPermission("strife.admin")
  public void raiseCommand(CommandSender sender, OnlinePlayer target, @Default("1") int newLevel) {
    int oldLevel = target.getPlayer().getLevel();
    if (newLevel <= oldLevel) {
      sendMessage(sender, "&cNew level must be higher than old level.");
      return;
    }
    target.getPlayer().setExp(0f);
    target.getPlayer().setLevel(newLevel);
    Champion champion = plugin.getChampionManager().getChampion(target.getPlayer());
    plugin.getChampionManager().update(target.getPlayer());
    sendMessage(sender, "&aYou raised &f%player% &ato level &f%level%.",
        new String[][]{{"%player%", target.getPlayer().getDisplayName()},
            {"%level%", "" + newLevel}});
    sendMessage(target.getPlayer(), "&aAn administrator has raised your level");
    plugin.getStatUpdateManager().updateVanillaAttributes(champion.getPlayer());
  }

  @Subcommand("ability set")
  @CommandCompletion("@players @abilities")
  @CommandPermission("strife.admin")
  public void setAbilityCommand(CommandSender sender, OnlinePlayer target, String abilityId) {
    Ability ability = plugin.getAbilityManager().getAbility(abilityId.replace("_", " "));
    if (ability == null) {
      sendMessage(sender, "&cInvalid ability ID: " + abilityId);
      return;
    }
    if (ability.getAbilityIconData() == null) {
      sendMessage(sender, "&cInvalid ability - This ability is not hotbar compatible");
      return;
    }
    if (ability.getAbilityIconData().getAbilitySlot() == null) {
      sendMessage(sender, "&cInvalid ability - No ability slot set");
      return;
    }
    if (ability.getAbilityIconData().getStack() == null) {
      sendMessage(sender, "&cCannot use this command for an ability without an icon!");
      return;
    }
    plugin.getChampionManager().getChampion(target.getPlayer()).getSaveData().setAbility(
        ability.getAbilityIconData().getAbilitySlot(), ability);
    plugin.getAbilityIconManager().setAllAbilityIcons(target.getPlayer());
  }

  @Subcommand("ability remove")
  @CommandCompletion("@players @range:0-2")
  @CommandPermission("strife.admin")
  public void removeAbilityCommand(CommandSender sender, OnlinePlayer target, int slot) {
    AbilitySlot abilitySlot = AbilitySlot.fromSlot(slot);
    if (abilitySlot == AbilitySlot.INVALID) {
      sendMessage(sender, "<red>Invalid slot: " + slot);
      return;
    }
    plugin.getChampionManager().getChampion(target.getPlayer()).getSaveData()
        .setAbility(abilitySlot, null);
    plugin.getAbilityIconManager().setAllAbilityIcons(target.getPlayer());
  }

  @Subcommand("ability menu")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void menuAbilityCommand(CommandSender sender, OnlinePlayer target) {
    ReturnButton.setBackButtonEnabled(target.getPlayer(), true);
    plugin.getAbilityPicker().open(target.getPlayer());
  }

  @Subcommand("ability submenu")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void submenuAbilityCommand(CommandSender sender, OnlinePlayer target, String menu) {
    ReturnButton.setBackButtonEnabled(target.getPlayer(), false);
    plugin.getSubmenu(menu).open(target.getPlayer());
  }

  @Subcommand("ability refresh")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void submenuAbilityCommand(CommandSender sender, OnlinePlayer target) {
    plugin.getAbilityIconManager().setAllAbilityIcons(target.getPlayer());
  }

  @Subcommand("bind")
  @CommandCompletion("@players @loreabilities")
  @CommandPermission("strife.admin")
  public void bindCommand(CommandSender sender, OnlinePlayer target, String loreAbilityId) {
    LoreAbility ability = plugin.getLoreAbilityManager().getLoreAbilityFromId(loreAbilityId);
    if (ability == null) {
      sendMessage(sender, "<red>Invalid loreAbility ID: " + loreAbilityId);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(target.getPlayer());
    boolean success = plugin.getChampionManager().addBoundLoreAbility(champion, ability);
    if (success) {
      sendMessage(sender,
          "&aBound loreAbility " + loreAbilityId + " to player " + target.getPlayer().getName());
    } else {
      sendMessage(sender,
          "&cLoreAbility " + loreAbilityId + " already exists on " + target.getPlayer().getName());
    }
  }

  @Subcommand("unbind")
  @CommandCompletion("@players @loreabilities")
  @CommandPermission("strife.admin")
  public void unbindCommand(CommandSender sender, OnlinePlayer target, String loreAbilityId) {
    LoreAbility ability = plugin.getLoreAbilityManager().getLoreAbilityFromId(loreAbilityId);
    if (ability == null) {
      sendMessage(sender, "<red>Invalid loreAbility ID: " + loreAbilityId);
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(target.getPlayer());
    boolean success = plugin.getChampionManager().removeBoundLoreAbility(champion, ability);
    if (success) {
      sendMessage(sender,
          "&aUnbound loreAbility " + loreAbilityId + " to player " + target.getPlayer().getName());
    } else {
      sendMessage(sender,
          "&cLoreAbility " + loreAbilityId + " doesn't exist on " + target.getPlayer().getName());
    }
  }

  @Subcommand("setskill")
  @CommandCompletion("@players @skills @range:1-99")
  @CommandPermission("strife.admin")
  public void skillCommand(CommandSender sender, OnlinePlayer target, String skill, int newLevel) {
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

    plugin.getChampionManager().getChampion(target.getPlayer()).getSaveData()
        .setSkillLevel(type, newLevel);
    sendMessage(target.getPlayer(), SET_LEVEL_MSG
        .replace("{c}", "" + color)
        .replace("{n}", name)
        .replace("{a}", Integer.toString(newLevel))
    );
    sendMessage(sender,
        "Set " + name + " level of " + target.getPlayer().getName() + " to " + newLevel);
  }

  @Subcommand("addskillxp|skillxp")
  @CommandCompletion("@players @skills @range:1-100000 true|false true|false")
  @CommandPermission("strife.admin")
  public void addSkillXp(CommandSender sender, OnlinePlayer target, String skill, int amount,
      @Default("true") boolean exact, @Default("false") boolean silent) {
    String skillName = skill.toUpperCase();
    LifeSkillType type;
    try {
      type = LifeSkillType.valueOf(skillName.toUpperCase());
    } catch (Exception e) {
      sendMessage(sender, "&cUnknown skill " + skill + "???");
      return;
    }
    plugin.getSkillExperienceManager()
        .addExperience(target.getPlayer(), type, amount, exact, !silent);
  }

  @Subcommand("addxp")
  @CommandCompletion("@players @range:1-10")
  @CommandPermission("strife.admin")
  public void addXpCommand(CommandSender sender, OnlinePlayer player, double amount) {
    plugin.getExperienceManager().addExperience(player.getPlayer(), amount, true);
    sendMessage(player.player, "&aYou gained &f" + (int) amount + " &aXP!");
    sendMessage(sender,
        "&a&oAwarded " + amount + " xp to " + player.getPlayer().getName() + " via command");
  }

  @Subcommand("boost")
  @CommandCompletion("@boosts @players @range:1-10")
  @CommandPermission("strife.admin")
  public void startBoostCommand(CommandSender sender, String boostId, String creator,
      int duration) {
    boolean success = plugin.getBoostManager().startBoost(creator, boostId, duration);
    if (!success) {
      sendMessage(sender, "&cBoost with that ID doesn't exist, or this boost is running");
    }
  }

  @Subcommand("applybuff")
  @CommandCompletion("@players @buffs @range:1-10")
  @CommandPermission("strife.admin")
  public void applyBuff(CommandSender sender, OnlinePlayer player, String buffId, int seconds) {
    LoadedBuff buff = plugin.getBuffManager().getBuffFromId(buffId);
    if (buff == null) {
      sendMessage(sender, "&cBuff with that ID doesn't exist: " + buffId);
      return;
    }
    plugin.getStrifeMobManager().getStatMob(player.getPlayer())
        .addBuff(LoadedBuff.toRunningBuff(buff), seconds);
  }

  @Subcommand("reveal")
  @CommandCompletion("@players")
  @CommandPermission("strife.admin")
  public void reveal(CommandSender sender, OnlinePlayer target) {
    if (target.getPlayer().getEquipment() == null) {
      sendMessage(sender, "&cTarget's equipment is null, somehow.");
    }
    ItemStack item = target.getPlayer().getEquipment().getItemInMainHand();
    if (item.getItemMeta() == null) {
      sendMessage(target.getPlayer(), REVEAL_FAIL);
      return;
    }
    if (item.getItemMeta().getLore() == null || item.getItemMeta().getLore().size() == 0) {
      sendMessage(target.getPlayer(), REVEAL_FAIL);
      return;
    }
    List<String> lore = item.getItemMeta().getLore();
    for (int i = 0; i < lore.size(); i++) {
      String loreString = lore.get(i);
      if (loreString.contains(REVEAL_PREFIX)) {
        lore.set(i, lore.get(i).replace(REVEAL_PREFIX, REVEAL_REPLACEMENT));
        ItemStackExtensionsKt.setLore(item, lore);
        target.getPlayer().updateInventory();
        sendMessage(target.getPlayer(), REVEAL_SUCCESS);
        return;
      }
    }
    sendMessage(target.getPlayer(), REVEAL_FAIL);
  }

  @Subcommand("togglexp")
  public void toggleExp(Player player) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    champion.getSaveData().setDisplayExp(!champion.getSaveData().isDisplayExp());
    sendMessage(player, "&aDisplay XP: &f" + champion.getSaveData().isDisplayExp());
  }
}
