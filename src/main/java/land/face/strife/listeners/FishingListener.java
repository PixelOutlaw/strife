package land.face.strife.listeners;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.util.Vector;

public class FishingListener implements Listener {

  private final StrifePlugin plugin;

  public FishingListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onCastFishingRod(PlayerFishEvent event) {
    if (event.getState() != State.FISHING) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    Champion champion = mob.getChampion();

    event.getHook().setCustomName(StringExtensionsKt.chatColorize("&b&l<><"));
    event.getHook().setCustomNameVisible(true);

    Vector bobberVelocity = event.getHook().getVelocity().clone();
    bobberVelocity.multiply(0.65f * (1 + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    event.getHook().setVelocity(bobberVelocity);

    float speedBonus = mob.getStat(StrifeStat.FISHING_SPEED) +
        champion.getLifeSkillLevel(LifeSkillType.FISHING);
    float fishMult = 1 / (1 + (speedBonus / 100));
    int minFishTime = 20 + (int) (180f * fishMult);
    int maxFishTime = minFishTime + (int) (140f * Math.random() * fishMult);

    event.getHook().setMaxWaitTime(maxFishTime);
    event.getHook().setMinWaitTime(minFishTime);
    event.getHook().setWaitTime((int)
        (minFishTime + (Math.random() * (maxFishTime - minFishTime))));
  }
}
