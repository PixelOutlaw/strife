package land.face.strife.data;

public class DamageOverTimeData {
  private int storedDamage;
  private long lastAddition;

  public DamageOverTimeData() {
    this.lastAddition = System.currentTimeMillis();
  }

  public int getStoredDamage() {
    return storedDamage;
  }

  public void setStoredDamage(int storedDamage) {
    this.storedDamage = storedDamage;
  }

  public long getLastAddition() {
    return lastAddition;
  }

  public void setLastAddition(long lastAddition) {
    this.lastAddition = lastAddition;
  }
}
