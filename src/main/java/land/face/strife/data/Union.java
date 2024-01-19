package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.lang.ref.WeakReference;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.managers.UnionManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

@Getter
@Setter
public class Union {

  private UnionManager unionManager;
  private String id;
  private WeakReference<LivingEntity> owner;
  private LivingEntity stand;
  private ActiveModel activeModel;
  private ModeledEntity modeledEntity;
  private LoadedBuff loadedBuff;
  private int ticksRemaining;
  private int animationTicks;

  public void build(LivingEntity owner, Location location) {
    this.owner = new WeakReference<>(owner);
    stand = location.getWorld().spawn(location, ArmorStand.class);
    stand.setGravity(false);
    stand.setAI(false);
    stand.setInvulnerable(true);
    ChunkUtil.setDespawnOnUnload(stand);
    modeledEntity = ModelEngineAPI.createModeledEntity(stand);
    if (modeledEntity != null) {
      modeledEntity.getBase().getBodyRotationController().setYBodyRot(stand.getLocation().getYaw());
      // For unions, do not override hitbox or model
      modeledEntity.addModel(activeModel, false);
      modeledEntity.setBaseEntityVisible(true);
    }
  }

  public void tick() {
    LivingEntity o = owner.get();
    if (o == null || o.getWorld() != stand.getWorld()) {
      ticksRemaining = -1;
    } else {
      stand.teleport(o);
      ticksRemaining--;
    }
    animationTicks--;
  }

  public boolean isExpired() {
    return ticksRemaining < 0;
  }

  public boolean canCancel() {
    return ticksRemaining < 0 && animationTicks < 0;
  }

}
