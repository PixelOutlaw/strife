package land.face.strife.data;

import java.util.Set;
import org.bukkit.entity.Player;

public class NearbyPlayerCache {

  private Set<Player> players;
  private long expiryTimestamp;

  public Set<Player> getPlayers() {
    return players;
  }

  public void setPlayers(Set<Player> players) {
    this.players = players;
  }

  public long getExpiryTimestamp() {
    return expiryTimestamp;
  }

  public void setExpiryTimestamp(long expiryTimestamp) {
    this.expiryTimestamp = expiryTimestamp;
  }

}
