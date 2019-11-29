package land.face.strife.data;

public class BlockData {

  private long lastHit;
  private double storedBlock;
  private int runes;

  public BlockData(long lastHit, double storedBlock) {
    this.lastHit = lastHit;
    this.storedBlock = storedBlock;
    this.runes = 0;
  }

  public long getLastHit() {
    return lastHit;
  }

  public void setLastHit(long lastHit) {
    this.lastHit = lastHit;
  }

  public double getStoredBlock() {
    return storedBlock;
  }

  public void setStoredBlock(double storedBlock) {
    this.storedBlock = storedBlock;
  }

  public int getRunes() {
    return runes;
  }

  public void setRunes(int runes) {
    this.runes = runes;
  }
}
