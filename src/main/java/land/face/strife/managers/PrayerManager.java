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
package land.face.strife.managers;

import static land.face.strife.managers.GuiManager.noShadow;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.GodPrayerDetails;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.champion.ChampionSaveData.SelectedGod;
import land.face.strife.menus.prayer.PrayerMenu;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;

public class PrayerManager {

  private final StrifePlugin plugin;
  @Getter private final PrayerMenu prayerMenu;

  private final Map<Player, Set<Prayer>> activePrayers = new WeakHashMap<>();
  @Getter private final Map<Prayer, String> prayerNames = new HashMap<>();
  @Getter private final Map<Prayer, List<String>> prayerLore = new HashMap<>();
  @Getter private final Map<Prayer, Float> prayerActivationCost = new HashMap<>();
  @Getter private final Map<Prayer, Sound> prayerSounds = new HashMap<>();
  private final Map<Prayer, Float> prayerCostPerTick = new HashMap<>();
  @Getter private final Map<Prayer, Integer> prayerLevelReq = new HashMap<>();
  @Getter private final Map<Prayer, Integer> prayerModelData = new HashMap<>();
  @Getter private final Map<SelectedGod, GodPrayerDetails> godPassiveOne = new HashMap<>();
  @Getter private final Map<SelectedGod, GodPrayerDetails> godPassiveTwo = new HashMap<>();
  @Getter private final Map<SelectedGod, GodPrayerDetails> godPassiveThree = new HashMap<>();


  @Getter private final int godLevelXpTwo;
  @Getter private final int godLevelXpThree;
  @Getter private final int godLevelXpFour;

  public static final net.kyori.adventure.sound.Sound FAITH_RESTORED = Sound.sound(
      Key.key("minecraft:custom.faith_restored"), Source.MASTER, 1f, 1f);

  private static final String ACTIVE_PRAYER_BASE = "᮴";
  private static final String INACTIVE_PRAYER_BASE = "᮳";
  private static final String BAR_NEGATIVE_SPACE = "\uF809\uF803";

  private static final long TICK_RATE = 6L;
  private static final float TICK_MULT = 1 / (20f / TICK_RATE);

  public PrayerManager(StrifePlugin plugin) {
    this.plugin = plugin;
    godLevelXpTwo = plugin.getConfig().getInt("gods.xp-for-level-two", 2000);
    godLevelXpThree = plugin.getConfig().getInt("gods.xp-for-level-three", 10000);
    godLevelXpFour = plugin.getConfig().getInt("gods.xp-for-level-four", 95000);
    for (Prayer p : Prayer.values()) {
      prayerNames.put(p, PaletteUtil.color(
          plugin.getPrayerYAML().getString("prayer." + p.toString().toLowerCase() + ".name", "Changeme")));
      prayerLore.put(p, PaletteUtil.color(
          plugin.getPrayerYAML().getStringList("prayer." + p.toString().toLowerCase() + ".lore")));
      prayerActivationCost.put(p, (float)
          plugin.getPrayerYAML().getDouble("prayer." + p.toString().toLowerCase() + ".activation-cost", 5));
      // Fuck paper, fuck kyori
      //noinspection ALL
      String soundFx = plugin.getPrayerYAML().getString("prayer." + p.toString().toLowerCase() + ".sound", "minecraft:custom.skill_up");
      float pitch = (float) plugin.getPrayerYAML().getDouble("prayer." + p.toString().toLowerCase() + ".pitch", 1);
      // Fuck paper, fuck kyori
      //noinspection ALL
      prayerSounds.put(p, Sound.sound(Key.key(soundFx), Source.MASTER, 1f, pitch));
      prayerCostPerTick.put(p, TICK_MULT * (float)
          plugin.getPrayerYAML().getDouble("prayer." + p.toString().toLowerCase() + ".per-second-cost", 5));
      prayerLevelReq.put(p, plugin.getPrayerYAML().getInt("prayer." + p.toString().toLowerCase() + ".level-req", 0));
      prayerModelData.put(p,
          plugin.getPrayerYAML().getInt("prayer." + p.toString().toLowerCase() + ".model-data", 800));
    }
    prayerMenu = new PrayerMenu(plugin);

    Bukkit.getScheduler().runTaskTimer(plugin, this::tickAllPrayer, 50L, TICK_RATE);
  }

  public boolean isPrayerActive(Player player) {
    return activePrayers.containsKey(player) && !activePrayers.get(player).isEmpty();
  }

  public boolean isPrayerActive(LivingEntity le) {
    if (!(le instanceof Player)) {
      return false;
    }
    return isPrayerActive((Player) le);
  }

  public boolean isPrayerActive(Player player, Prayer prayer) {
    return activePrayers.containsKey(player) && activePrayers.get(player).contains(prayer);
  }

  public boolean isPrayerActive(LivingEntity le, Prayer prayer) {
    if (le instanceof Player) {
      return isPrayerActive((Player) le, prayer);
    }
    return false;
  }

  public boolean activatePrayer(Player player, Prayer prayer) {
    if (isPrayerActive(player, prayer)) {
      return false;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    float cost;
    Sound sound;
    if (prayer.ordinal() > 11) {
      switch (prayer) {
        case THIRTEEN -> {
          GodPrayerDetails details = getGodPassiveOne().get(mob.getChampion().getSaveData().getSelectedGod());
          cost = details.getActivationCost();
          sound = details.getSound();
        }
        case FOURTEEN -> {
          GodPrayerDetails details = getGodPassiveTwo().get(mob.getChampion().getSaveData().getSelectedGod());
          cost = details.getActivationCost();
          sound = details.getSound();
        }
        case FIFTEEN -> {
          GodPrayerDetails details = getGodPassiveThree().get(mob.getChampion().getSaveData().getSelectedGod());
          cost = details.getActivationCost();
          sound = details.getSound();
        }
        default -> {
          cost = 10;
          sound = null;
        }
      }
    } else {
      cost = prayerActivationCost.get(prayer);
      sound = prayerSounds.get(prayer);
    }
    if (cost > mob.getPrayer()) {
      return false;
    }
    mob.setPrayer(mob.getPrayer() - cost);
    Audience audience = Audience.audience(player);
    assert sound != null;
    audience.playSound(sound);
    activePrayers.putIfAbsent(player, new HashSet<>());
    activePrayers.get(player).add(prayer);
    switch (prayer) {
      case TWELVE -> mob.applyInvincible((int) (TICK_RATE + 2));
      case THIRTEEN -> applyGodBuff(mob, godPassiveOne);
      case FOURTEEN -> applyGodBuff(mob, godPassiveTwo);
      case FIFTEEN -> applyGodBuff(mob, godPassiveThree);
    }
    sendPrayerUpdate(player, mob.getPrayer() / mob.getMaxPrayer(), true);
    //PaletteUtil.sendMessage(player, "Prayer activated " + prayer);
    return true;
  }

  public boolean disablePrayer(Player player, Prayer prayer) {
    if (!isPrayerActive(player, prayer)) {
      return false;
    }
    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 0.65f, 2);
    activePrayers.putIfAbsent(player, new HashSet<>());
    activePrayers.get(player).remove(prayer);
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    switch (prayer) {
      case THIRTEEN -> removeGodBuff(mob, godPassiveOne);
      case FOURTEEN -> removeGodBuff(mob, godPassiveTwo);
      case FIFTEEN -> removeGodBuff(mob, godPassiveThree);
    }
    sendPrayerUpdate(player, mob.getPrayer() / mob.getMaxPrayer(), isPrayerActive(player));
    //PaletteUtil.sendMessage(player, "Prayer deactivated " + prayer);
    return true;
  }

  public void setupGodPrayers() {
    putGodPrayerDetails(SelectedGod.FACEGUY, 2, "FACEGUY-2");
    putGodPrayerDetails(SelectedGod.FACEGUY, 3, "FACEGUY-3");
    putGodPrayerDetails(SelectedGod.FACEGUY, 4, "FACEGUY-4");
    putGodPrayerDetails(SelectedGod.AURORA, 2, "AURORA-2");
    putGodPrayerDetails(SelectedGod.AURORA, 3, "AURORA-3");
    putGodPrayerDetails(SelectedGod.AURORA, 4, "AURORA-4");
    putGodPrayerDetails(SelectedGod.ZEXIR, 2, "ZEXIR-2");
    putGodPrayerDetails(SelectedGod.ZEXIR, 3, "ZEXIR-3");
    putGodPrayerDetails(SelectedGod.ZEXIR, 4, "ZEXIR-4");
  }

  private void applyGodBuff(StrifeMob mob, Map<SelectedGod, GodPrayerDetails> map) {
    switch (mob.getChampion().getSaveData().getSelectedGod()) {
      case FACEGUY -> mob.addBuff(map.get(SelectedGod.FACEGUY).getLoadedBuff(), null, 0.5f);
      case AURORA -> mob.addBuff(map.get(SelectedGod.AURORA).getLoadedBuff(), null, 0.5f);
      case ZEXIR -> mob.addBuff(map.get(SelectedGod.ZEXIR).getLoadedBuff(), null, 0.5f);
      case ANYA -> mob.addBuff(map.get(SelectedGod.ANYA).getLoadedBuff(), null, 0.5f);
    }
  }

  private void removeGodBuff(StrifeMob mob, Map<SelectedGod, GodPrayerDetails> map) {
    switch (mob.getChampion().getSaveData().getSelectedGod()) {
      case FACEGUY -> mob.removeBuff(map.get(SelectedGod.FACEGUY).getLoadedBuff().getId(), null);
      case AURORA -> mob.removeBuff(map.get(SelectedGod.AURORA).getLoadedBuff().getId(), null);
      case ZEXIR -> mob.removeBuff(map.get(SelectedGod.ZEXIR).getLoadedBuff().getId(), null);
      case ANYA -> mob.removeBuff(map.get(SelectedGod.ANYA).getLoadedBuff().getId(), null);
    }
  }

  public void putGodPrayerDetails(SelectedGod god, int level, String loreAbilityId) {
    LoadedBuff loadedBuff = new LoadedBuff(god.name() + level, new HashMap<>(), god.name() + level, 1, 1);
    loadedBuff.getLoreAbilities().add(plugin.getLoreAbilityManager().getLoreAbilityFromId(loreAbilityId));
    String name = PaletteUtil.color(
        plugin.getPrayerYAML().getString("gods." + god.name() + ".prayer" + (level-1) + ".name", "test"));
    List<String> desc = PaletteUtil.color(
        plugin.getPrayerYAML().getStringList("gods." + god.name() + ".prayer" + (level-1) + ".desc"));
    float activation = (float) plugin.getPrayerYAML()
        .getDouble("gods." + god.name() + ".prayer" + (level-1) + ".activation-cost", 5);
    float perSecond = (float) plugin.getPrayerYAML()
        .getDouble("gods." + god.name() + ".prayer" + (level-1) + ".per-second-cost", 5);
    // Fuck paper, fuck kyori
    //noinspection ALL
    String soundFx = plugin.getPrayerYAML().getString("gods." + god.name() + ".prayer" + (level-1) + ".sound", "minecraft:custom.skill_up");
    float pitch = (float) plugin.getPrayerYAML().getDouble("gods." + god.name() + ".prayer" + (level-1) + ".pitch", 1);
    GodPrayerDetails prayerDetails = new GodPrayerDetails();
    prayerDetails.setName(name);
    prayerDetails.setDescription(desc);
    prayerDetails.setActivationCost(activation);
    prayerDetails.setPerTickCost(perSecond * TICK_MULT);
    prayerDetails.setLoadedBuff(loadedBuff);
    // Fuck paper, fuck kyori
    //noinspection ALL
    prayerDetails.setSound(Sound.sound(Key.key(soundFx), Source.MASTER, 1f, pitch));
    switch (level) {
      case 2: godPassiveOne.put(god, prayerDetails);
      case 3: godPassiveTwo.put(god, prayerDetails);
      case 4: godPassiveThree.put(god, prayerDetails);
    }
  }

  public void tickAllPrayer() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      tickPrayer(p);
    }
  }

  private void tickPrayer(Player p) {
    if (!isPrayerActive(p)) {
      return;
    }
    StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(p);
    float pietyMult = 100f / (100f + playerMob.getStat(StrifeStat.PRAYER_COST));
    Iterator<Prayer> it = activePrayers.get(p).iterator();
    boolean removed = false;
    while (it.hasNext()) {
      Prayer prayer = it.next();
      float currentPrayer = playerMob.getPrayer();
      float cost;
      if (prayer.ordinal() > 11) {
        switch (prayer) {
          case THIRTEEN -> cost = getGodPassiveOne()
              .get(playerMob.getChampion().getSaveData().getSelectedGod()).getPerTickCost();
          case FOURTEEN -> cost = getGodPassiveTwo()
              .get(playerMob.getChampion().getSaveData().getSelectedGod()).getPerTickCost();
          case FIFTEEN -> cost = getGodPassiveThree()
              .get(playerMob.getChampion().getSaveData().getSelectedGod()).getPerTickCost();
          default -> cost = 10;
        }
      } else {
        cost = prayerCostPerTick.get(prayer);
      }
      cost *= pietyMult;
      if (cost >= currentPrayer) {
        removed = true;
        it.remove();
      } else {
        switch (prayer) {
          case TWELVE -> playerMob.applyInvincible((int) (TICK_RATE + 2));
          case THIRTEEN -> applyGodBuff(playerMob, godPassiveOne);
          case FOURTEEN -> applyGodBuff(playerMob, godPassiveTwo);
          case FIFTEEN -> applyGodBuff(playerMob, godPassiveThree);
        }
        playerMob.setPrayer(currentPrayer - cost);
      }
    }
    if (removed) {
      p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 0.65f, 2);
    }
    if (activePrayers.get(p).isEmpty()) {
      playerMob.setPrayer(0);
      sendPrayerUpdate(p, 0, false);
    } else {
      float prayerPercent = playerMob.getPrayer() / playerMob.getMaxPrayer();
      sendPrayerUpdate(p, prayerPercent, true);
    }
  }

  public void sendPrayerUpdate(Player player, float prayerPercent, boolean isActive) {
    int index = (int) Math.ceil(16f * prayerPercent);
    boolean rightHanded = player.getMainHand() == MainHand.RIGHT;
    TextComponent component = isActive ? ACTIVE_PRAYER_ICONS.get(index) : INACTIVE_PRAYER_ICONS.get(index);
    if (rightHanded) {
      plugin.getGuiManager().updateComponent(player,
          new GUIComponent("god-slot", component, 19, 99, Alignment.LEFT));
    } else {
      plugin.getGuiManager().updateComponent(player,
          new GUIComponent("god-slot", component, 19, -100, Alignment.RIGHT));
    }
  }

  public void sendPrayerUpdate(Player player, boolean isActive) {
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(player);
    sendPrayerUpdate(player, mob.getPrayer() / mob.getMaxPrayer(), isActive);
  }

  private static final List<TextComponent> ACTIVE_PRAYER_ICONS = List.of(
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE)),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侏")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侎")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侍")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侌")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "例")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侊")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侉")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侈")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侇")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "來")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侅")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侄")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侃")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侂")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侁")),
      noShadow(new TextComponent(ACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侀"))
  );

  private static final List<TextComponent> INACTIVE_PRAYER_ICONS = List.of(
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE)),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侏")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侎")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侍")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侌")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "例")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侊")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侉")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侈")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侇")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "來")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侅")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侄")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侃")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侂")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侁")),
      noShadow(new TextComponent(INACTIVE_PRAYER_BASE + BAR_NEGATIVE_SPACE + "侀"))
  );

  public enum Prayer {
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN,
    ELEVEN,
    TWELVE,
    THIRTEEN,
    FOURTEEN,
    FIFTEEN
  }
}
