package land.face.strife.events;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class VagabondEquipEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  @Getter
  private final LivingEntity livingEntity;
  @Getter
  private final String combatClass;
  @Getter
  private final int level;

  public VagabondEquipEvent(LivingEntity livingEntity, String combatClass, int level) {
    this.livingEntity = livingEntity;
    this.level = level;
    this.combatClass = combatClass;
  }

  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

}
