package land.face.strife.hooks;

import java.util.HashSet;
import java.util.Set;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.strife.util.LogUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SnazzyPartiesHook {

  public Set<Player> getNearbyPartyMembers(Player player, Location location, double range) {
    Set<Player> members = new HashSet<>();
    members.add(player);
    if (getPlugin() == null) {
      return members;
    }
    Party party = getPlugin().getPartyManager().getParty(player);
    if (party == null) {
      return members;
    }
    members.addAll(getPlugin().getPartyManager().getNearbyPlayers(party, location, range));
    return members;
  }

  public boolean inSameParty(Player player, Player player2) {
    return getPlugin().getPartyManager().areInSameParty(player, player2);
  }

  private SnazzyPartiesPlugin getPlugin() {
    try {
      return SnazzyPartiesPlugin.getInstance();
    } catch (Exception e) {
      LogUtil.printWarning(
          "Encountered an error when trying to get party info. Is a valid party plugin loaded?");
      return null;
    }
  }
}
