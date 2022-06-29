package land.face.strife.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.stats.AbilitySlot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinAndLeaveListener implements Listener {

  private final StrifePlugin plugin;

  private final static String UNUSED_MESSAGE_1 =
      "&6&lLevelup! You have &f&l{0} &6&lunused Levelpoints!";
  private final static String UNUSED_MESSAGE_2 =
      "&6&lOpen your inventory or use &e&l/levelup &6&lto spend them!";
  private final static String UNUSED_PATH =
      "&f&lYou have a choice to make! Use &e&l/levelup &f&lto select a path!";

  private final Set<UUID> mounted = new HashSet<>();

  public JoinAndLeaveListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    event.getPlayer().setGravity(true);
    plugin.getGuiManager().setupGui(event.getPlayer());

    StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    Champion champion = playerMob.getChampion();
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getBoostManager().updateGlobalBoostStatus(event.getPlayer());

    if (champion.getUnusedStatPoints() > 0) {
      notifyUnusedPoints(event.getPlayer(), champion.getUnusedStatPoints());
    }
    if (event.getPlayer().getLevel() / 10 > champion.getSaveData().getPathMap().size()) {
      notifyUnusedPaths(event.getPlayer());
    }

    plugin.getCounterManager().clearCounters(event.getPlayer());
    ensureAbilitiesDontInstantCast(event.getPlayer());

    plugin.getChampionManager().verifyStatValues(champion);
    plugin.getChampionManager().update(event.getPlayer());

    if (champion.getSaveData().isOnMount()) {
      plugin.getPlayerMountManager().spawnMount(event.getPlayer());
    }

    Bukkit.getScheduler().runTaskLater(plugin, () ->
        plugin.getStrifeMobManager().updateCollisions(event.getPlayer()), 20L);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getPlayerMountManager().updateSelectedMount(event.getPlayer());
      if (mounted.contains(event.getPlayer().getUniqueId())) {
        plugin.getPlayerMountManager().spawnMount(event.getPlayer());
        mounted.remove(event.getPlayer().getUniqueId());
      }
    }, 40L);

    event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(200);
    event.getPlayer().getAttribute(GENERIC_ATTACK_SPEED).setBaseValue(1000);
    plugin.getAttackSpeedManager().getAttackMultiplier(playerMob, 1);

    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 1L);
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 10L);
    }

    plugin.getBossBarManager().createBars(event.getPlayer());
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getChampionManager().pushCloseSkills(champion);
      plugin.getBossBarManager().updateBar(event.getPlayer(), 1,
          plugin.getSkillExperienceManager().updateSkillString(champion));
    }, 33L);

    plugin.getGuiManager().updateLevelDisplay(event.getPlayer());
    //plugin.getGuiManager().updateEquipmentDisplay(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    doPlayerLeave(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerKick(final PlayerKickEvent event) {
    doPlayerLeave(event.getPlayer());
  }

  private void doPlayerLeave(Player player) {
    if (plugin.getPlayerMountManager().isMounted(player)) {
      mounted.add(player.getUniqueId());
    }
    plugin.getPlayerMountManager().despawn(player.getUniqueId());
    plugin.getAbilityManager().unToggleAll(player);
    plugin.getStrifeMobManager().saveEnergy(player);
    plugin.getBoostManager().removeBooster(player.getUniqueId());
    plugin.getAbilityManager().savePlayerCooldowns(player);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_A);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_B);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_C);
    plugin.getCounterManager().clearCounters(player);
    plugin.getBossBarManager().clearBars(player);
  }

  private void notifyUnusedPoints(final Player player, final int unused) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      MessageUtils.sendMessage(player, UNUSED_MESSAGE_1.replace("{0}", String.valueOf(unused)));
      MessageUtils.sendMessage(player, UNUSED_MESSAGE_2);
    }, 20L * 5);
  }

  private void notifyUnusedPaths(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        MessageUtils.sendMessage(player, UNUSED_PATH), 20L * 5 + 1);
  }

  private void ensureAbilitiesDontInstantCast(Player player) {
    plugin.getAbilityManager().setGlobalCooldown(player, 30);
    if (player.getInventory().getHeldItemSlot() < 3) {
      player.getInventory().setHeldItemSlot(3);
    }
  }
}
