package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.regex.Pattern;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.pojo.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.events.AutoFishEvent;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FishingListener implements Listener {

  private final StrifePlugin plugin;

  private static final Pattern pattern = Pattern.compile("[^/d.-]");

  public FishingListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBite(PlayerFishEvent event) {
    if (event.getState() != State.BITE) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    if (Math.random() < mob.getStat(StrifeStat.AUTO_FISH_CHANCE) / 100) {
      event.setCancelled(true);
      AutoFishEvent autoFishEvent = new AutoFishEvent(mob, event.getHook().getLocation());
      Bukkit.getPluginManager().callEvent(autoFishEvent);
      consumeBait(mob);
      degradeRod(event.getPlayer().getEquipment().getItemInMainHand());
    }
    applyWaitTime(mob, event.getPlayer().getEquipment().getItemInMainHand(), event.getHook());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCatch(PlayerFishEvent event) {
    if (event.getState() != State.CAUGHT_FISH) {
      return;
    }
    consumeBait(plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    degradeRod(event.getPlayer().getEquipment().getItemInMainHand());
  }

  private void consumeBait(StrifeMob mob) {
    if (mob.getEntity().getEquipment().getItemInOffHand() == null ||
        mob.getEntity().getEquipment().getItemInOffHand().getType() == Material.AIR) {
      return;
    }
    if (Math.random() < mob.getStat(StrifeStat.FISH_BAIT_KEEP) / 100) {
      return;
    }
    PlayerData data = DeluxeInvyPlugin.getInstance()
        .getPlayerManager().getPlayerData((Player) mob.getEntity());
    ItemStack stack = data.getEquipmentItem(DeluxeSlot.OFF_HAND);
    if (data == null) {
      return;
    }
    if (stack.getType() == Material.WHEAT_SEEDS) {
      if (stack.getAmount() == 1) {
        data.setEquipmentItem(DeluxeSlot.OFF_HAND, new ItemStack(Material.AIR));
      } else {
        stack.setAmount(stack.getAmount() - 1);
        mob.getEntity().getEquipment().getItemInOffHand().setAmount(stack.getAmount());
      }
    }
  }

  private void degradeRod(ItemStack stack) {
    if (stack == null || stack.getType() != Material.FISHING_ROD || stack.getDurability() == 63) {
      return;
    }
    double repairMod = 100;
    for (String loreLine : TextUtils.getLore(stack)) {
      if (!loreLine.endsWith(" Increased Durability")) {
        continue;
      }
      String strippedLore = ChatColor.stripColor(loreLine);
      repairMod += Double.parseDouble(pattern.matcher(strippedLore).replaceAll(""));
    }
    if (Math.random() < 0.05 / (repairMod / 100)) {
      stack.setDurability((short) (stack.getDurability() + 1));
    }
  }

  @EventHandler
  public void onCastFishingRod(PlayerFishEvent event) {
    if (event.getState() != State.FISHING) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());

    event.getHook().setCustomName(StringExtensionsKt.chatColorize("&b&l<><"));
    event.getHook().setCustomNameVisible(true);

    Vector bobberVelocity = event.getHook().getVelocity().clone();
    bobberVelocity.multiply(0.65f * (1 + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    event.getHook().setVelocity(bobberVelocity);

    applyWaitTime(mob, event.getPlayer().getEquipment().getItemInMainHand(), event.getHook());
  }

  private void applyWaitTime(StrifeMob mob, ItemStack fishingRod, FishHook hook) {

    boolean damaged = (fishingRod == null || fishingRod.getType() != Material.FISHING_ROD || fishingRod.getDurability() == 63);

    float speedBonus = mob.getStat(StrifeStat.FISHING_SPEED) +
        mob.getChampion().getLifeSkillLevel(LifeSkillType.FISHING);
    speedBonus = Math.max(-75, speedBonus);
    float fishDivisor = 1f + (speedBonus / 100f);
    int minFishTime = 20 + (int) (180f / fishDivisor);
    int maxFishTime = minFishTime + (int) (Math.random() * (200f / fishDivisor));

    if (damaged) {
      minFishTime *= 4;
      maxFishTime *= 4;
    }

    // Reset hook values because spigot is stupid
    hook.setMinWaitTime(0);
    hook.setMaxWaitTime(100000);

    // Actually set hook values
    hook.setMinWaitTime(minFishTime);
    hook.setMaxWaitTime(maxFishTime);
    hook.setApplyLure(false);

    //event.getHook().setWaitTime((int)
    //    (minFishTime + (Math.random() * (maxFishTime - minFishTime))));
  }
}
