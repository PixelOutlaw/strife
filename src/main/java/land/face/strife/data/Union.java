package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.lang.ref.WeakReference;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.managers.UnionManager;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.TargetingUtil;
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
  private WeakReference<StrifeMob> owner;
  private ArmorStand stand;
  private ActiveModel activeModel;
  private ModeledEntity modeledEntity;
  private LoadedBuff loadedBuff;
  private Ability autoAttackAbility;
  private long stallStamp = 0;
  private int ticksRemaining;

  public void build(StrifeMob owner, Location location) {
    this.owner = new WeakReference<>(owner);
    stand = location.getWorld().spawn(location, ArmorStand.class, e -> {
      e.setInvisible(true);
      e.setInvulnerable(true);
      e.setCollidable(false);
      e.setAI(false);
      e.setGravity(false);
      e.setSilent(true);
      e.setMarker(true);
      e.setCanTick(false);
      ChunkUtil.setDespawnOnUnload(e);
    });
    modeledEntity = ModelEngineAPI.createModeledEntity(stand);
    if (modeledEntity != null) {
      modeledEntity.getBase().getBodyRotationController().setYBodyRot(stand.getLocation().getYaw());
      // For unions, do not override hitbox or model
      modeledEntity.addModel(activeModel, false);
      modeledEntity.setBaseEntityVisible(true);
    }
  }

  public void tick() {
    StrifeMob mob = owner.get();
    if (mob == null || mob.getEntity() == null || mob.getEntity().getWorld() != stand.getWorld()) {
      ticksRemaining = -1;
    } else {
      stand.teleport(mob.getEntity());
      ticksRemaining--;
    }
    if (ticksRemaining % 10 == 0) {
      if (System.currentTimeMillis() > stallStamp && autoAttackAbility != null) {
        Set<LivingEntity> targets = TargetingUtil.getEntitiesInLine(mob.getEntity().getEyeLocation(), 4, 3);
        TargetingUtil.filterFriendlyEntities(targets, mob, false);
        if (!targets.isEmpty()) {
          doAutoAttack();
          stallStamp = System.currentTimeMillis() + 2000;
        }
      }
      if (loadedBuff != null) {
        mob.addBuff(loadedBuff, null, 11);
      }
    }
  }

  private void doAutoAttack() {
    StrifePlugin.getInstance().getAbilityManager().execute(autoAttackAbility, owner.get(), owner.get(), AbilitySlot.INVALID);
  }

  public boolean isExpired() {
    return ticksRemaining < 0;
  }

  public boolean canCancel() {
    return ticksRemaining < 0 && stallStamp < System.currentTimeMillis();
  }

}
