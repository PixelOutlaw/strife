package land.face.strife.data.effects;

import land.face.strife.data.LoreAbility;

public class FiniteUsesEffect {

  private LoreAbility loreAbility;
  private int uses;
  private long expiration;

  public LoreAbility getLoreAbility() {
    return loreAbility;
  }

  public void setLoreAbility(LoreAbility loreAbility) {
    this.loreAbility = loreAbility;
  }

  public int getUses() {
    return uses;
  }

  public void setUses(int uses) {
    this.uses = uses;
  }

  public long getExpiration() {
    return expiration;
  }

  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }
}
