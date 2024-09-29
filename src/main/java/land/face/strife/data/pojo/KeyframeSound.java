package land.face.strife.data.pojo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class KeyframeSound {

  @Getter @Setter
  private String sound = "minecraft:entity.pig.ambient";
  @Getter @Setter
  private float volume = 1;
  @Getter @Setter
  private float pitch = 1;

  public void play(Location location) {
    location.getWorld().playSound(location, sound, SoundCategory.MASTER, volume, pitch);
  }

  public void play(Player player, Location location) {
    player.playSound(location, sound, SoundCategory.MASTER, volume, pitch);
  }

}
