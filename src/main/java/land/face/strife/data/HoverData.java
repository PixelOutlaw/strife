package land.face.strife.data;

public class HoverData {

  int blockX;
  int blockZ;
  double groundBlockY;

  public HoverData(int blockX, int blockZ, double groundBlockY) {
    this.blockX = blockX;
    this.blockZ = blockZ;
    this.groundBlockY = groundBlockY;
  }

  public int getBlockX() {
    return blockX;
  }

  public void setBlockX(int blockX) {
    this.blockX = blockX;
  }

  public int getBlockZ() {
    return blockZ;
  }

  public void setBlockZ(int blockZ) {
    this.blockZ = blockZ;
  }

  public double getGroundBlockY() {
    return groundBlockY;
  }

  public void setGroundBlockY(double groundBlockY) {
    this.groundBlockY = groundBlockY;
  }
}
