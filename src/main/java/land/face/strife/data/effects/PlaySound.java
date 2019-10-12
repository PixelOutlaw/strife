package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.Sound;

public class PlaySound extends Effect {

  private Sound sound;
  private float volume;
  private float pitch;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = target.getEntity().getLocation().clone();
    loc.getWorld().playSound(loc, sound, volume, pitch);
  }

  public void playAtLocation(Location location) {
    location.getWorld().playSound(location, sound, volume, pitch);
  }

  public Sound getSound() {
    return sound;
  }

  public void setSound(Sound sound) {
    this.sound = sound;
  }

  public float getVolume() {
    return volume;
  }

  public void setVolume(float volume) {
    this.volume = volume;
  }

  public float getPitch() {
    return pitch;
  }

  public void setPitch(float pitch) {
    this.pitch = pitch;
  }
}
