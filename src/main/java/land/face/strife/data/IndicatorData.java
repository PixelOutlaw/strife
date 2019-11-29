package land.face.strife.data;

import java.util.HashSet;
import java.util.Set;
import land.face.strife.managers.IndicatorManager;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class IndicatorData {

  private final Vector vectorData;
  private final IndicatorStyle style;
  private Set<Player> owners = new HashSet<>();
  private int stage = 0;

  public IndicatorData(Vector vectorData, IndicatorStyle style) {
    this.vectorData = vectorData;
    this.style = style;
  }

  public Set<Player> getOwners() {
    return owners;
  }

  public void addOwner(Player player) {
    owners.add(player);
  }

  public Vector getVectorData() {
    return vectorData;
  }

  public IndicatorStyle getStyle() {
    return style;
  }

  public int getStage() {
    return stage;
  }

  public void setStage(int stage) {
    this.stage = stage;
  }

  public static Vector getRelativeChange(IndicatorData data) {
    switch (data.getStyle()) {
      case SHAKE:
        double shakeX = data.getVectorData().getX();
        double shakeY = data.getVectorData().getY();
        double shakeZ = data.getVectorData().getZ();
        return new Vector(shakeX - shakeX * 2 * Math.random(), shakeY - shakeY * 2 * Math.random(),
            shakeZ - shakeZ * 2 * Math.random());
      case GRAVITY:
        Vector vec = data.getVectorData();
        data.getVectorData()
            .setY(data.getVectorData().getY() - IndicatorManager.GRAVITY_FALL_SPEED);
        return vec;
      case FLOAT_UP:
        return data.getVectorData().clone();
    }
    throw new IllegalStateException();
  }

  public enum IndicatorStyle {
    FLOAT_UP,
    SHAKE,
    GRAVITY
  }
}
