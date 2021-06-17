package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.Ability.TargetType;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.SoulUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SoulManager {

  private final StrifePlugin plugin;
  private final Set<SoulTimer> souls = new HashSet<>();
  private final Set<String> deathWorlds = new HashSet<>();
  private final String reviveMessage;
  private final String soulName;
  private final ChatColor soulColor = ChatColor.of(new Color(72, 144, 198));

  public SoulManager(StrifePlugin plugin) {
    this.plugin = plugin;
    deathWorlds.addAll(plugin.getSettings().getStringList("config.death-worlds"));
    reviveMessage = plugin.getSettings().getString("language.generic.revive-exp-message", "");
    soulName = plugin.getSettings().getString("language.generic.soul-name", "");
  }

  public boolean isDeathWorld(String worldName) {
    return deathWorlds.contains(worldName);
  }

  public boolean canSeeSouls(Player player) {
    ChampionSaveData data = plugin.getChampionManager().getChampion(player).getSaveData();
    for (Ability ability : data.getAbilities().values()) {
      if (ability.getTargetType() == TargetType.NEAREST_SOUL) {
        return true;
      }
    }
    return false;
  }

  public void createSoul(StrifeMob mob) {
    SoulTimer oldSoul = null;
    Player player = (Player) mob.getEntity();
    for (SoulTimer soulTimer : souls) {
      if (soulTimer.getOwner() == player) {
        oldSoul = soulTimer;
        break;
      }
    }
    if (oldSoul != null) {
      removeSoul(oldSoul);
    }
    Location location = player.getLocation().clone().add(0, 1, 0);

    String text = soulColor + "" + ChatColor.ITALIC + soulName.replace("{n}", player.getName());
    Hologram soulHead = SoulUtil.createSoul(player, text, location.clone().add(0, 0.75, 0));
    souls.add(new SoulTimer(mob, soulHead, location));
  }

  public void removeSoul(SoulTimer soulTimer) {
    souls.remove(soulTimer);
    soulTimer.getSoulHead().delete();
    soulTimer.cancel();
  }

  public SoulTimer getNearestSoul(LivingEntity le, float maxDistSquared) {
    SoulTimer selectedSoul = null;
    double selectedDist = Math.pow(maxDistSquared, 2);
    for (SoulTimer soulTimer : souls) {
      if (!soulTimer.getLocation().getWorld().equals(le.getWorld())) {
        continue;
      }
      double dist = soulTimer.getLocation().distanceSquared(le.getLocation());
      if (dist < selectedDist) {
        selectedSoul = soulTimer;
        selectedDist = dist;
      }
    }
    return selectedSoul;
  }

  public SoulTimer getSoul(Player player) {
    for (SoulTimer soulTimer : souls) {
      if (soulTimer.getOwner() == player) {
        return soulTimer;
      }
    }
    return null;
  }

  public void setLostExp(Player player, double amount) {
    SoulTimer soul = getSoul(player);
    if (soul == null) {
      return;
    }
    soul.setLostExp(amount);
  }

  public void revive(Player player, int xpRestored) {
    SoulTimer soul = getSoul(player);
    if (soul == null) {
      sendMessage(player, "Sorry! You've been dead too long to be revived :(");
      return;
    }
    plugin.getExperienceManager().addExperience(player, xpRestored, true);
    sendMessage(player, reviveMessage.replace("{n}", String.valueOf(xpRestored)));
    player.teleport(soul.getLocation());
    removeSoul(soul);
  }

  public void revive(Player player) {
    revive(player, 0);
  }

}
