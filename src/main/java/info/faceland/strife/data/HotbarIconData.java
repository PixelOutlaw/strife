package info.faceland.strife.data;

import info.faceland.strife.data.ability.Ability;
import org.bukkit.inventory.ItemStack;

public class HotbarIconData {

  private Ability ability;
  private ItemStack itemStack;

  public HotbarIconData(Ability ability, ItemStack stack) {
    this.ability = ability;
    this.itemStack = stack;
  }

  public Ability getAbility() {
    return ability;
  }

  public void setAbility(Ability ability) {
    this.ability = ability;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
  }
}

