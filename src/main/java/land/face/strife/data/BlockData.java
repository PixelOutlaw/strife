package land.face.strife.data;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.HashSet;
import java.util.Set;

public class BlockData {

  private long runeFalloff;
  private int runes;
  private final Set<Hologram> runeHolograms = new HashSet<>();

  public BlockData() {
    this.runes = 0;
  }

  public int getRunes() {
    return runes;
  }

  public void setRunes(int runes) {
    if (runes >= this.runes) {
      runeFalloff = System.currentTimeMillis() + 300000L;
    }
    this.runes = runes;
  }

  public Set<Hologram> getRuneHolograms() {
    return runeHolograms;
  }

  public long getRuneFalloff() {
    return runeFalloff;
  }
}
