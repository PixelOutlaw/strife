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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.ShaderStyle;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.ChampionSaveData.SelectedGod;
import land.face.strife.managers.PrayerManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

@CommandAlias("gods")
public class GodCommand extends BaseCommand {

  private final StrifePlugin plugin;

  public GodCommand(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("set")
  @CommandPermission("strife.admin")
  public void setGodCommand(CommandSender sender, OnlinePlayer target, String god) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(target.getPlayer());
    SelectedGod selectedGod = SelectedGod.valueOf(god.toUpperCase());

    plugin.getChampionManager().removeBoundLoreAbility(mob,
        plugin.getLoreAbilityManager().getLoreAbilityFromId("FACEGUY-1"));
    plugin.getChampionManager().removeBoundLoreAbility(mob,
        plugin.getLoreAbilityManager().getLoreAbilityFromId("AURORA-1"));
    plugin.getChampionManager().removeBoundLoreAbility(mob,
        plugin.getLoreAbilityManager().getLoreAbilityFromId("ZEXIR-1"));
    plugin.getChampionManager().removeBoundLoreAbility(mob,
        plugin.getLoreAbilityManager().getLoreAbilityFromId("ANYA-1"));
    plugin.getChampionManager().addBoundLoreAbility(mob, selectedGod.name() + "-1");
    StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS);
    if (target.getPlayer().hasPermission("prayer.enabled") &&
        mob.getPrayer() != StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS)) {
      PaletteUtil.sendMessage(target.getPlayer(), FaceColor.WHITE.s() + FaceColor.ITALIC + "Faith restored!");
      Audience audience = Audience.audience(target.getPlayer());
      audience.playSound(PrayerManager.FAITH_RESTORED);
    }
    mob.setPrayer(mob.getMaxPrayer());
    plugin.getPrayerManager().sendPrayerUpdate(target.getPlayer(), 1, false);

    if (mob.getChampion().getSaveData().getSelectedGod() != selectedGod) {
      //PaletteUtil.sendMessage(target.getPlayer(), "God Updated!");
    } else {
      //PaletteUtil.sendMessage(target.getPlayer(), "God Updated?");
    }

    mob.getChampion().getSaveData().setSelectedGod(selectedGod);
    String subtitle = switch (selectedGod) {
      case FACEGUY -> FaceColor.RED.shaded(ShaderStyle.SHAKE) + "CHAOS EMBRACED!";
      case AURORA -> FaceColor.LIGHT_GREEN.shaded(ShaderStyle.SHAKE) + "LIGHT EMBRACED!";
      case ZEXIR -> FaceColor.BLACK.shaded(ShaderStyle.SHAKE) + "DARKNESS EMBRACED!";
      case ANYA -> FaceColor.BLUE.shaded(ShaderStyle.SHAKE) + "ORDER EMBRACED";
      default -> "BUG??";
    };

    TitleUtils.sendTitle(
        target.getPlayer(),
        FaceColor.YELLOW.s() + FaceColor.BOLD + "GOD CHOSEN",
        subtitle
    );
    target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 2.0F);

    PaletteUtil.sendMessage(target.getPlayer(), switch (selectedGod) {
      case FACEGUY -> "|red||b|[GOD] |white||b|Faceguy: |white|May fortune favor you! Because I certainly won't :^)";
      case AURORA -> "|lgreen||b|[GOD] |white||b|Aurora: |white|Bring peace in my name, my child";
      case ZEXIR -> "|purple||b|[GOD] |white||b|Zexir: |white|We will ravage this land, minion!";
      case ANYA -> "|blue||b|[GOD] |white||b|Anya: |white|It's about time, nerd!";
      default -> "BUG??";
    });
  }

  @Subcommand("addxp")
  @CommandPermission("strife.admin")
  public void addxp(CommandSender sender, OnlinePlayer target, int amount) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(target.getPlayer());
    SelectedGod selectedGod = mob.getChampion().getSaveData().getSelectedGod();
    int currentXp = mob.getChampion().getSaveData().getGodXp().getOrDefault(selectedGod, 0);
    int newXp = currentXp + amount;
    int currentGodLevel = mob.getChampion().getSaveData().getGodLevel().getOrDefault(selectedGod, 1);
    int xpToLevel = switch (currentGodLevel) {
      case 1 -> plugin.getPrayerManager().getGodLevelXpTwo();
      case 2 -> plugin.getPrayerManager().getGodLevelXpThree();
      case 3 -> plugin.getPrayerManager().getGodLevelXpFour();
      default -> -1;
    };
    PaletteUtil.sendMessage(target.getPlayer(), "|teal|Gained |cyan|" + amount + " XP |teal|for |white|" + selectedGod);
    if (xpToLevel == -1) {
      mob.getChampion().getSaveData().getGodXp().put(selectedGod, newXp);
      return;
    }
    if (newXp >= xpToLevel) {
      newXp -= xpToLevel;
      TitleUtils.sendTitle(
          target.getPlayer(),
          FaceColor.YELLOW.s() + FaceColor.BOLD + "GOD LEVEL UP",
          FaceColor.WHITE + "Your allegiance has grown stronger!"
      );
      target.getPlayer().playSound(target.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 2.0F);
      currentGodLevel++;
      mob.getChampion().getSaveData().getGodLevel().put(selectedGod, currentGodLevel);
    }
    mob.getChampion().getSaveData().getGodXp().put(selectedGod, newXp);
  }

  @Subcommand("tribute")
  @CommandPermission("strife.admin")
  public void shittyTribute(CommandSender sender, OnlinePlayer target) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(target.getPlayer());
    SelectedGod selectedGod = mob.getChampion().getSaveData().getSelectedGod();

    StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS);
    if (target.getPlayer().hasPermission("prayer.enabled") &&
        mob.getPrayer() != StatUtil.getStat(mob, StrifeStat.MAX_PRAYER_POINTS)) {
      PaletteUtil.sendMessage(target.getPlayer(), FaceColor.WHITE.s() + FaceColor.ITALIC + "Faith restored!");
      Audience audience = Audience.audience(target.getPlayer());
      audience.playSound(PrayerManager.FAITH_RESTORED);
    }
    mob.setPrayer(mob.getMaxPrayer());
    plugin.getPrayerManager().sendPrayerUpdate(target.getPlayer(), 1, false);

    if (selectedGod == SelectedGod.NONE) {
      PaletteUtil.sendMessage(target.getPlayer(),
          FaceColor.YELLOW + "You must choose a God before you can tribute! Select one of the nearby heads!");
      return;
    }
    // TODO: Not use bossshop for this UI
    Bukkit.getServer().dispatchCommand(sender, "bs open tribute-gui " + target.getPlayer().getName());
  }
}
