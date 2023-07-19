package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.pojo.OrderEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class OrderEntityEffect extends LocationEffect {

  @Getter @Setter
  private int travelTime;
  @Getter @Setter
  private float minimumSpeed;

  @Getter @Setter
  private String model;
  @Getter @Setter
  private List<Effect> impactEffects = new ArrayList<>();
  @Getter @Setter
  private List<Effect> idleEffects = new ArrayList<>();
  @Getter @Setter
  private List<Effect> moveEffects = new ArrayList<>();
  @Getter @Setter
  private List<Effect> stopEffects = new ArrayList<>();
  @Getter @Setter
  private List<Effect> riseEffects = new ArrayList<>();

  @Getter
  private final Map<UUID, OrderEntity> activeEntities = new HashMap<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, target.getEntity().getLocation());
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    if (activeEntities.containsKey(caster.getEntity().getUniqueId())) {
      activeEntities.get(caster.getEntity().getUniqueId()).command(location);
    } else {
      activeEntities.put(caster.getEntity().getUniqueId(),
          new OrderEntity(getPlugin(), this, caster, location));
    }
  }
}